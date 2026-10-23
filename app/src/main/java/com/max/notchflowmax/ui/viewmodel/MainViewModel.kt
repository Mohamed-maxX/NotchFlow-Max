package com.max.notchflowmax.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.max.notchflowmax.data.GestureAction
import com.max.notchflowmax.data.GestureType
import com.max.notchflowmax.data.NotchConfig
import com.max.notchflowmax.data.PreferencesRepository
import com.max.notchflowmax.service.NotchOverlayService
import com.max.notchflowmax.update.UpdateInfo
import com.max.notchflowmax.update.UpdateManager
import com.max.notchflowmax.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepo = PreferencesRepository(application)
    private val updateManager = UpdateManager(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeConfig()
        observeUpdateStatus()
        refreshPermissions()
        // Check for updates automatically on app startup as requested
        checkForUpdates(manual = false)
    }

    private fun observeConfig() {
        viewModelScope.launch {
            preferencesRepo.configFlow.collectLatest { config ->
                _uiState.update { it.copy(config = config, isReady = true) }
                syncOverlayService(config)
            }
        }
    }

    private fun observeUpdateStatus() {
        viewModelScope.launch {
            updateManager.status.collectLatest { status ->
                _uiState.update { it.copy(updateStatus = status) }
            }
        }
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        val hasOverlay = PermissionHelper.hasOverlayPermission(context)
        val hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
        val isBatteryIgnored = PermissionHelper.isBatteryOptimizationIgnored(context)

        _uiState.update {
            it.copy(
                hasOverlayPermission = hasOverlay,
                hasAccessibilityPermission = hasAccessibility,
                isBatteryOptimizationIgnored = isBatteryIgnored
            )
        }
        syncOverlayService(_uiState.value.config)
    }

    private fun syncOverlayService(config: NotchConfig) {
        val context = getApplication<Application>()
        if (config.serviceEnabled && PermissionHelper.hasOverlayPermission(context)) {
            NotchOverlayService.start(context)
        } else {
            NotchOverlayService.stop(context)
        }
    }

    fun setServiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setServiceEnabled(enabled)
        }
    }

    fun setPreviewActive(active: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setPreviewActive(active)
        }
    }

    fun updatePosX(x: Int) {
        viewModelScope.launch { preferencesRepo.updatePosX(x) }
    }

    fun updatePosY(y: Int) {
        viewModelScope.launch { preferencesRepo.updatePosY(y) }
    }

    fun updateWidth(w: Int) {
        viewModelScope.launch { preferencesRepo.updateWidth(w) }
    }

    fun updateHeight(h: Int) {
        viewModelScope.launch { preferencesRepo.updateHeight(h) }
    }

    fun updateCornerRadius(r: Int) {
        viewModelScope.launch { preferencesRepo.updateCornerRadius(r) }
    }

    fun applyPreset(x: Int, y: Int, w: Int, h: Int, radius: Int) {
        viewModelScope.launch { preferencesRepo.applyPreset(x, y, w, h, radius) }
    }

    fun resetToDefaults() {
        viewModelScope.launch { preferencesRepo.resetToDefaults() }
    }

    fun updateGesture(gesture: GestureType, action: GestureAction) {
        viewModelScope.launch { preferencesRepo.updateGestureAction(gesture, action) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { preferencesRepo.setHapticFeedback(enabled) }
    }

    fun setAutoPauseLandscape(enabled: Boolean) {
        viewModelScope.launch { preferencesRepo.setAutoPauseLandscape(enabled) }
    }

    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            updateManager.checkForUpdates(manual = manual)
        }
    }

    fun downloadAndInstallUpdate(info: UpdateInfo) {
        viewModelScope.launch {
            updateManager.downloadAndInstall(info)
        }
    }

    fun dismissUpdateDialog() {
        updateManager.resetStatus()
    }
}
