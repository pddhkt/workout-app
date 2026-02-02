package com.workout.app.di

import com.workout.app.data.DatabaseSeeder
import com.workout.app.data.repository.ExerciseRepository
import com.workout.app.data.repository.ExerciseRepositoryImpl
import com.workout.app.data.repository.SessionExerciseRepository
import com.workout.app.data.repository.SessionExerciseRepositoryImpl
import com.workout.app.data.repository.SessionRepository
import com.workout.app.data.repository.SessionRepositoryImpl
import com.workout.app.data.repository.SetRepository
import com.workout.app.data.repository.SetRepositoryImpl
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
    // Database Seeder
    single {
        DatabaseSeeder(
            workoutQueries = get(),
            sessionQueries = get(),
            sessionExerciseQueries = get(),
            setQueries = get(),
            exerciseQueries = get()
        )
    }

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
            sessionQueries = get(),
            sessionExerciseQueries = get(),
            setQueries = get()
        )
    }

    single<SessionExerciseRepository> {
        SessionExerciseRepositoryImpl(
            sessionExerciseQueries = get()
        )
    }

    single<SetRepository> {
        SetRepositoryImpl(
            setQueries = get()
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
