package com.max.notchflowmax.service

import android.accessibilityservice.AccessibilityService
import android.content.res.Configuration
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.max.notchflowmax.util.FlashlightController

class NotchFlowAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: NotchFlowAccessibilityService? = null
            private set

        val isRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        // Attach overlay directly inside AccessibilityService using TYPE_ACCESSIBILITY_OVERLAY
        NotchOverlayManager.attach(this, isAccessibility = true)

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val autoPause = NotchOverlayManager.configFlow.value.autoPauseLandscape
        NotchOverlayManager.setLandscapePaused(isLandscape && autoPause)

        FlashlightController.register(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        val autoPause = NotchOverlayManager.configFlow.value.autoPauseLandscape
        NotchOverlayManager.setLandscapePaused(isLandscape && autoPause)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No heavy window event processing needed
    }

    override fun onInterrupt() {
        // Interrupt handling
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    private fun cleanup() {
        FlashlightController.unregister()
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
