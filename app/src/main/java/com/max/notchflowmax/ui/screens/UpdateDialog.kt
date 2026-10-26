package com.max.notchflowmax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.max.notchflowmax.R
import com.max.notchflowmax.update.UpdateInfo
import com.max.notchflowmax.update.UpdateStatus
import com.max.notchflowmax.ui.theme.BackgroundDark
import com.max.notchflowmax.ui.theme.CardBorder
import com.max.notchflowmax.ui.theme.ElectricBlue
import com.max.notchflowmax.ui.theme.PrimaryBlue
import com.max.notchflowmax.ui.theme.SurfaceDark
import com.max.notchflowmax.ui.theme.SurfaceVariantDark
import com.max.notchflowmax.ui.theme.TextPrimary
import com.max.notchflowmax.ui.theme.TextSecondary

@Composable
fun UpdateDialog(
    status: UpdateStatus,
    onDismiss: () -> Unit,
    onDownloadInstall: (UpdateInfo) -> Unit
) {
    when (status) {
        is UpdateStatus.UpdateAvailable -> {
            val info = status.info
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceDark,
                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(PrimaryBlue.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.update_available_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(ElectricBlue.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "v${info.versionName} (Build ${info.versionCode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlue
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Changelog:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = info.changelog.ifEmpty { "Performance optimizations & bug fixes." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onDownloadInstall(info) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(stringResource(id = R.string.update_download_install))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text(stringResource(id = R.string.update_later))
                    }
                }
            )
        }
        is UpdateStatus.Downloading -> {
            val progress = status.progress
            AlertDialog(
                onDismissRequest = {},
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = stringResource(id = R.string.update_downloading, progress),
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = ElectricBlue,
                            trackColor = SurfaceVariantDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$progress%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                    }
                },
                confirmButton = {}
            )
        }
        is UpdateStatus.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = stringResource(id = R.string.update_failed),
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        text = status.message,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }
}
