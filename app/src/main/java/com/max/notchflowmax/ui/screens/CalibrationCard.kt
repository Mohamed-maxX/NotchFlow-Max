package com.max.notchflowmax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.max.notchflowmax.R
import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.ui.components.GlowCard
import com.max.notchflowmax.ui.components.SliderWithLabel
import com.max.notchflowmax.ui.theme.BackgroundDark
import com.max.notchflowmax.ui.theme.CardBorder
import com.max.notchflowmax.ui.theme.NeonGreen
import com.max.notchflowmax.ui.theme.PrimaryNeon
import com.max.notchflowmax.ui.theme.SecondaryCyan
import com.max.notchflowmax.ui.theme.SurfaceVariantDark
import com.max.notchflowmax.ui.theme.TextPrimary
import com.max.notchflowmax.ui.theme.TextSecondary
import com.max.notchflowmax.ui.theme.TextTertiary

@Composable
fun CalibrationCard(
    config: NotchConfig,
    onXChange: (Int) -> Unit,
    onYChange: (Int) -> Unit,
    onWidthChange: (Int) -> Unit,
    onHeightChange: (Int) -> Unit,
    onCornerRadiusChange: (Int) -> Unit,
    onPreviewToggle: (Boolean) -> Unit,
    onApplyPreset: (x: Int, y: Int, w: Int, h: Int, radius: Int) -> Unit,
    onResetDefaults: () -> Unit
) {
    GlowCard(
        title = stringResource(id = R.string.calibration_title),
        subtitle = stringResource(id = R.string.calibration_desc),
        icon = Icons.Default.CropFree,
        isHighlighted = config.previewActive
    ) {
        Column {
            // Preview Overlay Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (config.previewActive) NeonGreen.copy(alpha = 0.12f) else SurfaceVariantDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (config.previewActive) NeonGreen.copy(alpha = 0.2f) else TextTertiary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (config.previewActive) NeonGreen else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.preview_overlay_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (config.previewActive) NeonGreen else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.preview_overlay_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = config.previewActive,
                    onCheckedChange = onPreviewToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDark,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = SurfaceVariantDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Row
            Text(
                text = stringResource(id = R.string.preset_title),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip("Center Hole") { onApplyPreset(0, 4, 80, 80, 40) }
                PresetChip("Left Hole") { onApplyPreset(-140, 4, 80, 80, 40) }
                PresetChip("Right Hole") { onApplyPreset(140, 4, 80, 80, 40) }
                PresetChip("Dynamic Pill") { onApplyPreset(0, 4, 150, 60, 30) }
                PresetChip("Waterdrop") { onApplyPreset(0, 0, 95, 75, 20) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calibration Sliders
            SliderWithLabel(
                label = stringResource(id = R.string.pos_x_label),
                value = config.posX,
                range = -180f..180f,
                onValueChange = onXChange
            )

            SliderWithLabel(
                label = stringResource(id = R.string.pos_y_label),
                value = config.posY,
                range = 0f..100f,
                onValueChange = onYChange
            )

            SliderWithLabel(
                label = stringResource(id = R.string.size_width_label),
                value = config.width,
                range = 30f..250f,
                onValueChange = onWidthChange
            )

            SliderWithLabel(
                label = stringResource(id = R.string.size_height_label),
                value = config.height,
                range = 30f..140f,
                onValueChange = onHeightChange
            )

            SliderWithLabel(
                label = stringResource(id = R.string.corner_radius_label),
                value = config.cornerRadius,
                range = 0f..60f,
                onValueChange = onCornerRadiusChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reset Button
            OutlinedButton(
                onClick = onResetDefaults,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.reset_defaults),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryCyan
        )
    }
}
