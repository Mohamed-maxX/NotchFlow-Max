package com.max.notchflowmax.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.max.notchflowmax.data.PreferencesRepository
import com.max.notchflowmax.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            val repository = PreferencesRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val config = repository.configFlow.first()
                    if (config.serviceEnabled && PermissionHelper.hasOverlayPermission(context)) {
                        NotchOverlayService.start(context)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
