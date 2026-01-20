---
name: android
description: Android development patterns with Jetpack Compose. Use when implementing Android UI, ViewModels, or platform-specific features.
---

# Android Development Skill

## Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Compose Patterns](#compose-patterns)
- [ViewModel Patterns](#viewmodel-patterns)
- [Navigation](#navigation)
- [State Management](#state-management)

---

## Overview

Android app structure for KMP projects:
- Jetpack Compose for UI
- ViewModel for presentation logic
- Koin for dependency injection
- Integration with shared KMP module

---

## Project Structure

```
composeApp/src/androidMain/kotlin/com/example/
├── MainActivity.kt
├── App.kt                        # Application class
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt      # Nav host and routes
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── components/               # Reusable composables
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorMessage.kt
│   │   └── EmptyState.kt
│   └── screen/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewModel.kt
│       ├── detail/
│       │   ├── DetailScreen.kt
│       │   └── DetailViewModel.kt
│       └── settings/
│           └── SettingsScreen.kt
└── di/
    └── AndroidModule.kt          # Android-specific DI
```

---

## Compose Patterns

### Screen Template

```kotlin
@Composable
fun BookListScreen(
    viewModel: BookListViewModel = koinViewModel(),
    onBookClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BookListContent(
        state = state,
        onBookClick = onBookClick,
        onRetry = viewModel::loadBooks
    )
}

@Composable
private fun BookListContent(
    state: BookListState,
    onBookClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is BookListState.Loading -> LoadingIndicator()
        is BookListState.Error -> ErrorMessage(
            message = state.message,
            onRetry = onRetry
        )
        is BookListState.Success -> BookList(
            books = state.books,
            onBookClick = onBookClick
        )
    }
}

@Composable
private fun BookList(
    books: List<Book>,
    onBookClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookItem(
                book = book,
                onClick = { onBookClick(book.id) }
            )
        }
    }
}
```

### Stateless Component

```kotlin
@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun BookItemPreview() {
    AppTheme {
        BookItem(
            book = Book(
                id = "1",
                title = "Sample Book",
                author = "Author Name"
            ),
            onClick = {}
        )
    }
}
```

### Form Screen

```kotlin
@Composable
fun CreateBookScreen(
    viewModel: CreateBookViewModel = koinViewModel(),
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
        }
    }

    CreateBookContent(
        state = state,
        onTitleChange = viewModel::updateTitle,
        onDescriptionChange = viewModel::updateDescription,
        onSubmit = viewModel::submit
    )
}

@Composable
private fun CreateBookContent(
    state: CreateBookState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            isError = state.titleError != null,
            supportingText = state.titleError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            enabled = state.isValid && !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Book")
            }
        }
    }
}
```

---

## ViewModel Patterns

### Standard ViewModel

```kotlin
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

sealed class BookListState {
    data object Loading : BookListState()
    data class Success(val books: List<Book>) : BookListState()
    data class Error(val message: String) : BookListState()
}
```

### Form ViewModel

```kotlin
class CreateBookViewModel(
    private val createBookUseCase: CreateBookUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateBookState())
    val state: StateFlow<CreateBookState> = _state.asStateFlow()

    fun updateTitle(title: String) {
        _state.update {
            it.copy(
                title = title,
                titleError = validateTitle(title)
            )
        }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun submit() {
        val currentState = _state.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            createBookUseCase(currentState.title, currentState.description)
                .onSuccess {
                    _state.update { it.copy(isSuccess = true, isLoading = false) }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    private fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "Title is required"
            title.length < 3 -> "Title must be at least 3 characters"
            else -> null
        }
    }
}

data class CreateBookState(
    val title: String = "",
    val description: String = "",
    val titleError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && titleError == null
}
```

---

## Navigation

### Navigation Setup

```kotlin
// AppNavigation.kt
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onBookClick = { bookId ->
                    navController.navigate("book/$bookId")
                },
                onCreateClick = {
                    navController.navigate("create")
                }
            )
        }

        composable(
            route = "book/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            BookDetailScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("create") {
            CreateBookScreen(
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}
```

### Type-Safe Navigation (Recommended)

```kotlin
// Routes.kt
sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Create : Route("create")
    data class BookDetail(val bookId: String) : Route("book/$bookId") {
        companion object {
            const val ROUTE = "book/{bookId}"
            const val ARG_BOOK_ID = "bookId"
        }
    }
}

// Extension functions
fun NavController.navigateToBookDetail(bookId: String) {
    navigate(Route.BookDetail(bookId).route)
}
```

---

## State Management

### Collecting State

```kotlin
// With Lifecycle awareness (recommended)
val state by viewModel.state.collectAsStateWithLifecycle()

// One-time events
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is Event.ShowError -> snackbarHostState.showSnackbar(event.message)
            is Event.NavigateBack -> navController.popBackStack()
        }
    }
}
```

### Event Channel Pattern

```kotlin
class BookDetailViewModel(
    private val bookId: String,
    private val deleteBookUseCase: DeleteBookUseCase
) : ViewModel() {

    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    fun deleteBook() {
        viewModelScope.launch {
            deleteBookUseCase(bookId)
                .onSuccess {
                    _events.send(Event.NavigateBack)
                }
                .onError { error ->
                    _events.send(Event.ShowError(error.message ?: "Delete failed"))
                }
        }
    }

    sealed class Event {
        data class ShowError(val message: String) : Event()
        data object NavigateBack : Event()
    }
}
```

---

## Best Practices

| Area | Recommendation |
|------|----------------|
| **State** | Use StateFlow for UI state, Channel for events |
| **Lifecycle** | Use `collectAsStateWithLifecycle()` |
| **Preview** | Add @Preview for all composables |
| **Separation** | Screen = state collection, Content = pure UI |
| **Testing** | Extract logic to ViewModel, test without UI |
| **Modifiers** | Accept Modifier parameter, apply last |
| **Navigation** | Use type-safe routes |
