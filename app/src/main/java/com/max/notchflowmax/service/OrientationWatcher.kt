package com.max.notchflowmax.service

import android.content.Context
import android.content.res.Configuration
import android.view.OrientationEventListener

class OrientationWatcher(
    private val context: Context,
    private val onOrientationChanged: (isLandscape: Boolean) -> Unit
) {
    private var lastIsLandscape: Boolean = false

    private val orientationEventListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return
            val isLandscape = (orientation in 60..120) || (orientation in 240..300)
            if (isLandscape != lastIsLandscape) {
                lastIsLandscape = isLandscape
                onOrientationChanged(isLandscape)
            }
        }
    }

    fun start() {
        val currentOrientation = context.resources.configuration.orientation
        lastIsLandscape = (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        onOrientationChanged(lastIsLandscape)
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
    }

    fun stop() {
        orientationEventListener.disable()
    }
}
