package com.max.notchflowmax.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.max.notchflowmax.MainActivity
import com.max.notchflowmax.R
import com.max.notchflowmax.data.GestureAction
import com.max.notchflowmax.data.GestureType
import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.data.PreferencesRepository
import com.max.notchflowmax.util.HapticHelper
import com.max.notchflowmax.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    private lateinit var windowManager: WindowManager
    private lateinit var actionExecutor: ActionExecutor
    private lateinit var flowCapsule: FlowCapsuleOverlay
    private lateinit var orientationWatcher: OrientationWatcher

    private var overlayView: View? = null
    private var currentConfig: NotchConfig = NotchConfig()
    private var isLandscapePaused: Boolean = false

    override fun onCreate() {
        super.onCreate()
        preferencesRepo = PreferencesRepository(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        actionExecutor = ActionExecutor(this)
        flowCapsule = FlowCapsuleOverlay(this, actionExecutor)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        orientationWatcher = OrientationWatcher(this) { isLandscape ->
            isLandscapePaused = isLandscape && currentConfig.autoPauseLandscape
            updateOverlayVisibility()
        }
        orientationWatcher.start()

        observeConfig()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeConfig() {
        serviceScope.launch {
            preferencesRepo.configFlow.collectLatest { config ->
                currentConfig = config
                applyConfig(config)
            }
        }
    }

    private fun applyConfig(config: NotchConfig) {
        if (!PermissionHelper.hasOverlayPermission(this) || !config.serviceEnabled) {
            removeOverlay()
            return
        }

        if (overlayView == null) {
            createOverlayView()
        } else {
            updateOverlayLayoutParams()
        }
        updateOverlayAppearance()
        updateOverlayVisibility()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlayView() {
        val view = View(this)
        val density = resources.displayMetrics.density

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                triggerGesture(GestureType.SINGLE_TAP)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                triggerGesture(GestureType.DOUBLE_TAP)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                triggerGesture(GestureType.LONG_PRESS)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                val minSwipeDistance = 30 * density

                if (abs(diffX) > abs(diffY)) {
                    if (diffX > minSwipeDistance) {
                        triggerGesture(GestureType.SWIPE_RIGHT)
                        return true
                    } else if (diffX < -minSwipeDistance) {
                        triggerGesture(GestureType.SWIPE_LEFT)
                        return true
                    }
                } else {
                    if (diffY > minSwipeDistance) {
                        triggerGesture(GestureType.SWIPE_DOWN)
                        return true
                    }
                }
                return false
            }
        })

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        overlayView = view
        val layoutParams = createLayoutParams()

        try {
            windowManager.addView(view, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
            overlayView = null
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val density = resources.displayMetrics.density
        return WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = (currentConfig.posX * density).toInt()
            y = (currentConfig.posY * density).toInt()
            width = (currentConfig.width * density).toInt()
            height = (currentConfig.height * density).toInt()
        }
    }

    private fun updateOverlayLayoutParams() {
        val view = overlayView ?: return
        val layoutParams = createLayoutParams()
        try {
            windowManager.updateViewLayout(view, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayAppearance() {
        val view = overlayView ?: return
        val density = resources.displayMetrics.density
        val radiusPx = currentConfig.cornerRadius * density

        if (currentConfig.previewActive) {
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radiusPx
                setColor(Color.parseColor("#4D00FF66")) // Neon green translucent fill
                setStroke((2 * density).toInt(), Color.parseColor("#00FF66")) // Neon green vibrant stroke
            }
        } else {
            view.background = null // 100% transparent for invisible touch zone
        }
    }

    private fun updateOverlayVisibility() {
        val view = overlayView ?: return
        if (isLandscapePaused || !currentConfig.serviceEnabled) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun triggerGesture(gesture: GestureType) {
        if (isLandscapePaused || !currentConfig.serviceEnabled) return

        if (currentConfig.hapticFeedback) {
            HapticHelper.performClickHaptic(this)
        }

        val action = currentConfig.gestures[gesture] ?: GestureAction.NONE
        actionExecutor.execute(action) {
            flowCapsule.show(currentConfig.posY, currentConfig.height)
        }
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NotchFlow Max Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Runs the camera notch touch detection service"
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
        orientationWatcher.stop()
        removeOverlay()
        flowCapsule.dismiss()
        serviceScope.cancel()
    }
}
