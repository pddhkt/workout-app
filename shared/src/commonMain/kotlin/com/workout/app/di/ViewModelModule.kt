package com.workout.app.di

import com.workout.app.presentation.detail.ExerciseDetailViewModel
import com.workout.app.presentation.home.HomeViewModel
import com.workout.app.presentation.library.ExerciseLibraryViewModel
import com.workout.app.presentation.onboarding.OnboardingViewModel
import com.workout.app.presentation.planning.SessionPlanningViewModel
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
            sessionRepository = get()
        )
    }

    // Workout ViewModel - requires sessionId parameter
    factory { (sessionId: String) ->
        WorkoutViewModel(
            sessionId = sessionId,
            sessionRepository = get()
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
            exerciseRepository = get()
        )
    }

    // Onboarding ViewModel
    factory {
        OnboardingViewModel()
    }
}
