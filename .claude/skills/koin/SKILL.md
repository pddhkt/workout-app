---
name: koin
description: Koin dependency injection patterns for KMP. Use when setting up DI modules, injecting dependencies, or managing scopes.
---

# Koin Dependency Injection Skill

## Contents

- [Overview](#overview)
- [Module Definitions](#module-definitions)
- [Injection Patterns](#injection-patterns)
- [Scopes](#scopes)
- [ViewModel Injection](#viewmodel-injection)
- [Testing](#testing)

---

## Overview

Koin is a pragmatic DI framework for Kotlin:
- No code generation
- DSL-based configuration
- Multiplatform support
- Lazy by default

---

## Module Definitions

### Basic Module

```kotlin
// commonMain/di/AppModule.kt
import org.koin.dsl.module

val appModule = module {
    // Singleton - one instance for entire app
    single { createHttpClient() }
    single { AppDatabase(get()) }

    // Factory - new instance each time
    factory { UserMapper() }

    // Bind interface to implementation
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
}
```

### Domain Module

```kotlin
// commonMain/di/DomainModule.kt
val domainModule = module {
    // Use cases
    factory { GetUserUseCase(get()) }
    factory { GetBooksUseCase(get()) }
    factory { CreateBookUseCase(get()) }
    factory { SyncBooksUseCase(get(), get()) }
}
```

### Data Module

```kotlin
// commonMain/di/DataModule.kt
val dataModule = module {
    // API clients
    single { UserApi(get()) }
    single { BookApi(get()) }

    // Local data sources
    single { UserLocalDataSource(get<AppDatabase>().userQueries) }
    single { BookLocalDataSource(get<AppDatabase>().bookQueries) }

    // Remote data sources
    single { UserRemoteDataSource(get()) }
    single { BookRemoteDataSource(get()) }

    // Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<BookRepository> { BookRepositoryImpl(get(), get()) }
}
```

### Database Module

```kotlin
// commonMain/di/DatabaseModule.kt
val databaseModule = module {
    // Platform-specific driver provided by expect/actual
    single { createDriver(getOrNull()) }

    // Database
    single {
        AppDatabase(
            driver = get(),
            BookAdapter = Book.Adapter(
                createdAtAdapter = instantAdapter,
                updatedAtAdapter = instantAdapter
            )
        )
    }

    // Queries (optional - can also get from database)
    single { get<AppDatabase>().userQueries }
    single { get<AppDatabase>().bookQueries }
}
```

### All Modules Combined

```kotlin
// commonMain/di/Modules.kt
val sharedModules = listOf(
    databaseModule,
    dataModule,
    domainModule,
    appModule
)
```

---

## Injection Patterns

### Constructor Injection (Recommended)

```kotlin
class UserRepositoryImpl(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {
    // Implementation
}

// In module
single<UserRepository> { UserRepositoryImpl(get(), get()) }
```

### Named Injection

```kotlin
// Define with names
single(named("prod")) { createProductionClient() }
single(named("staging")) { createStagingClient() }

// Inject by name
single { UserApi(get(named("prod"))) }
```

### Parametrized Injection

```kotlin
// Factory with parameter
factory { (userId: String) -> UserProfileUseCase(get(), userId) }

// Use with parameter
val useCase: UserProfileUseCase = get { parametersOf(userId) }
```

### Lazy Injection

```kotlin
class SomeClass(private val scope: Scope) {
    // Lazy injection
    private val userRepository: UserRepository by scope.inject()

    // Or with KoinComponent
    class SomeComponent : KoinComponent {
        private val userRepository: UserRepository by inject()
    }
}
```

---

## Scopes

### Session Scope

```kotlin
// Define scope
val sessionModule = module {
    scope<UserSession> {
        scoped { UserPreferences(get()) }
        scoped { NotificationManager(get()) }
    }
}

// Create scope
val session = getKoin().createScope<UserSession>()

// Use
val prefs: UserPreferences = session.get()

// Close when done
session.close()
```

### Feature Scope

```kotlin
// Define feature scope
val bookFeatureModule = module {
    scope(named("bookFeature")) {
        scoped { BookSearchViewModel(get()) }
        scoped { BookFilterState() }
    }
}

// In feature
class BookFeature : KoinScopeComponent {
    override val scope = createScope(this)

    private val viewModel: BookSearchViewModel by scope.inject()

    fun cleanup() {
        scope.close()
    }
}
```

---

## ViewModel Injection

### Android ViewModel

```kotlin
// androidMain/di/ViewModelModule.kt
val viewModelModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { (bookId: String) -> BookDetailViewModel(bookId, get()) }
}

// In Composable
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = koinViewModel()
    // ...
}

@Composable
fun BookDetailScreen(bookId: String) {
    val viewModel: BookDetailViewModel = koinViewModel { parametersOf(bookId) }
    // ...
}
```

### KMP ViewModel Pattern

```kotlin
// commonMain - define ViewModel base
expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}

// commonMain - ViewModel implementation
class BookListViewModel(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BookListState>(BookListState.Loading)
    val state: StateFlow<BookListState> = _state.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _state.value = BookListState.Loading
            getBooksUseCase()
                .onSuccess { books ->
                    _state.value = BookListState.Success(books)
                }
                .onError { error ->
                    _state.value = BookListState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
```

### iOS ViewModel Wrapper

```swift
// iosApp - Observable wrapper
class BookListViewModelWrapper: ObservableObject {
    let viewModel: BookListViewModel

    @Published var state: BookListState = BookListState.Loading()

    private var stateWatcher: Closeable?

    init() {
        viewModel = KoinHelper.shared.getBookListViewModel()

        stateWatcher = viewModel.state.watch { [weak self] state in
            self?.state = state
        }
    }

    deinit {
        stateWatcher?.close()
    }
}
```

---

## Testing

### Test Module

```kotlin
// commonTest
class BookRepositoryTest : KoinTest {

    private val mockApi = mockk<BookApi>()
    private val mockDao = mockk<BookLocalDataSource>()

    private val testModule = module {
        single<BookApi> { mockApi }
        single<BookLocalDataSource> { mockDao }
        single<BookRepository> { BookRepositoryImpl(get(), get()) }
    }

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    fun testGetBooks() = runTest {
        val repository: BookRepository by inject()

        coEvery { mockDao.getAll() } returns flowOf(emptyList())

        val result = repository.getBooks().first()
        assertTrue(result.isEmpty())
    }
}
```

### Check Module Configuration

```kotlin
@Test
fun checkModules() {
    koinApplication {
        modules(sharedModules)
    }.checkModules()
}
```

---

## Best Practices

| Area | Recommendation |
|------|----------------|
| **Single** | Use for stateless services, repositories |
| **Factory** | Use for use cases, mappers, stateful objects |
| **Scoped** | Use for user session, feature-specific |
| **Naming** | Name modules clearly by layer/feature |
| **Testing** | Override modules in tests, don't mock Koin |
| **Lazy** | Use `by inject()` for optional/late dependencies |
| **Organization** | One module file per layer or feature |

### App Initialization

```kotlin
// commonMain
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(sharedModules)
    }
}

// androidMain
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MyApplication)
            modules(viewModelModule)
        }
    }
}

// iosMain
fun initKoinIOS() {
    initKoin()
}
```
