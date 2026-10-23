package com.max.notchflowmax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.max.notchflowmax.R
import com.max.notchflowmax.ui.theme.BackgroundDark
import com.max.notchflowmax.ui.theme.CardBorder
import com.max.notchflowmax.ui.theme.PrimaryNeon
import com.max.notchflowmax.ui.theme.SecondaryCyan
import com.max.notchflowmax.ui.theme.SuccessGreen
import com.max.notchflowmax.ui.theme.SurfaceDark
import com.max.notchflowmax.ui.theme.TextPrimary
import com.max.notchflowmax.ui.theme.TextSecondary
import com.max.notchflowmax.ui.viewmodel.MainViewModel
import com.max.notchflowmax.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uiState.config.serviceEnabled && uiState.hasOverlayPermission) PrimaryNeon else TextSecondary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { viewModel.checkForUpdates(manual = true) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SecondaryCyan
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Check for Updates",
                            tint = SecondaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OTA Update",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Card 1: Service Status & Permissions Card
            item {
                PermissionsCard(
                    isServiceEnabled = uiState.config.serviceEnabled,
                    hasOverlayPerm = uiState.hasOverlayPermission,
                    hasAccessibilityPerm = uiState.hasAccessibilityPermission,
                    isBatteryOptIgnored = uiState.isBatteryOptimizationIgnored,
                    onServiceToggle = { viewModel.setServiceEnabled(it) },
                    onRequestOverlay = { PermissionHelper.openOverlaySettings(context) },
                    onRequestAccessibility = { PermissionHelper.openAccessibilitySettings(context) },
                    onRequestBatteryOpt = { PermissionHelper.requestIgnoreBatteryOptimization(context) }
                )
            }

            // Card 2: Notch Calibration Card
            item {
                CalibrationCard(
                    config = uiState.config,
                    onXChange = { viewModel.updatePosX(it) },
                    onYChange = { viewModel.updatePosY(it) },
                    onWidthChange = { viewModel.updateWidth(it) },
                    onHeightChange = { viewModel.updateHeight(it) },
                    onCornerRadiusChange = { viewModel.updateCornerRadius(it) },
                    onPreviewToggle = { viewModel.setPreviewActive(it) },
                    onApplyPreset = { x, y, w, h, r -> viewModel.applyPreset(x, y, w, h, r) },
                    onResetDefaults = { viewModel.resetToDefaults() }
                )
            }

            // Card 3: Gestures & Actions Card + Smart UX
            item {
                GesturesCard(
                    config = uiState.config,
                    onGestureChange = { gesture, action ->
                        viewModel.updateGesture(gesture, action)
                    },
                    onHapticsToggle = { viewModel.setHapticFeedback(it) },
                    onAutoPauseToggle = { viewModel.setAutoPauseLandscape(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // OTA Update Dialog
        UpdateDialog(
            status = uiState.updateStatus,
            onDismiss = { viewModel.dismissUpdateDialog() },
            onDownloadInstall = { info -> viewModel.downloadAndInstallUpdate(info) }
        )
    }
}
