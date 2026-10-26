package com.max.notchflowmax.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast
import com.max.notchflowmax.data.GestureAction
import com.max.notchflowmax.util.FlashlightController
import com.max.notchflowmax.util.HapticHelper

class ActionExecutor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    fun execute(action: GestureAction, onTriggerFlowCapsule: (() -> Unit)? = null) {
        try {
            when (action) {
                GestureAction.NONE -> {}
                GestureAction.TAKE_SCREENSHOT -> {
                    val accessibility = NotchFlowAccessibilityService.instance
                    if (accessibility != null) {
                        accessibility.takeScreenshot()
                    } else {
                        showAccessibilityToast()
                    }
                }
                GestureAction.TOGGLE_FLASHLIGHT -> {
                    FlashlightController.toggle(context)
                }
                GestureAction.LOCK_SCREEN -> {
                    val accessibility = NotchFlowAccessibilityService.instance
                    if (accessibility != null) {
                        accessibility.lockScreen()
                    } else {
                        showAccessibilityToast()
                    }
                }
                GestureAction.NOTIFICATIONS -> {
                    val accessibility = NotchFlowAccessibilityService.instance
                    if (accessibility != null) {
                        accessibility.pullNotifications()
                    } else {
                        showAccessibilityToast()
                    }
                }
                GestureAction.QUICK_SETTINGS -> {
                    val accessibility = NotchFlowAccessibilityService.instance
                    if (accessibility != null) {
                        accessibility.openQuickSettings()
                    } else {
                        showAccessibilityToast()
                    }
                }
                GestureAction.POWER_MENU -> {
                    val accessibility = NotchFlowAccessibilityService.instance
                    if (accessibility != null) {
                        accessibility.openPowerMenu()
                    } else {
                        showAccessibilityToast()
                    }
                }
                GestureAction.MEDIA_PLAY_PAUSE -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                }
                GestureAction.MEDIA_NEXT -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                }
                GestureAction.MEDIA_PREVIOUS -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                }
                GestureAction.RINGER_MODE_TOGGLE -> {
                    toggleRingerMode()
                }
                GestureAction.FLOW_CAPSULE -> {
                    onTriggerFlowCapsule?.invoke()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        try {
            val am = audioManager ?: return
            val eventTime = SystemClock.uptimeMillis()
            val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
            val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
            am.dispatchMediaKeyEvent(downEvent)
            am.dispatchMediaKeyEvent(upEvent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleRingerMode() {
        try {
            val am = audioManager ?: return
            if (notificationManager?.isNotificationPolicyAccessGranted == false) {
                // If DND policy not granted, toggle between Normal and Vibrate
                when (am.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    else -> am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
                return
            }

            when (am.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> am.ringerMode = AudioManager.RINGER_MODE_SILENT
                else -> am.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAccessibilityToast() {
        try {
            Toast.makeText(
                context,
                "Enable NotchFlow Accessibility Service to use this action",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
