package com.workout.app.di

import com.workout.app.presentation.complete.WorkoutCompleteViewModel
import com.workout.app.presentation.detail.ExerciseDetailViewModel
import com.workout.app.presentation.history.SessionDetailViewModel
import com.workout.app.presentation.history.SessionHistoryViewModel
import com.workout.app.presentation.home.HomeViewModel
import com.workout.app.presentation.library.ExerciseLibraryViewModel
import com.workout.app.presentation.onboarding.OnboardingViewModel
import com.workout.app.presentation.active.ActiveSessionViewModel
import com.workout.app.presentation.planning.SessionPlanningViewModel
import com.workout.app.presentation.settings.SettingsViewModel
import com.workout.app.presentation.workout.WorkoutViewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels.
 * Provides ViewModel instances with repository dependencies.
 */
val viewModelModule = module {
    // Home ViewModel
    factory {
        HomeViewModel(
            workoutRepository = get(),
            templateRepository = get(),
            sessionRepository = get()
        )
    }

    // Session Planning ViewModel
    factory {
        SessionPlanningViewModel(
            exerciseRepository = get(),
            sessionRepository = get(),
            sessionExerciseRepository = get(),
            setRepository = get()
        )
    }

    // Workout ViewModel - requires sessionId parameter
    factory { (sessionId: String) ->
        WorkoutViewModel(
            sessionId = sessionId,
            sessionRepository = get(),
            sessionExerciseRepository = get(),
            setRepository = get(),
            exerciseRepository = get()
        )
    }

    // Exercise Library ViewModel
    factory {
        ExerciseLibraryViewModel(
            exerciseRepository = get()
        )
    }

    // Exercise Detail ViewModel - requires exerciseId parameter
    factory { (exerciseId: String) ->
        ExerciseDetailViewModel(
            exerciseId = exerciseId,
            exerciseRepository = get(),
            setRepository = get()
        )
    }

    // Onboarding ViewModel
    factory {
        OnboardingViewModel()
    }

    // Settings ViewModel
    factory {
        SettingsViewModel()
    }

    // Session History ViewModel
    factory {
        SessionHistoryViewModel(
            workoutRepository = get()
        )
    }

    // Session Detail ViewModel - requires workoutId parameter
    factory { (workoutId: String) ->
        SessionDetailViewModel(
            workoutId = workoutId,
            workoutRepository = get(),
            sessionRepository = get()
        )
    }

    // Active Session ViewModel - Singleton for observing active workout sessions
    single {
        ActiveSessionViewModel(
            sessionRepository = get()
        )
    }

    // Workout Complete ViewModel - requires sessionId parameter
    factory { (sessionId: String) ->
        WorkoutCompleteViewModel(
            sessionId = sessionId,
            sessionRepository = get(),
            setRepository = get(),
            workoutRepository = get(),
            templateRepository = get()
        )
    }
}
