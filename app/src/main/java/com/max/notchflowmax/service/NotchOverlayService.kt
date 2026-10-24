package com.max.notchflowmax.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.max.notchflowmax.MainActivity
import com.max.notchflowmax.R
import com.max.notchflowmax.data.PreferencesRepository
import com.max.notchflowmax.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotchOverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "notch_flow_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, NotchOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NotchOverlayService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var preferencesRepo: PreferencesRepository
    private var orientationWatcher: OrientationWatcher? = null

    override fun onCreate() {
        super.onCreate()
        preferencesRepo = PreferencesRepository(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // If accessibility service is not hosting, fallback to hosting in this service
        if (!NotchFlowAccessibilityService.isRunning && PermissionHelper.hasOverlayPermission(this)) {
            NotchOverlayManager.attach(this, isAccessibility = false)
        }

        orientationWatcher = OrientationWatcher(this) { isLandscape ->
            val autoPause = NotchOverlayManager.configFlow.value.autoPauseLandscape
            NotchOverlayManager.setLandscapePaused(isLandscape && autoPause)
        }
        orientationWatcher?.start()

        observeConfig()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If accessibility service is not hosting, check if we should attach
        if (!NotchFlowAccessibilityService.isRunning && PermissionHelper.hasOverlayPermission(this)) {
            NotchOverlayManager.attach(this, isAccessibility = false)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeConfig() {
        serviceScope.launch {
            preferencesRepo.configFlow.collectLatest { config ->
                NotchOverlayManager.updateConfigLive(config)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NotchFlow Max Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Runs the camera notch touch detection engine"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NotchFlow Max Active")
            .setContentText("Camera cutout gesture listener is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationWatcher?.stop()
        orientationWatcher = null
        if (!NotchFlowAccessibilityService.isRunning) {
            NotchOverlayManager.detach(this)
        }
        serviceScope.cancel()
    }
}
