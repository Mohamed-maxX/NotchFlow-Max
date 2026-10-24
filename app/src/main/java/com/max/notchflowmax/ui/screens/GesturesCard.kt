package com.max.notchflowmax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.max.notchflowmax.R
import com.max.notchflowmax.data.GestureAction
import com.max.notchflowmax.data.GestureType
import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.ui.components.ActionSelectorDropdown
import com.max.notchflowmax.ui.components.GlowCard
import com.max.notchflowmax.ui.theme.BackgroundDark
import com.max.notchflowmax.ui.theme.CardBorder
import com.max.notchflowmax.ui.theme.ElectricBlue
import com.max.notchflowmax.ui.theme.PrimaryBlue
import com.max.notchflowmax.ui.theme.SurfaceVariantDark
import com.max.notchflowmax.ui.theme.TextPrimary
import com.max.notchflowmax.ui.theme.TextSecondary
import com.max.notchflowmax.ui.theme.TextTertiary

@Composable
fun GesturesCard(
    config: NotchConfig,
    onGestureChange: (GestureType, GestureAction) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onAutoPauseToggle: (Boolean) -> Unit
) {
    GlowCard(
        title = stringResource(id = R.string.gestures_title),
        subtitle = stringResource(id = R.string.gestures_desc),
        icon = Icons.Default.Gesture
    ) {
        Column {
            GestureType.entries.forEach { gesture ->
                val currentAction = config.gestures[gesture] ?: GestureAction.NONE
                ActionSelectorDropdown(
                    gesture = gesture,
                    selectedAction = currentAction,
                    onActionSelected = { newAction ->
                        onGestureChange(gesture, newAction)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Smart UX Section
            Text(
                text = stringResource(id = R.string.smart_ux_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Haptic Feedback Row
            SmartToggleRow(
                title = stringResource(id = R.string.haptic_feedback_title),
                description = stringResource(id = R.string.haptic_feedback_desc),
                icon = Icons.Default.Vibration,
                checked = config.hapticFeedback,
                onCheckedChange = onHapticsToggle
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Auto-pause in landscape Row
            SmartToggleRow(
                title = stringResource(id = R.string.auto_pause_landscape_title),
                description = stringResource(id = R.string.auto_pause_landscape_desc),
                icon = Icons.Default.ScreenRotation,
                checked = config.autoPauseLandscape,
                onCheckedChange = onAutoPauseToggle
            )
        }
    }
}

@Composable
private fun SmartToggleRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SurfaceVariantDark.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = if (checked) PrimaryBlue.copy(alpha = 0.18f) else TextTertiary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) ElectricBlue else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BackgroundDark,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = SurfaceVariantDark
            )
        )
    }
}
