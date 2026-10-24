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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.max.notchflowmax.R
import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.ui.components.GlowCard
import com.max.notchflowmax.ui.components.SliderWithLabel
import com.max.notchflowmax.ui.theme.BackgroundDark
import com.max.notchflowmax.ui.theme.ElectricBlue
import com.max.notchflowmax.ui.theme.PrimaryBlue
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
                        color = if (config.previewActive) ElectricBlue.copy(alpha = 0.15f) else SurfaceVariantDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (config.previewActive) ElectricBlue.copy(alpha = 0.25f) else TextTertiary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (config.previewActive) ElectricBlue else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.preview_overlay_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (config.previewActive) ElectricBlue else TextPrimary
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
                        checkedTrackColor = ElectricBlue,
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
                PresetChip(stringResource(id = R.string.preset_center)) { onApplyPreset(0, 0, 90, 90, 45) }
                PresetChip(stringResource(id = R.string.preset_left)) { onApplyPreset(-140, 0, 90, 90, 45) }
                PresetChip(stringResource(id = R.string.preset_right)) { onApplyPreset(140, 0, 90, 90, 45) }
                PresetChip(stringResource(id = R.string.preset_pill)) { onApplyPreset(0, 0, 160, 65, 32) }
                PresetChip(stringResource(id = R.string.preset_teardrop)) { onApplyPreset(0, 0, 100, 80, 20) }
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
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ElectricBlue
        )
    }
}
