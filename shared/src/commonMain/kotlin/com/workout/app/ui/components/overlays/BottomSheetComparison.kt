package com.workout.app.ui.components.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workout.app.ui.theme.AppTheme

/**
 * Comparison screen to test Custom BottomSheet vs M3 ModalBottomSheet.
 *
 * Use this screen to compare:
 * - Scrim appearance in light/dark mode
 * - Drag-to-dismiss behavior
 * - Tap-scrim-to-dismiss
 * - Animation smoothness
 * - Visual styling differences
 */
@Composable
fun BottomSheetComparisonScreen() {
    var showCustomSheet by remember { mutableStateOf(false) }
    var showM3Sheet by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BottomSheet Comparison",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Test both implementations to compare scrim behavior, animations, and drag gestures.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showCustomSheet = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Custom BottomSheet")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showM3Sheet = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open M3 ModalBottomSheet")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Key differences:\n• Custom: 300ms tween animation\n• M3: Native spring animation\n• Custom: 30% drag threshold\n• M3: Built-in predictive back",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }

        // Custom BottomSheet
        BottomSheet(
            visible = showCustomSheet,
            onDismiss = { showCustomSheet = false }
        ) {
            BottomSheetContent(title = "Custom BottomSheet")
        }

        // M3 ModalBottomSheet
        M3BottomSheet(
            visible = showM3Sheet,
            onDismiss = { showM3Sheet = false }
        ) {
            BottomSheetContent(title = "M3 ModalBottomSheet")
        }
    }
}

@Composable
private fun BottomSheetContent(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This is sample content for comparison testing.\n\nTry the following:\n• Drag down to dismiss\n• Tap the scrim to dismiss\n• Observe the animation smoothness\n• Test in both light and dark mode",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Scrim should be dark (black with 50% alpha) in both themes.",
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.primaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
