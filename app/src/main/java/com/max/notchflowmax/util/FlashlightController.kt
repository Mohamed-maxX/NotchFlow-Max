package com.max.notchflowmax.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

object FlashlightController {

    private var isTorchOn: Boolean = false

    fun toggle(context: Context): Boolean {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return false
        try {
            val cameraId = getTorchCameraId(cameraManager) ?: return false
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            return isTorchOn
        } catch (e: Exception) {
            e.printStackTrace()
            isTorchOn = false
            return false
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
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }
        return cameraManager.cameraIdList.firstOrNull()
    }
}
