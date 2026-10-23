package com.max.notchflowmax.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.max.notchflowmax.R
import com.max.notchflowmax.ui.components.GlowCard
import com.max.notchflowmax.ui.components.PermissionToggleRow

@Composable
fun PermissionsCard(
    isServiceEnabled: Boolean,
    hasOverlayPerm: Boolean,
    hasAccessibilityPerm: Boolean,
    isBatteryOptIgnored: Boolean,
    onServiceToggle: (Boolean) -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestBatteryOpt: () -> Unit
) {
    val allGranted = hasOverlayPerm && hasAccessibilityPerm && isBatteryOptIgnored

    GlowCard(
        title = stringResource(id = R.string.service_status_title),
        subtitle = stringResource(id = R.string.service_status_desc),
        icon = Icons.Default.Security,
        isHighlighted = isServiceEnabled && allGranted
    ) {
        Column {
            PermissionToggleRow(
                title = stringResource(id = R.string.master_switch_title),
                description = stringResource(id = R.string.master_switch_desc),
                isGranted = isServiceEnabled,
                icon = Icons.Default.PowerSettingsNew,
                onGrantClick = {},
                showSwitch = true,
                switchChecked = isServiceEnabled,
                onSwitchChanged = onServiceToggle
            )

            Spacer(modifier = Modifier.height(6.dp))

            PermissionToggleRow(
                title = stringResource(id = R.string.perm_overlay_title),
                description = stringResource(id = R.string.perm_overlay_desc),
                isGranted = hasOverlayPerm,
                icon = Icons.Default.Layers,
                onGrantClick = onRequestOverlay
            )

            Spacer(modifier = Modifier.height(6.dp))

            PermissionToggleRow(
                title = stringResource(id = R.string.perm_accessibility_title),
                description = stringResource(id = R.string.perm_accessibility_desc),
                isGranted = hasAccessibilityPerm,
                icon = Icons.Default.Security,
                onGrantClick = onRequestAccessibility
            )

            Spacer(modifier = Modifier.height(6.dp))

            PermissionToggleRow(
                title = stringResource(id = R.string.perm_battery_title),
                description = stringResource(id = R.string.perm_battery_desc),
                isGranted = isBatteryOptIgnored,
                icon = Icons.Default.BatteryAlert,
                onGrantClick = onRequestBatteryOpt
            )
        }
    }
}
