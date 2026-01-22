package com.workout.app.di

import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.ExerciseRepositoryImpl
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.SessionRepositoryImpl
import com.workout.app.data.repository.SettingsRepository
import com.workout.app.data.repository.SettingsRepositoryImpl
import com.workout.app.data.repository.TemplateRepository
import com.workout.app.data.repository.TemplateRepositoryImpl
import com.workout.app.data.repository.WorkoutRepository
import com.workout.app.data.repository.WorkoutRepositoryImpl
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations.
 */
val dataModule = module {
    // Repositories
    single<ExerciseRepository> {
        ExerciseRepositoryImpl(
            exerciseQueries = get()
        )
    }

    single<WorkoutRepository> {
        WorkoutRepositoryImpl(
            workoutQueries = get()
        )
    }

    single<SessionRepository> {
        SessionRepositoryImpl(
            sessionQueries = get()
        )
    }

    single<TemplateRepository> {
        TemplateRepositoryImpl(
            templateQueries = get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsQueries = get()
        )
    }
}
