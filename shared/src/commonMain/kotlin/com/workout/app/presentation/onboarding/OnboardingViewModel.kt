package com.workout.app.presentation.onboarding

import com.workout.app.presentation.base.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for Onboarding flow.
 * Manages multi-step form data and validation.
 */
class OnboardingViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun toggleGoal(goalId: String) {
        val currentGoals = _state.value.selectedGoals.toMutableSet()
        if (currentGoals.contains(goalId)) {
            currentGoals.remove(goalId)
        } else {
            currentGoals.add(goalId)
        }
        _state.update { it.copy(selectedGoals = currentGoals) }
    }

    fun selectWeightUnit(unit: WeightUnit) {
        _state.update { it.copy(weightUnit = unit) }
    }

    fun nextStep() {
        val currentStep = _state.value.currentStep
        if (currentStep < OnboardingStep.UNIT_PREFERENCE) {
            val nextStep = OnboardingStep.values()[currentStep.ordinal + 1]
            _state.update { it.copy(currentStep = nextStep) }
        }
    }

    fun previousStep() {
        val currentStep = _state.value.currentStep
        if (currentStep > OnboardingStep.WELCOME) {
            val prevStep = OnboardingStep.values()[currentStep.ordinal - 1]
            _state.update { it.copy(currentStep = prevStep) }
        }
    }

    fun goToStep(step: OnboardingStep) {
        _state.update { it.copy(currentStep = step) }
    }

    suspend fun completeOnboarding(): OnboardingData {
        // TODO: Save preferences to local storage or repository
        val currentState = _state.value
        return OnboardingData(
            name = currentState.name,
            goals = currentState.selectedGoals.toList(),
            weightUnit = currentState.weightUnit
        )
    }

    fun canProceedFromCurrentStep(): Boolean {
        return when (_state.value.currentStep) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.NAME_INPUT -> _state.value.name.isNotBlank()
            OnboardingStep.GOAL_SELECTION -> _state.value.selectedGoals.isNotEmpty()
            OnboardingStep.UNIT_PREFERENCE -> true
        }
    }
}

/**
 * Onboarding steps.
 */
enum class OnboardingStep {
    WELCOME,
    NAME_INPUT,
    GOAL_SELECTION,
    UNIT_PREFERENCE
}

/**
 * Weight unit preference.
 */
enum class WeightUnit {
    KILOGRAMS,
    POUNDS
}

/**
 * Completed onboarding data.
 */
data class OnboardingData(
    val name: String,
    val goals: List<String>,
    val weightUnit: WeightUnit
)

/**
 * UI state for Onboarding flow.
 */
data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val name: String = "",
    val selectedGoals: Set<String> = emptySet(),
    val weightUnit: WeightUnit = WeightUnit.KILOGRAMS
) {
    /**
     * Total number of steps in onboarding.
     */
    val totalSteps: Int = OnboardingStep.values().size

    /**
     * Current step index (1-based).
     */
    val currentStepIndex: Int = currentStep.ordinal + 1

    /**
     * Whether currently on first step.
     */
    val isFirstStep: Boolean = currentStep == OnboardingStep.WELCOME

    /**
     * Whether currently on last step.
     */
    val isLastStep: Boolean = currentStep == OnboardingStep.UNIT_PREFERENCE

    /**
     * Whether user can proceed from current step.
     */
    val canProceed: Boolean
        get() = when (currentStep) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.NAME_INPUT -> name.isNotBlank()
            OnboardingStep.GOAL_SELECTION -> selectedGoals.isNotEmpty()
            OnboardingStep.UNIT_PREFERENCE -> true
        }
}
