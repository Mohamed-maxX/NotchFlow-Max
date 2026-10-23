package com.max.notchflowmax.data

data class NotchConfig(
    val posX: Int = 0,
    val posY: Int = 0,
    val width: Int = 90,
    val height: Int = 90,
    val cornerRadius: Int = 45,
    val previewActive: Boolean = false,
    val serviceEnabled: Boolean = true,
    val hapticFeedback: Boolean = true,
    val autoPauseLandscape: Boolean = true,
    val gestures: Map<GestureType, GestureAction> = defaultGestures()
) {
    companion object {
        fun defaultGestures(): Map<GestureType, GestureAction> = mapOf(
            GestureType.SINGLE_TAP to GestureAction.TAKE_SCREENSHOT,
            GestureType.DOUBLE_TAP to GestureAction.FLOW_CAPSULE,
            GestureType.LONG_PRESS to GestureAction.TOGGLE_FLASHLIGHT,
            GestureType.SWIPE_LEFT to GestureAction.MEDIA_PREVIOUS,
            GestureType.SWIPE_RIGHT to GestureAction.MEDIA_NEXT,
            GestureType.SWIPE_DOWN to GestureAction.NOTIFICATIONS
        )
    }
}
