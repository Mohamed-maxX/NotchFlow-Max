package com.max.notchflowmax.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

object FlashlightController {

    @Volatile
    private var isTorchOn: Boolean = false
    private var isCallbackRegistered: Boolean = false
    private var registeredCameraManager: CameraManager? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            isTorchOn = enabled
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            super.onTorchModeUnavailable(cameraId)
            isTorchOn = false
        }
    }

    fun register(context: Context) {
        if (isCallbackRegistered) return
        try {
            val cameraManager = context.applicationContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
            cameraManager.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
            registeredCameraManager = cameraManager
            isCallbackRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregister() {
        if (!isCallbackRegistered) return
        try {
            registeredCameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            registeredCameraManager = null
            isCallbackRegistered = false
        }
    }

    fun toggle(context: Context): Boolean {
        register(context)
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return false
        return try {
            val cameraId = getTorchCameraId(cameraManager) ?: return false
            val targetState = !isTorchOn
            cameraManager.setTorchMode(cameraId, targetState)
            isTorchOn = targetState
            targetState
        } catch (e: Exception) {
            e.printStackTrace()
            isTorchOn = false
            false
        }
    }

    fun turnOff(context: Context) {
        if (!isTorchOn) return
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        try {
            val cameraId = getTorchCameraId(cameraManager) ?: return
            cameraManager.setTorchMode(cameraId, false)
            isTorchOn = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTorchCameraId(cameraManager: CameraManager): String? {
        return try {
            for (id in cameraManager.cameraIdList) {
                try {
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        return id
                    }
                } catch (_: Exception) {}
            }
            cameraManager.cameraIdList.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
