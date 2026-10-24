package com.max.notchflowmax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.max.notchflowmax.ui.theme.ElectricBlue
import com.max.notchflowmax.ui.theme.PrimaryBlue
import com.max.notchflowmax.ui.theme.SurfaceVariantDark
import com.max.notchflowmax.ui.theme.TextPrimary
import com.max.notchflowmax.ui.theme.TextSecondary

@Composable
fun SliderWithLabel(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    unit: String = "dp",
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .background(
                        color = PrimaryBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$value $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newVal = (value - 2).coerceIn(range.start.toInt(), range.endInclusive.toInt())
                    onValueChange(newVal)
                },
                modifier = Modifier
                    .size(28.dp)
                    .background(SurfaceVariantDark, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = range,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricBlue,
                    activeTrackColor = PrimaryBlue,
                    inactiveTrackColor = SurfaceVariantDark
                )
            )

            IconButton(
                onClick = {
                    val newVal = (value + 2).coerceIn(range.start.toInt(), range.endInclusive.toInt())
                    onValueChange(newVal)
                },
                modifier = Modifier
                    .size(28.dp)
                    .background(SurfaceVariantDark, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
