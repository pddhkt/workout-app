package com.workout.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workout.app.ui.components.buttons.AppIconButton
import com.workout.app.ui.components.buttons.SecondaryButton
import com.workout.app.ui.theme.AppTheme

/**
 * Settings screen with app preferences.
 *
 * @param onBackClick Callback when back button is clicked
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onBottomSheetComparisonClick: () -> Unit = {},
    onWorkoutLayoutExperimentClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackClick
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.lg)
        ) {
            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            // Debug Section
            SectionTitle(title = "Debug")

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            SecondaryButton(
                text = "BottomSheet Comparison",
                onClick = onBottomSheetComparisonClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

            SecondaryButton(
                text = "Workout Layout Experiment",
                onClick = onWorkoutLayoutExperimentClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
        }
    }
}

/**
 * Section title component.
 */
@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}
