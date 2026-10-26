package com.max.notchflowmax.service

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class NotchFlowAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: NotchFlowAccessibilityService? = null
            private set

        val isRunning: Boolean
            get() = instance != null
    }

    private var orientationWatcher: OrientationWatcher? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        // Attach overlay directly inside AccessibilityService using TYPE_ACCESSIBILITY_OVERLAY
        // This allows the overlay to draw directly over the camera notch cutout and top status bar!
        NotchOverlayManager.attach(this, isAccessibility = true)

        orientationWatcher = OrientationWatcher(this) { isLandscape ->
            val autoPause = NotchOverlayManager.configFlow.value.autoPauseLandscape
            NotchOverlayManager.setLandscapePaused(isLandscape && autoPause)
        }
        orientationWatcher?.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Window tracking if needed
    }

    override fun onInterrupt() {
        // Interrupt handling
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationWatcher?.stop()
        orientationWatcher = null
        NotchOverlayManager.detach(this)
        if (instance == this) {
            instance = null
        }
    }

    fun takeScreenshot(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun lockScreen(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun pullNotifications(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openQuickSettings(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openPowerMenu(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun performHome(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_HOME)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun performBack(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_BACK)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun performRecents(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_RECENTS)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
