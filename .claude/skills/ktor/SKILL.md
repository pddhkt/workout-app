---
name: ktor
description: Ktor HTTP client patterns for KMP. Use when implementing API clients, network requests, or remote data sources.
---

# Ktor Client Skill

## Contents

- [Overview](#overview)
- [Client Setup](#client-setup)
- [Request Patterns](#request-patterns)
- [Serialization](#serialization)
- [Error Handling](#error-handling)
- [Authentication](#authentication)
- [Testing](#testing)

---

## Overview

Ktor is a KMP-compatible HTTP client:
- Async with coroutines
- Multiplatform engines (OkHttp, Darwin, etc.)
- Built-in serialization support
- Extensible plugin system

---

## Client Setup

### Basic Configuration

```kotlin
// commonMain/data/remote/HttpClientFactory.kt
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    return HttpClient {
        // JSON serialization
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        // Logging (debug only)
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }

        // Timeouts
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        // Default request configuration
        defaultRequest {
            url("https://api.example.com/v1/")
        }
    }
}
```

### Platform Engines

```kotlin
// androidMain
actual fun createPlatformEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            // OkHttp-specific config
        }
    }
}

// iosMain
actual fun createPlatformEngine(): HttpClientEngine {
    return Darwin.create {
        // Darwin-specific config
    }
}
```

### Koin Integration

```kotlin
// commonMain/di/NetworkModule.kt
val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single { createHttpClient() }

    single { UserApi(get()) }
    single { BookApi(get()) }
}
```

---

## Request Patterns

### Basic Requests

```kotlin
class BookApi(private val client: HttpClient) {

    suspend fun getBooks(): List<BookDto> {
        return client.get("books").body()
    }

    suspend fun getBook(id: String): BookDto {
        return client.get("books/$id").body()
    }

    suspend fun createBook(book: CreateBookDto): BookDto {
        return client.post("books") {
            contentType(ContentType.Application.Json)
            setBody(book)
        }.body()
    }

    suspend fun updateBook(id: String, book: UpdateBookDto): BookDto {
        return client.put("books/$id") {
            contentType(ContentType.Application.Json)
            setBody(book)
        }.body()
    }

    suspend fun deleteBook(id: String) {
        client.delete("books/$id")
    }
}
```

### Query Parameters

```kotlin
suspend fun searchBooks(
    query: String,
    page: Int = 1,
    limit: Int = 20
): PaginatedResponse<BookDto> {
    return client.get("books/search") {
        parameter("q", query)
        parameter("page", page)
        parameter("limit", limit)
    }.body()
}
```

### Path Parameters

```kotlin
suspend fun getBooksByAuthor(authorId: String): List<BookDto> {
    return client.get("authors/$authorId/books").body()
}
```

### Headers

```kotlin
suspend fun getProtectedResource(token: String): ResourceDto {
    return client.get("protected/resource") {
        header("Authorization", "Bearer $token")
        header("X-Request-Id", randomUUID())
    }.body()
}
```

---

## Serialization

### DTO Definitions

```kotlin
// commonMain/data/dto/BookDto.kt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookDto(
    val id: String,
    val title: String,
    val description: String?,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("page_count")
    val pageCount: Int?,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long
)

@Serializable
data class CreateBookDto(
    val title: String,
    val description: String? = null,
    @SerialName("author_id")
    val authorId: String
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_count")
    val totalCount: Int
)
```

### Mapping to Domain

```kotlin
// commonMain/data/dto/Mappers.kt
fun BookDto.toDomain(): Book = Book(
    id = id,
    title = title,
    description = description ?: "",
    authorId = authorId,
    pageCount = pageCount,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt)
)

fun Book.toCreateDto(): CreateBookDto = CreateBookDto(
    title = title,
    description = description.takeIf { it.isNotBlank() },
    authorId = authorId
)
```

---

## Error Handling

### API Error Response

```kotlin
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

sealed class ApiException(message: String) : Exception(message) {
    data class BadRequest(val error: ApiError) : ApiException(error.message)
    data class Unauthorized(val error: ApiError) : ApiException(error.message)
    data class NotFound(val error: ApiError) : ApiException(error.message)
    data class ServerError(val error: ApiError) : ApiException(error.message)
    data class NetworkError(override val cause: Throwable) : ApiException(cause.message ?: "Network error")
}
```

### Response Handling Plugin

```kotlin
fun HttpClientConfig<*>.installErrorHandling() {
    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val error = try {
                    response.body<ApiError>()
                } catch (e: Exception) {
                    ApiError("UNKNOWN", "Unknown error")
                }

                throw when (response.status.value) {
                    400 -> ApiException.BadRequest(error)
                    401 -> ApiException.Unauthorized(error)
                    404 -> ApiException.NotFound(error)
                    in 500..599 -> ApiException.ServerError(error)
                    else -> ApiException.ServerError(error)
                }
            }
        }

        handleResponseExceptionWithRequest { exception, _ ->
            when (exception) {
                is ApiException -> throw exception
                else -> throw ApiException.NetworkError(exception)
            }
        }
    }
}
```

### Safe API Calls

```kotlin
// In repository
suspend fun getBook(id: String): Result<Book> {
    return try {
        val dto = bookApi.getBook(id)
        Result.Success(dto.toDomain())
    } catch (e: ApiException.NotFound) {
        Result.Error(BookNotFoundException(id))
    } catch (e: ApiException) {
        Result.Error(e)
    }
}
```

---

## Authentication

### Token Interceptor

```kotlin
fun HttpClientConfig<*>.installAuth(tokenProvider: TokenProvider) {
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(
                    accessToken = tokenProvider.getAccessToken() ?: "",
                    refreshToken = tokenProvider.getRefreshToken() ?: ""
                )
            }

            refreshTokens {
                val refreshToken = tokenProvider.getRefreshToken()
                if (refreshToken != null) {
                    val response = client.post("auth/refresh") {
                        setBody(RefreshRequest(refreshToken))
                    }.body<TokenResponse>()

                    tokenProvider.saveTokens(response.accessToken, response.refreshToken)

                    BearerTokens(response.accessToken, response.refreshToken)
                } else {
                    null
                }
            }
        }
    }
}
```

### Token Provider Interface

```kotlin
interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
}
```

---

## Testing

### Mock Engine

```kotlin
// commonTest
class BookApiTest {

    private fun createMockClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Test
    fun testGetBooks() = runTest {
        val mockBooks = listOf(
            BookDto("1", "Book 1", null, "author1", null, 0, 0)
        )

        val client = createMockClient { request ->
            when (request.url.encodedPath) {
                "/books" -> respond(
                    content = Json.encodeToString(mockBooks),
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        val api = BookApi(client)
        val books = api.getBooks()

        assertEquals(1, books.size)
        assertEquals("Book 1", books[0].title)
    }
}
```

---

## Best Practices

| Area | Recommendation |
|------|----------------|
| **Timeouts** | Always configure appropriate timeouts |
| **Serialization** | Use `ignoreUnknownKeys = true` for API evolution |
| **Error handling** | Map HTTP errors to domain exceptions |
| **Logging** | Use LogLevel.HEADERS in debug, NONE in release |
| **DTOs** | Keep DTOs separate from domain models |
| **Testing** | Use MockEngine for unit tests |
| **Retry** | Implement retry for transient failures |
