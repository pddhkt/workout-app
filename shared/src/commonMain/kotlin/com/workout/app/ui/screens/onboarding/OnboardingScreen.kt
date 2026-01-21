package com.workout.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.PrimaryButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.components.buttons.ToggleButton
import com.workout.app.ui.components.cards.BaseCard
import com.workout.app.ui.components.chips.ProgressDots
import com.workout.app.ui.components.inputs.AppTextField
import com.workout.app.ui.theme.AppTheme

/**
 * Data class representing available fitness goals
 */
data class FitnessGoal(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
)

/**
 * Unit preference for weight tracking
 */
enum class WeightUnit {
    KILOGRAMS,
    POUNDS
}

/**
 * Data class holding all onboarding form data
 */
data class OnboardingData(
    val name: String = "",
    val selectedGoals: Set<String> = emptySet(),
    val weightUnit: WeightUnit = WeightUnit.KILOGRAMS
)

/**
 * Onboarding Flow Screen - Multi-step flow for new users
 * Based on mockup screen AN-17 with elements EL-95 through EL-102
 *
 * Features:
 * - Multi-step progression with progress dots (EL-95)
 * - Welcome hero card with illustration (EL-96)
 * - Name input with text field (EL-97)
 * - Goal selection with selectable cards (EL-98, EL-99, EL-100)
 * - Unit preference toggle for kg/lbs (EL-101)
 * - Primary CTA button for navigation (EL-102)
 * - Skip option on each step
 * - State management for step navigation and form data
 *
 * @param onComplete Callback invoked when onboarding is completed with user data
 * @param onSkip Callback invoked when user skips onboarding
 * @param modifier Optional modifier for customization
 */
@Composable
fun OnboardingScreen(
    onComplete: (OnboardingData) -> Unit = {},
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var onboardingData by remember { mutableStateOf(OnboardingData()) }
    val totalSteps = 4

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Skip button in top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppTheme.spacing.xl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress dots (EL-95)
            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            ProgressDots(
                total = totalSteps,
                current = currentStep,
                modifier = Modifier.padding(bottom = AppTheme.spacing.xxl)
            )

            // Animated step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        // Moving forward
                        (slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        )
                    } else {
                        // Moving backward
                        (slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        )
                    }
                },
                label = "onboarding_step_transition"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(
                        onNext = { currentStep = 1 }
                    )
                    1 -> NameInputStep(
                        name = onboardingData.name,
                        onNameChange = { onboardingData = onboardingData.copy(name = it) },
                        onNext = { currentStep = 2 },
                        onBack = { currentStep = 0 }
                    )
                    2 -> GoalSelectionStep(
                        selectedGoals = onboardingData.selectedGoals,
                        onGoalToggle = { goalId ->
                            onboardingData = onboardingData.copy(
                                selectedGoals = if (goalId in onboardingData.selectedGoals) {
                                    onboardingData.selectedGoals - goalId
                                } else {
                                    onboardingData.selectedGoals + goalId
                                }
                            )
                        },
                        onNext = { currentStep = 3 },
                        onBack = { currentStep = 1 }
                    )
                    3 -> UnitPreferenceStep(
                        selectedUnit = onboardingData.weightUnit,
                        onUnitChange = { onboardingData = onboardingData.copy(weightUnit = it) },
                        onComplete = { onComplete(onboardingData) },
                        onBack = { currentStep = 2 }
                    )
                }
            }
        }
    }
}

/**
 * Step 1: Welcome Hero Card (EL-96)
 * Introduction screen with hero illustration and welcome message
 */
@Composable
private fun WelcomeStep(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero illustration (placeholder - in production use actual asset)
        Text(
            text = "💪",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 120.dp.value.toInt().let { MaterialTheme.typography.displayLarge.fontSize * (it / 32f) },
            modifier = Modifier.padding(vertical = AppTheme.spacing.xxl)
        )

        // Title
        Text(
            text = "Welcome to Workout App",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Subtitle
        Text(
            text = "Let's personalize your fitness journey in just a few steps",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Primary CTA (EL-102)
        PrimaryButton(
            text = "Get Started",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Step 2: Name Input (EL-97)
 * Text field for user to enter their name
 */
@Composable
private fun NameInputStep(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step title
        Text(
            text = "What's your name?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Step description
        Text(
            text = "We'll use this to personalize your experience",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Name input field (EL-97)
        AppTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Enter your name",
            label = "Name",
            leadingIcon = Icons.Default.Person,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Continue",
                onClick = onNext,
                enabled = name.isNotBlank(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Step 3: Goal Selection (EL-98, EL-99, EL-100)
 * Multiple selectable goal cards for user preferences
 */
@Composable
private fun GoalSelectionStep(
    selectedGoals: Set<String>,
    onGoalToggle: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goals = remember { getAvailableGoals() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step title
        Text(
            text = "What are your goals?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Step description
        Text(
            text = "Select all that apply to customize your recommendations",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Goal cards (EL-98, EL-99, EL-100)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    isSelected = goal.id in selectedGoals,
                    onClick = { onGoalToggle(goal.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Continue",
                onClick = onNext,
                enabled = selectedGoals.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual goal card component (EL-98, EL-99, EL-100)
 * Selectable card showing goal with emoji, title, and description
 */
@Composable
private fun GoalCard(
    goal: FitnessGoal,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        contentPadding = AppTheme.spacing.lg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // Emoji icon
            Text(
                text = goal.emoji,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 40.dp.value.toInt().let { MaterialTheme.typography.headlineMedium.fontSize * (it / 20f) }
            )

            // Goal info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Person, // In production use checkmark icon
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Step 4: Unit Preference (EL-101)
 * Toggle for weight unit preference (kg vs lbs)
 */
@Composable
private fun UnitPreferenceStep(
    selectedUnit: WeightUnit,
    onUnitChange: (WeightUnit) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step title
        Text(
            text = "Choose your unit",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        // Step description
        Text(
            text = "Select your preferred unit for weight tracking",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))

        // Unit toggle (EL-101)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            ToggleButton(
                text = "Kilograms (kg)",
                selected = selectedUnit == WeightUnit.KILOGRAMS,
                onClick = { onUnitChange(WeightUnit.KILOGRAMS) },
                modifier = Modifier.weight(1f)
            )
            ToggleButton(
                text = "Pounds (lbs)",
                selected = selectedUnit == WeightUnit.POUNDS,
                onClick = { onUnitChange(WeightUnit.POUNDS) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Complete",
                onClick = onComplete,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Returns list of available fitness goals
 */
private fun getAvailableGoals(): List<FitnessGoal> {
    return listOf(
        FitnessGoal(
            id = "build_muscle",
            title = "Build Muscle",
            description = "Gain strength and increase muscle mass",
            emoji = "💪"
        ),
        FitnessGoal(
            id = "lose_weight",
            title = "Lose Weight",
            description = "Reduce body fat and improve body composition",
            emoji = "🔥"
        ),
        FitnessGoal(
            id = "improve_endurance",
            title = "Improve Endurance",
            description = "Build stamina and cardiovascular fitness",
            emoji = "🏃"
        ),
        FitnessGoal(
            id = "stay_active",
            title = "Stay Active",
            description = "Maintain fitness and healthy lifestyle",
            emoji = "🎯"
        ),
        FitnessGoal(
            id = "increase_flexibility",
            title = "Increase Flexibility",
            description = "Improve range of motion and mobility",
            emoji = "🧘"
        ),
        FitnessGoal(
            id = "sport_performance",
            title = "Sport Performance",
            description = "Enhance athletic performance and skills",
            emoji = "⚡"
        )
    )
}
