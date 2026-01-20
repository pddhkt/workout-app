# Workout App - Code Inventory

Last Updated: 2026-01-20

## Project Structure

### Shared Module (KMP)
Pure Kotlin code that runs on all platforms (Android, iOS, JVM, JS).

#### Design System
**Location:** `shared/src/commonMain/kotlin/com/workout/app/ui/theme/`

**Color.kt**
- Primary colors: Green accent (#13EC5B) matching mockup design
- Background: Dark theme (#0A0A0A, #111111, #1A1A1A)
- Status colors: Success, Warning, Error, Info
- Exercise states: Completed, Active, Pending
- Tag colors: 9 semantic colors from mockup elements.json

**Type.kt**
- Typography system using Material3 Typography
- Heading styles: Large (28sp), Medium (24sp), Small (20sp)
- Body styles: Large (16sp), Medium (14sp), Small (12sp)
- Label styles: Large (14sp), Medium (12sp), Small (10sp)
- Font weights: Bold for headlines, SemiBold for labels, Normal for body

**Spacing.kt**
- Spacing scale using CompositionLocal
- Values: xs=4dp, sm=8dp, md=12dp, lg=16dp, xl=24dp, xxl=32dp
- Accessible via `AppTheme.spacing`

**Theme.kt**
- WorkoutAppTheme composable
- Dark color scheme as default
- Material3 integration
- AppTheme object for accessing spacing

#### Domain Layer
*Not yet implemented*

#### Data Layer
*Not yet implemented*

#### DI (Koin)
*Not yet implemented*

### ComposeApp Module (Android)
Android-specific application code.

**MainActivity.kt**
- Entry point for Android app
- Sets up WorkoutAppTheme
- Displays placeholder "Workout App" text with primary color
- Uses edge-to-edge display

**App.kt**
- Application class
- Initializes Koin DI
- Currently has empty modules list (to be populated)

### Build Configuration

**gradle/libs.versions.toml**
- Kotlin 2.0.0
- Compose Multiplatform 1.6.10
- Koin 3.5.3
- Coroutines 1.8.0
- AGP 8.2.2
- Ktor 2.3.9 (not yet used)
- SQLDelight 2.0.1 (not yet used)

**shared/build.gradle.kts**
- Android target (minSdk 24, compileSdk 34)
- iOS targets: x64, arm64, simulatorArm64
- Framework: Static library named "Shared"
- Dependencies: Compose Multiplatform, Koin, Coroutines

**composeApp/build.gradle.kts**
- Android application
- Depends on :shared module
- Version: 1.0.0 (versionCode 1)
- ProGuard enabled for release builds

## Patterns in Use

### KMP Patterns
- commonMain for shared code
- androidMain for Android-specific implementations
- Source set hierarchy: commonMain -> androidMain/iosMain

### Design System Patterns
- Material3 ColorScheme for theming
- CompositionLocal for spacing system
- Static object for accessing theme values

### Dependency Injection
- Koin initialization in Application class
- Lazy module loading approach
- Android-specific context injection

## Next Steps (Not Implemented)

1. Domain models and use cases
2. Repository interfaces
3. Data layer with expect/actual
4. Koin modules for DI
5. Navigation setup
6. Feature screens
7. Unit tests in commonTest
8. Platform-specific tests

## Key Decisions

1. **Dark theme only**: Mockups show dark UI, no light theme planned
2. **Material3**: Using official Material Design 3 for components
3. **Pure Kotlin commonMain**: No platform-specific code in shared
4. **Koin for DI**: Chosen over manual DI for KMP support
5. **Static framework**: iOS framework is static library for simplicity
