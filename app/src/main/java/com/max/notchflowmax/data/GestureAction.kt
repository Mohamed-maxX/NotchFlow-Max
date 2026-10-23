package com.max.notchflowmax.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.max.notchflowmax.R

enum class GestureAction(
    val id: String,
    @StringRes val titleRes: Int
) {
    NONE("none", R.string.action_none),
    TAKE_SCREENSHOT("take_screenshot", R.string.action_screenshot),
    TOGGLE_FLASHLIGHT("toggle_flashlight", R.string.action_flashlight),
    LOCK_SCREEN("lock_screen", R.string.action_lock_screen),
    NOTIFICATIONS("notifications", R.string.action_notifications),
    QUICK_SETTINGS("quick_settings", R.string.action_quick_settings),
    POWER_MENU("power_menu", R.string.action_power_menu),
    MEDIA_PLAY_PAUSE("media_play_pause", R.string.action_media_play_pause),
    MEDIA_NEXT("media_next", R.string.action_media_next),
    MEDIA_PREVIOUS("media_prev", R.string.action_media_prev),
    RINGER_MODE_TOGGLE("ringer_toggle", R.string.action_ringer_toggle),
    FLOW_CAPSULE("flow_capsule", R.string.action_flow_capsule);

    companion object {
        fun fromId(id: String): GestureAction =
            entries.find { it.id == id } ?: NONE
    }
}
