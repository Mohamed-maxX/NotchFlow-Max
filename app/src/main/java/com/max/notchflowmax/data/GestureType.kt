package com.max.notchflowmax.data

import androidx.annotation.StringRes
import com.max.notchflowmax.R

enum class GestureType(
    val id: String,
    @StringRes val titleRes: Int
) {
    SINGLE_TAP("single_tap", R.string.gesture_single_tap),
    DOUBLE_TAP("double_tap", R.string.gesture_double_tap),
    LONG_PRESS("long_press", R.string.gesture_long_press),
    SWIPE_LEFT("swipe_left", R.string.gesture_swipe_left),
    SWIPE_RIGHT("swipe_right", R.string.gesture_swipe_right),
    SWIPE_DOWN("swipe_down", R.string.gesture_swipe_down);

    companion object {
        fun fromId(id: String): GestureType =
            entries.find { it.id == id } ?: SINGLE_TAP
    }
}
