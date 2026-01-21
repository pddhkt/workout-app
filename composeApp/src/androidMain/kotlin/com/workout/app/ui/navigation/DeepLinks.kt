package com.workout.app.ui.navigation

import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest

/**
 * Deep link configuration for the Workout App.
 * Defines URI patterns for external navigation and app shortcuts.
 *
 * Deep link structure support for FT-020:
 * - workoutapp://home
 * - workoutapp://exercise/{exerciseId}
 * - workoutapp://workout?sessionId={sessionId}
 * - workoutapp://library
 * - workoutapp://onboarding
 *
 * Usage in AndroidManifest.xml:
 * ```xml
 * <intent-filter>
 *     <action android:name="android.intent.action.VIEW" />
 *     <category android:name="android.intent.category.DEFAULT" />
 *     <category android:name="android.intent.category.BROWSABLE" />
 *     <data android:scheme="workoutapp" />
 * </intent-filter>
 * ```
 */
object DeepLinks {

    const val SCHEME = "workoutapp"
    const val HOST = "app"

    /**
     * Deep link patterns for each route
     */
    object Pattern {
        const val HOME = "$SCHEME://$HOST/home"
        const val ONBOARDING = "$SCHEME://$HOST/onboarding"
        const val SESSION_PLANNING = "$SCHEME://$HOST/planning"
        const val WORKOUT = "$SCHEME://$HOST/workout"
        const val WORKOUT_WITH_SESSION = "$SCHEME://$HOST/workout/{sessionId}"
        const val EXERCISE_LIBRARY = "$SCHEME://$HOST/library"
        const val EXERCISE_DETAIL = "$SCHEME://$HOST/exercise/{exerciseId}"
        const val WORKOUT_COMPLETE = "$SCHEME://$HOST/complete/{sessionId}"
    }

    /**
     * Create deep link URI for home screen
     */
    fun createHomeDeepLink(): Uri {
        return Pattern.HOME.toUri()
    }

    /**
     * Create deep link URI for exercise detail
     */
    fun createExerciseDetailDeepLink(exerciseId: String): Uri {
        return "$SCHEME://$HOST/exercise/$exerciseId".toUri()
    }

    /**
     * Create deep link URI for workout session
     */
    fun createWorkoutDeepLink(sessionId: String? = null): Uri {
        return if (sessionId != null) {
            "$SCHEME://$HOST/workout/$sessionId".toUri()
        } else {
            "$SCHEME://$HOST/workout".toUri()
        }
    }

    /**
     * Create deep link URI for exercise library
     */
    fun createLibraryDeepLink(): Uri {
        return Pattern.EXERCISE_LIBRARY.toUri()
    }

    /**
     * Create deep link URI for workout complete
     */
    fun createWorkoutCompleteDeepLink(sessionId: String): Uri {
        return "$SCHEME://$HOST/complete/$sessionId".toUri()
    }

    /**
     * Create deep link request from URI
     */
    fun createDeepLinkRequest(uri: Uri): NavDeepLinkRequest {
        return NavDeepLinkRequest.Builder
            .fromUri(uri)
            .build()
    }

    /**
     * Parse deep link to extract route and arguments
     */
    fun parseDeepLink(uri: Uri): Pair<String, Map<String, String>>? {
        if (uri.scheme != SCHEME || uri.host != HOST) {
            return null
        }

        val pathSegments = uri.pathSegments
        if (pathSegments.isEmpty()) {
            return null
        }

        return when (pathSegments[0]) {
            "home" -> Route.Home.route to emptyMap()
            "onboarding" -> Route.Onboarding.route to emptyMap()
            "planning" -> Route.SessionPlanning.route to emptyMap()
            "library" -> Route.ExerciseLibrary.route to emptyMap()

            "workout" -> {
                val sessionId = pathSegments.getOrNull(1)
                if (sessionId != null) {
                    Route.Workout.ROUTE to mapOf(Route.Workout.ARG_SESSION_ID to sessionId)
                } else {
                    Route.Workout.ROUTE to emptyMap()
                }
            }

            "exercise" -> {
                val exerciseId = pathSegments.getOrNull(1) ?: return null
                Route.ExerciseDetail.ROUTE to mapOf(Route.ExerciseDetail.ARG_EXERCISE_ID to exerciseId)
            }

            "complete" -> {
                val sessionId = pathSegments.getOrNull(1) ?: return null
                Route.WorkoutComplete.ROUTE to mapOf(Route.WorkoutComplete.ARG_SESSION_ID to sessionId)
            }

            else -> null
        }
    }
}

/**
 * App shortcuts for launcher integration.
 * These can be registered in shortcuts.xml for quick access.
 *
 * Example shortcuts.xml:
 * ```xml
 * <shortcuts>
 *     <shortcut
 *         android:shortcutId="start_workout"
 *         android:enabled="true"
 *         android:icon="@drawable/ic_workout"
 *         android:shortcutShortLabel="@string/shortcut_start_workout"
 *         android:shortcutLongLabel="@string/shortcut_start_workout_long">
 *         <intent
 *             android:action="android.intent.action.VIEW"
 *             android:data="workoutapp://app/planning" />
 *     </shortcut>
 *     <shortcut
 *         android:shortcutId="exercise_library"
 *         android:enabled="true"
 *         android:icon="@drawable/ic_library"
 *         android:shortcutShortLabel="@string/shortcut_library">
 *         <intent
 *             android:action="android.intent.action.VIEW"
 *             android:data="workoutapp://app/library" />
 *     </shortcut>
 * </shortcuts>
 * ```
 */
object AppShortcuts {
    const val START_WORKOUT = "start_workout"
    const val EXERCISE_LIBRARY = "exercise_library"
    const val HOME = "home"
}
