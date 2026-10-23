package com.max.notchflowmax.ui.viewmodel

import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.update.UpdateStatus

data class MainUiState(
    val config: NotchConfig = NotchConfig(),
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val isReady: Boolean = false
)
