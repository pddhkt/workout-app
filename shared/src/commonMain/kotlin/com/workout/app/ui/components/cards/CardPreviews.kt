package com.workout.app.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.Active
import com.workout.app.ui.theme.AppTheme
import com.workout.app.ui.theme.Completed
import com.workout.app.ui.theme.Primary
import com.workout.app.ui.theme.WorkoutAppTheme

/**
 * Preview demonstrating all card variants
 */
@Composable
fun CardPreviewsScreen() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
        ) {
            Text(
                text = "Card Components",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

            // Section: Base Card
            SectionTitle("Base Card")
            BaseCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Standard Card",
                    subtitle = "Default elevation and styling"
                )
            }

            // Section: Clickable Card
            SectionTitle("Clickable Card")
            BaseCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Handle click */ }
            ) {
                CardContent(
                    title = "Clickable Card",
                    subtitle = "Tap to interact"
                )
            }

            // Section: Elevated Card
            SectionTitle("Elevated Card")
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Elevated Card",
                    subtitle = "Higher shadow for emphasis"
                )
            }

            // Section: Flat Card
            SectionTitle("Flat Card")
            FlatCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Flat Card",
                    subtitle = "No elevation, subtle appearance"
                )
            }

            // Section: Outlined Card
            SectionTitle("Outlined Card")
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Outlined Card",
                    subtitle = "Border for selection or grouping"
                )
            }

            // Section: Selected Card
            SectionTitle("Selected Card")
            SelectedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Selected Card",
                    subtitle = "Combines border and elevation"
                )
            }

            // Section: Colored Cards
            SectionTitle("Colored Cards")
            ColoredCard(
                surfaceColor = Primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Primary Card",
                    subtitle = "For active or featured content"
                )
            }

            ColoredCard(
                surfaceColor = Completed,
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Success Card",
                    subtitle = "For completed exercises or achievements"
                )
            }

            ColoredCard(
                surfaceColor = Active,
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Active Card",
                    subtitle = "For current workout or active state"
                )
            }

            ColoredCard(
                surfaceColor = AppTheme.colors.error,
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Error Card",
                    subtitle = "For warnings or error states"
                )
            }

            // Section: Disabled State
            SectionTitle("Disabled State")
            BaseCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Won't fire */ },
                enabled = false
            ) {
                CardContent(
                    title = "Disabled Card",
                    subtitle = "Not interactive, reduced opacity"
                )
            }

            // Section: Custom Background
            SectionTitle("Custom Background")
            BaseCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CardContent(
                    title = "Custom Background",
                    subtitle = "Surface variant color"
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppTheme.spacing.md)
    )
}

@Composable
private fun CardContent(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Individual preview composables for Android Studio preview

@Composable
fun BaseCardPreview() {
    WorkoutAppTheme {
        BaseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CardContent(
                title = "Example Card",
                subtitle = "This is a preview of the base card component"
            )
        }
    }
}

@Composable
fun ClickableCardPreview() {
    WorkoutAppTheme {
        BaseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = { }
        ) {
            CardContent(
                title = "Clickable Card",
                subtitle = "Tap to interact with this card"
            )
        }
    }
}

@Composable
fun ElevatedCardPreview() {
    WorkoutAppTheme {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CardContent(
                title = "Elevated Card",
                subtitle = "Higher shadow for visual emphasis"
            )
        }
    }
}

@Composable
fun OutlinedCardPreview() {
    WorkoutAppTheme {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CardContent(
                title = "Outlined Card",
                subtitle = "With primary color border"
            )
        }
    }
}

@Composable
fun SelectedCardPreview() {
    WorkoutAppTheme {
        SelectedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CardContent(
                title = "Selected Card",
                subtitle = "Combined border and elevation effect"
            )
        }
    }
}

@Composable
fun ColoredCardPreview() {
    WorkoutAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColoredCard(
                surfaceColor = Primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Primary Card",
                    subtitle = "Active or featured content"
                )
            }

            ColoredCard(
                surfaceColor = Completed,
                modifier = Modifier.fillMaxWidth()
            ) {
                CardContent(
                    title = "Success Card",
                    subtitle = "Completed state"
                )
            }
        }
    }
}

@Composable
fun DisabledCardPreview() {
    WorkoutAppTheme {
        BaseCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = { },
            enabled = false
        ) {
            CardContent(
                title = "Disabled Card",
                subtitle = "Not interactive"
            )
        }
    }
}
