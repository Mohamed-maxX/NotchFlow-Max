package com.max.notchflowmax.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.max.notchflowmax.data.GestureAction
import com.max.notchflowmax.data.GestureType
import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.util.HapticHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

@SuppressLint("StaticFieldLeak")
object NotchOverlayManager {

    private val _configFlow = MutableStateFlow(NotchConfig())
    val configFlow: StateFlow<NotchConfig> = _configFlow.asStateFlow()

    private var activeContext: Context? = null
    private var windowManager: WindowManager? = null
    private var isAccessibilityHost: Boolean = false
    private var overlayView: View? = null
    private var actionExecutor: ActionExecutor? = null
    private var flowCapsule: FlowCapsuleOverlay? = null
    private var isLandscapePaused: Boolean = false

    val isAttached: Boolean
        get() = overlayView != null

    val isHostingViaAccessibility: Boolean
        get() = isAccessibilityHost && isAttached

    fun setInitialConfig(config: NotchConfig) {
        _configFlow.value = config
    }

    fun updateConfigLive(config: NotchConfig) {
        _configFlow.value = config
        applyCurrentConfigToOverlay()
    }

    fun attach(context: Context, isAccessibility: Boolean) {
        // If already attached via Accessibility, do not downgrade to standard application overlay
        if (isAccessibilityHost && !isAccessibility && overlayView != null) {
            return
        }

        // If switching from app overlay to accessibility overlay, remove the old one first
        if (overlayView != null) {
            detach()
        }

        activeContext = context
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        isAccessibilityHost = isAccessibility
        actionExecutor = ActionExecutor(context)
        flowCapsule = FlowCapsuleOverlay(context, actionExecutor!!)

        val config = _configFlow.value
        if (config.serviceEnabled) {
            createAndAddOverlay(context, config)
        }
    }

    fun detach(fromContext: Context? = null) {
        if (fromContext != null && fromContext != activeContext) return

        removeOverlayView()
        try {
            flowCapsule?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        flowCapsule = null
        actionExecutor = null
        overlayView = null
        windowManager = null
        activeContext = null
        isAccessibilityHost = false
    }

    fun setLandscapePaused(paused: Boolean) {
        if (isLandscapePaused != paused) {
            isLandscapePaused = paused
            updateVisibility()
        }
    }

    private fun applyCurrentConfigToOverlay() {
        val context = activeContext ?: return
        val config = _configFlow.value

        if (!config.serviceEnabled) {
            removeOverlayView()
            return
        }

        if (overlayView == null) {
            createAndAddOverlay(context, config)
        } else {
            updateOverlayLayoutParams(config)
            updateOverlayAppearance(config)
            updateVisibility()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createAndAddOverlay(context: Context, config: NotchConfig) {
        val wm = windowManager ?: return

        val view = View(context)
        val density = context.resources.displayMetrics.density

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
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
        })

        // Custom touch listener with 40px delta threshold for instant responsive swipes on the notch
        var downX = 0f
        var downY = 0f
        var isSwipeTriggered = false
        val swipeThreshold = 40f * density

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    isSwipeTriggered = false
                    gestureDetector.onTouchEvent(event)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isSwipeTriggered) {
                        val dx = event.rawX - downX
                        val dy = event.rawY - downY

                        if (abs(dx) > abs(dy) && abs(dx) >= swipeThreshold) {
                            isSwipeTriggered = true
                            if (dx > 0) {
                                triggerGesture(GestureType.SWIPE_RIGHT)
                            } else {
                                triggerGesture(GestureType.SWIPE_LEFT)
                            }
                        } else if (dy >= swipeThreshold) {
                            isSwipeTriggered = true
                            triggerGesture(GestureType.SWIPE_DOWN)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isSwipeTriggered) {
                        true
                    } else {
                        gestureDetector.onTouchEvent(event)
                    }
                }
                else -> gestureDetector.onTouchEvent(event)
            }
        }

        overlayView = view
        val layoutParams = buildLayoutParams(config, density)

        try {
            wm.addView(view, layoutParams)
            updateOverlayAppearance(config)
            updateVisibility()
        } catch (e: Exception) {
            e.printStackTrace()
            overlayView = null
        }
    }

    private fun buildLayoutParams(config: NotchConfig, density: Float): WindowManager.LayoutParams {
        val windowType = if (isAccessibilityHost) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        return WindowManager.LayoutParams().apply {
            type = windowType
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = (config.posX * density).toInt()
            y = (config.posY * density).toInt()
            width = (config.width * density).toInt()
            height = (config.height * density).toInt()
        }
    }

    private fun updateOverlayLayoutParams(config: NotchConfig) {
        val view = overlayView ?: return
        val wm = windowManager ?: return
        try {
            if (!view.isAttachedToWindow) return
            val density = view.resources.displayMetrics.density
            val params = buildLayoutParams(config, density)
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayAppearance(config: NotchConfig) {
        val view = overlayView ?: return
        val density = view.resources.displayMetrics.density
        val radiusPx = config.cornerRadius * density

        if (config.previewActive) {
            // Semi-Transparent Electric Blue (#8000B0FF) with a solid Electric Blue border (#FF2979FF)
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radiusPx
                setColor(Color.parseColor("#8000B0FF")) // Glowing semi-transparent electric blue fill
                setStroke((2.5f * density).toInt(), Color.parseColor("#FF2979FF")) // Solid electric blue border
            }
        } else {
            view.background = null // 100% transparent invisible touch zone
        }
    }

    private fun updateVisibility() {
        val view = overlayView ?: return
        val config = _configFlow.value
        if (isLandscapePaused || !config.serviceEnabled) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun triggerGesture(gesture: GestureType) {
        try {
            val context = activeContext ?: return
            val config = _configFlow.value
            if (isLandscapePaused || !config.serviceEnabled) return

            if (config.hapticFeedback) {
                HapticHelper.performClickHaptic(context)
            }

            val action = config.gestures[gesture] ?: GestureAction.NONE
            actionExecutor?.execute(action) {
                flowCapsule?.show(config.posY, config.height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlayView() {
        val view = overlayView ?: return
        val wm = windowManager ?: return
        try {
            if (view.isAttachedToWindow) {
                wm.removeViewImmediate(view)
            } else {
                wm.removeView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            overlayView = null
        }
    }
}
