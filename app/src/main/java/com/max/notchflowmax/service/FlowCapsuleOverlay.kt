package com.max.notchflowmax.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.max.notchflowmax.data.GestureAction
import com.max.notchflowmax.util.HapticHelper

class FlowCapsuleOverlay(
    private val context: Context,
    private val actionExecutor: ActionExecutor
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var capsuleView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoDismissRunnable = Runnable { dismiss() }

    val isShowing: Boolean
        get() = capsuleView != null

    @SuppressLint("ClickableViewAccessibility")
    fun show(notchY: Int, notchHeight: Int) {
        if (isShowing) {
            dismiss()
            return
        }

        val density = context.resources.displayMetrics.density
        val topMarginPx = ((notchY + notchHeight + 10) * density).toInt()

        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (NotchFlowAccessibilityService.isRunning) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            format = PixelFormat.TRANSLUCENT
            width = (330 * density).toInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = topMarginPx
        }

        val root = createCapsuleLayout()
        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                dismiss()
                true
            } else {
                resetAutoDismiss()
                false
            }
        }

        capsuleView = root
        try {
            windowManager.addView(root, layoutParams)
            resetAutoDismiss()
        } catch (e: Exception) {
            e.printStackTrace()
            capsuleView = null
        }
    }

    fun dismiss() {
        handler.removeCallbacks(autoDismissRunnable)
        capsuleView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        capsuleView = null
    }

    private fun resetAutoDismiss() {
        handler.removeCallbacks(autoDismissRunnable)
        handler.postDelayed(autoDismissRunnable, 5000)
    }

    private fun createCapsuleLayout(): LinearLayout {
        val density = context.resources.displayMetrics.density
        val pad16 = (16 * density).toInt()
        val pad8 = (8 * density).toInt()
        val pad12 = (12 * density).toInt()

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad16, pad12, pad16, pad16)

            // Electric Blue capsule dark glass background
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EE0A0E17")) // Deep dark translucent
                cornerRadius = 24 * density
                setStroke((1.8f * density).toInt(), Color.parseColor("#2979FF")) // Glowing Electric Blue border
            }

            // Header row
            val header = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                val dot = View(context).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor("#00B0FF"))
                    }
                    layoutParams = LinearLayout.LayoutParams((8 * density).toInt(), (8 * density).toInt()).apply {
                        marginEnd = pad8
                    }
                }
                addView(dot)

                val title = TextView(context).apply {
                    text = "Flow Capsule"
                    setTextColor(Color.parseColor("#F8FAFC"))
                    textSize = 14f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(title)

                val closeBtn = TextView(context).apply {
                    text = "✕"
                    setTextColor(Color.parseColor("#94A3B8"))
                    textSize = 15f
                    setPadding(pad8, 0, pad8, 0)
                    setOnClickListener { dismiss() }
                }
                addView(closeBtn)
            }
            addView(header)

            // Volume Control Row
            val volRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, pad8, 0, 0)

                val label = TextView(context).apply {
                    text = "🔊 Volume"
                    setTextColor(Color.parseColor("#94A3B8"))
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams((75 * density).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
                }
                addView(label)

                val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8

                val volSeekBar = SeekBar(context).apply {
                    max = maxVol
                    progress = currentVol
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                                resetAutoDismiss()
                            }
                        }
                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })
                }
                addView(volSeekBar)
            }
            addView(volRow)

            // Brightness Control Row
            val brightRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, pad8, 0, pad12)

                val label = TextView(context).apply {
                    text = "🔆 Brightness"
                    setTextColor(Color.parseColor("#94A3B8"))
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams((75 * density).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
                }
                addView(label)

                var currentBrightness = 128
                try {
                    currentBrightness = Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        128
                    )
                } catch (_: Exception) {}

                val brightSeekBar = SeekBar(context).apply {
                    max = 255
                    progress = currentBrightness
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                try {
                                    if (Settings.System.canWrite(context)) {
                                        Settings.System.putInt(
                                            context.contentResolver,
                                            Settings.System.SCREEN_BRIGHTNESS,
                                            progress
                                        )
                                    }
                                } catch (_: Exception) {}
                                resetAutoDismiss()
                            }
                        }
                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })
                }
                addView(brightSeekBar)
            }
            addView(brightRow)

            // Quick Actions Buttons Row
            val actionsRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                weightSum = 5f

                fun createActionButton(emoji: String, label: String, action: () -> Unit): LinearLayout {
                    return LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        setPadding(pad4(density), pad4(density), pad4(density), pad4(density))

                        background = GradientDrawable().apply {
                            setColor(Color.parseColor("#1E293B"))
                            cornerRadius = 12 * density
                        }

                        val iconText = TextView(context).apply {
                            text = emoji
                            textSize = 16f
                            gravity = Gravity.CENTER
                        }
                        addView(iconText)

                        val subText = TextView(context).apply {
                            text = label
                            textSize = 10f
                            setTextColor(Color.parseColor("#94A3B8"))
                            gravity = Gravity.CENTER
                        }
                        addView(subText)

                        setOnClickListener {
                            HapticHelper.performClickHaptic(context)
                            action()
                            resetAutoDismiss()
                        }
                    }
                }

                addView(createActionButton("🔦", "Torch") {
                    actionExecutor.execute(GestureAction.TOGGLE_FLASHLIGHT)
                })
                addView(createActionButton("📸", "Shot") {
                    dismiss()
                    actionExecutor.execute(GestureAction.TAKE_SCREENSHOT)
                })
                addView(createActionButton("⏯️", "Play") {
                    actionExecutor.execute(GestureAction.MEDIA_PLAY_PAUSE)
                })
                addView(createActionButton("⏭️", "Next") {
                    actionExecutor.execute(GestureAction.MEDIA_NEXT)
                })
                addView(createActionButton("🔒", "Lock") {
                    dismiss()
                    actionExecutor.execute(GestureAction.LOCK_SCREEN)
                })
            }
            addView(actionsRow)
        }
    }

    private fun pad4(density: Float): Int = (4 * density).toInt()
}
