package com.max.notchflowmax.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notch_flow_preferences")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val POS_X = intPreferencesKey("pos_x")
        val POS_Y = intPreferencesKey("pos_y")
        val WIDTH = intPreferencesKey("width")
        val HEIGHT = intPreferencesKey("height")
        val CORNER_RADIUS = intPreferencesKey("corner_radius")
        val PREVIEW_ACTIVE = booleanPreferencesKey("preview_active")
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val AUTO_PAUSE_LANDSCAPE = booleanPreferencesKey("auto_pause_landscape")

        fun gestureKey(gesture: GestureType) = stringPreferencesKey("gesture_${gesture.id}")
    }

    val configFlow: Flow<NotchConfig> = context.dataStore.data.map { prefs ->
        val gesturesMap = mutableMapOf<GestureType, GestureAction>()
        val defaults = NotchConfig.defaultGestures()
        for (type in GestureType.entries) {
            val actionId = prefs[Keys.gestureKey(type)]
            gesturesMap[type] = if (actionId != null) {
                GestureAction.fromId(actionId)
            } else {
                defaults[type] ?: GestureAction.NONE
            }
        }

        NotchConfig(
            posX = prefs[Keys.POS_X] ?: 0,
            posY = prefs[Keys.POS_Y] ?: 0,
            width = prefs[Keys.WIDTH] ?: 90,
            height = prefs[Keys.HEIGHT] ?: 90,
            cornerRadius = prefs[Keys.CORNER_RADIUS] ?: 45,
            previewActive = prefs[Keys.PREVIEW_ACTIVE] ?: false,
            serviceEnabled = prefs[Keys.SERVICE_ENABLED] ?: true,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            autoPauseLandscape = prefs[Keys.AUTO_PAUSE_LANDSCAPE] ?: true,
            gestures = gesturesMap
        )
    }

    suspend fun updatePosX(x: Int) {
        context.dataStore.edit { it[Keys.POS_X] = x }
    }

    suspend fun updatePosY(y: Int) {
        context.dataStore.edit { it[Keys.POS_Y] = y }
    }

    suspend fun updateWidth(w: Int) {
        context.dataStore.edit { it[Keys.WIDTH] = w }
    }

    suspend fun updateHeight(h: Int) {
        context.dataStore.edit { it[Keys.HEIGHT] = h }
    }

    suspend fun updateCornerRadius(r: Int) {
        context.dataStore.edit { it[Keys.CORNER_RADIUS] = r }
    }

    suspend fun setPreviewActive(active: Boolean) {
        context.dataStore.edit { it[Keys.PREVIEW_ACTIVE] = active }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SERVICE_ENABLED] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setAutoPauseLandscape(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_PAUSE_LANDSCAPE] = enabled }
    }

    suspend fun updateGestureAction(gesture: GestureType, action: GestureAction) {
        context.dataStore.edit { it[Keys.gestureKey(gesture)] = action.id }
    }

    suspend fun applyPreset(x: Int, y: Int, w: Int, h: Int, radius: Int) {
        context.dataStore.edit {
            it[Keys.POS_X] = x
            it[Keys.POS_Y] = y
            it[Keys.WIDTH] = w
            it[Keys.HEIGHT] = h
            it[Keys.CORNER_RADIUS] = radius
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[Keys.POS_X] = 0
            prefs[Keys.POS_Y] = 0
            prefs[Keys.WIDTH] = 90
            prefs[Keys.HEIGHT] = 90
            prefs[Keys.CORNER_RADIUS] = 45
            prefs[Keys.PREVIEW_ACTIVE] = false
            prefs[Keys.SERVICE_ENABLED] = true
            prefs[Keys.HAPTIC_FEEDBACK] = true
            prefs[Keys.AUTO_PAUSE_LANDSCAPE] = true

            val defaults = NotchConfig.defaultGestures()
            for ((type, action) in defaults) {
                prefs[Keys.gestureKey(type)] = action.id
            }
        }
    }
}
