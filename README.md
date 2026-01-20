# Workout App

A Kotlin Multiplatform workout tracking application with support for Android and iOS.

## Tech Stack

- **Kotlin Multiplatform** - Share code across platforms
- **Compose Multiplatform** - Modern declarative UI
- **Material3** - Design system
- **Koin** - Dependency injection
- **Coroutines** - Async operations
- **SQLDelight** - Local database (planned)
- **Ktor** - Network client (planned)

## Project Structure

```
workout-app/
├── shared/                     # Shared KMP module
│   └── src/
│       ├── commonMain/         # Shared code
│       │   └── kotlin/
│       │       └── com/workout/app/
│       │           └── ui/theme/    # Design system
│       ├── androidMain/        # Android-specific
│       └── iosMain/           # iOS-specific
├── composeApp/                # Android app
│   └── src/androidMain/
│       └── kotlin/com/workout/app/
└── iosApp/                    # iOS app (TBD)
```

## Design System

### Colors
- **Primary**: #13EC5B (Green accent)
- **Background**: #0A0A0A (Dark)
- **Surface**: #111111, #1A1A1A (Elevated surfaces)
- **Status**: Success, Warning, Error, Info
- **States**: Completed, Active, Pending

### Typography
- Headlines: 28sp, 24sp, 20sp (Bold/SemiBold)
- Body: 16sp, 14sp, 12sp (Normal)
- Labels: 14sp, 12sp, 10sp (SemiBold)

### Spacing
- xs: 4dp, sm: 8dp, md: 12dp
- lg: 16dp, xl: 24dp, xxl: 32dp

## Getting Started

### Prerequisites

- JDK 11 or higher
- Android Studio Hedgehog (2023.1.1) or later
- For iOS development: Xcode 15+

### Build

```bash
# Build the project
./gradlew build

# Run Android app
./gradlew composeApp:installDebug

# Run tests
./gradlew test
```

### Android

1. Open project in Android Studio
2. Select `composeApp` configuration
3. Run on emulator or device

### iOS

TBD - iOS target configuration pending

## Development

### Adding Features

1. Domain models in `shared/src/commonMain/kotlin/com/workout/app/domain/`
2. Use cases in same domain layer
3. Repository interfaces in `shared/src/commonMain/kotlin/com/workout/app/data/`
4. Platform implementations in `androidMain`/`iosMain`
5. UI in `composeApp` for Android, `iosApp` for iOS

### Dependency Injection

Using Koin for DI:
```kotlin
val myModule = module {
    single<Repository> { RepositoryImpl(get()) }
    factory { UseCase(get()) }
}
```

Register in `App.kt`:
```kotlin
startKoin {
    modules(myModule)
}
```

## Architecture

Following Clean Architecture principles:
- **Domain**: Business logic, models, use cases
- **Data**: Repositories, data sources, DTOs
- **Presentation**: ViewModels, UI states
- **UI**: Composables, screens

## License

TBD

## Status

**In Development** - Project setup complete, features in progress.

### Completed
- [x] Gradle configuration
- [x] Shared module setup
- [x] Design system (colors, typography, spacing)
- [x] Android app skeleton
- [x] Koin initialization

### Planned
- [ ] Domain models
- [ ] Local database (SQLDelight)
- [ ] Network layer (Ktor)
- [ ] Workout tracking features
- [ ] Exercise library
- [ ] Progress analytics
- [ ] iOS app
