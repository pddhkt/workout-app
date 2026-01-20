---
name: ios
description: iOS integration patterns for KMP. Use when implementing actual declarations, framework export, or SwiftUI integration.
---

# iOS Integration Skill

## Contents

- [Overview](#overview)
- [Framework Export](#framework-export)
- [Actual Implementations](#actual-implementations)
- [SwiftUI Integration](#swiftui-integration)
- [Observable Wrappers](#observable-wrappers)
- [Common Pitfalls](#common-pitfalls)

---

## Overview

iOS integration with KMP involves:
- Exporting shared module as iOS framework
- Providing `actual` implementations for `expect` declarations
- Creating Swift wrappers for Kotlin code
- Handling Flow observation in SwiftUI

---

## Framework Export

### Gradle Configuration

```kotlin
// shared/build.gradle.kts
kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true

            // Export dependencies to Swift
            export(libs.kotlinx.coroutines.core)
        }
    }
}
```

### XCFramework for Distribution

```kotlin
// For distributing to other teams
kotlin {
    val xcf = XCFramework()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }
}
```

### Swift Package Manager Integration

```kotlin
// Export as Swift package
kotlin {
    cocoapods {
        // Or use Swift Package Manager
    }
}
```

---

## Actual Implementations

### Platform Detection

```kotlin
// iosMain/kotlin/Platform.ios.kt
import platform.UIKit.UIDevice

actual fun getPlatformName(): String {
    return UIDevice.currentDevice.systemName() +
        " " + UIDevice.currentDevice.systemVersion
}
```

### Database Driver

```kotlin
// iosMain/kotlin/DatabaseDriver.ios.kt
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createDriver(context: Any?): SqlDriver {
    return NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = "app.db"
    )
}
```

### Coroutine Dispatcher

```kotlin
// iosMain/kotlin/Dispatchers.ios.kt
import kotlinx.coroutines.Dispatchers

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
```

### Secure Storage (Keychain)

```kotlin
// iosMain/kotlin/SecureStorage.ios.kt
import platform.Foundation.*
import platform.Security.*

actual class SecureStorage {
    actual fun getString(key: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecReturnData to true,
            kSecMatchLimit to kSecMatchLimitOne
        )

        memScoped {
            val result = alloc<ObjCObjectVar<Any?>>()
            val status = SecItemCopyMatching(query.toNSDict(), result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as? NSData
                return data?.toKString()
            }
            return null
        }
    }

    actual fun putString(key: String, value: String) {
        // Delete existing
        remove(key)

        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecValueData to value.toNSData()
        )

        SecItemAdd(query.toNSDict(), null)
    }

    actual fun remove(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query.toNSDict())
    }
}
```

---

## SwiftUI Integration

### App Entry Point

```swift
// iosApp/iOSApp.swift
import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Initialize Koin
        KoinHelperKt.doInitKoinIOS()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### Basic View Integration

```swift
// iosApp/Views/BookListView.swift
import SwiftUI
import Shared

struct BookListView: View {
    @StateObject private var viewModel = BookListViewModelWrapper()

    var body: some View {
        NavigationView {
            content
                .navigationTitle("Books")
        }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case is BookListState.Loading:
            ProgressView()
        case let state as BookListState.Error:
            ErrorView(message: state.message) {
                viewModel.loadBooks()
            }
        case let state as BookListState.Success:
            List(state.books, id: \.id) { book in
                BookRow(book: book)
            }
        default:
            EmptyView()
        }
    }
}

struct BookRow: View {
    let book: Book

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(book.title)
                .font(.headline)
            Text(book.authorName)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 4)
    }
}
```

---

## Observable Wrappers

### Flow Wrapper

```kotlin
// commonMain/kotlin/FlowWrapper.kt
class FlowWrapper<T>(private val flow: Flow<T>) {
    fun watch(block: (T) -> Unit): Closeable {
        val job = CoroutineScope(Dispatchers.Main).launch {
            flow.collect { value ->
                block(value)
            }
        }
        return object : Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}

// Extension for StateFlow
fun <T> StateFlow<T>.wrap(): FlowWrapper<T> = FlowWrapper(this)
```

### Swift ViewModel Wrapper

```swift
// iosApp/ViewModels/BookListViewModelWrapper.swift
import SwiftUI
import Shared

@MainActor
class BookListViewModelWrapper: ObservableObject {
    private let viewModel: BookListViewModel
    @Published private(set) var state: BookListState = BookListState.Loading()

    private var stateWatcher: Closeable?

    init() {
        viewModel = KoinHelper.shared.getBookListViewModel()

        stateWatcher = viewModel.state.watch { [weak self] state in
            DispatchQueue.main.async {
                self?.state = state
            }
        }
    }

    func loadBooks() {
        viewModel.loadBooks()
    }

    deinit {
        stateWatcher?.close()
    }
}
```

### Koin Helper

```kotlin
// iosMain/kotlin/KoinHelper.kt
object KoinHelper {
    fun getBookListViewModel(): BookListViewModel = getKoin().get()
    fun getBookDetailViewModel(bookId: String): BookDetailViewModel =
        getKoin().get { parametersOf(bookId) }
}
```

```swift
// Usage in Swift
extension KoinHelper {
    static var shared: KoinHelper { KoinHelper() }
}
```

---

## Common Pitfalls

### Main Thread Issues

```kotlin
// Wrong - may crash on iOS
class ViewModel {
    private val _state = MutableStateFlow<State>(State.Initial)
    val state: StateFlow<State> = _state
}

// Right - ensure main dispatcher
class ViewModel(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val _state = MutableStateFlow<State>(State.Initial)
    val state: StateFlow<State> = _state

    fun loadData() {
        viewModelScope.launch(mainDispatcher) {
            // State updates on main thread
            _state.value = State.Loading
        }
    }
}
```

### Memory Management

```swift
// Wrong - strong reference cycle
class ViewModelWrapper: ObservableObject {
    private var watcher: Closeable?

    init() {
        watcher = flow.watch { state in
            self.state = state  // Strong reference to self!
        }
    }
}

// Right - weak self
class ViewModelWrapper: ObservableObject {
    private var watcher: Closeable?

    init() {
        watcher = flow.watch { [weak self] state in
            self?.state = state
        }
    }

    deinit {
        watcher?.close()  // Always close!
    }
}
```

### Nullability

```kotlin
// Kotlin nullable
fun getUser(): User?

// Swift sees this as Optional
func getUser() -> User?

// Be explicit about nullability in your API
```

### Generics Erasure

```kotlin
// This won't work well in Swift
fun <T> getList(): List<T>

// Better - use specific types
fun getUsers(): List<User>
fun getBooks(): List<Book>
```

---

## Best Practices

| Area | Recommendation |
|------|----------------|
| **Framework** | Use static framework for smaller binary |
| **Dispatchers** | Use Main dispatcher for UI state |
| **Memory** | Always close Flow watchers in deinit |
| **Nullability** | Be explicit, avoid `!!` |
| **Generics** | Prefer concrete types over generics |
| **Naming** | Use Swift-friendly names (@ObjCName) |
| **Threading** | Wrap callbacks in DispatchQueue.main |
