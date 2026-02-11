package com.example.timelimit.feature.settings

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timelimit.core.data.repository.BlockedAppsRepository
import com.example.timelimit.core.model.AppInfo
import com.example.timelimit.core.model.DaySchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.timelimit.core.model.UiState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val blockedAppsRepository: BlockedAppsRepository
) : AndroidViewModel(application) {

    private val _showAppPicker = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    /**
     * The unified UI state for the Settings screen.
     * We use [combine] to merge multiple data sources into a single reactive flow.
     */
    val uiState: StateFlow<UiState<SettingsUiContent>> = combine(
        blockedAppsRepository.getBlockedApps(),
        blockedAppsRepository.getSchedules(),
        _showAppPicker,
        _isLoading
    ) { blockedApps, schedules, showAppPicker, isLoading ->
        if (isLoading) {
            UiState.Loading
        } else {
            // We get installed apps on demand or fetch them once
            UiState.Success(
                SettingsUiContent(
                    installedApps = _installedApps.value,
                    blockedApps = blockedApps,
                    schedules = schedules,
                    showAppPicker = showAppPicker
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    init {
        getInstalledApps()
    }

    private fun getInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            // Move package manager heavy work to IO to avoid blocking main thread
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .asSequence()
                    .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                    .map {
                        AppInfo(
                            appName = it.loadLabel(pm).toString(),
                            packageName = it.packageName,
                            icon = it.loadIcon(pm)
                        )
                    }
                    .sortedBy { it.appName.lowercase() }
                    .toList()
            }
            _installedApps.value = apps
            _isLoading.value = false
        }
    }

    fun toggleBlockedApp(packageName: String) {
        viewModelScope.launch {
            val state = uiState.value
            if (state is UiState.Success) {
                val currentBlockedApps = state.data.blockedApps.toMutableSet()
                if (currentBlockedApps.contains(packageName)) {
                    currentBlockedApps.remove(packageName)
                } else {
                    currentBlockedApps.add(packageName)
                }
                blockedAppsRepository.saveBlockedApps(currentBlockedApps)
            }
        }
    }

    fun updateSchedule(schedule: DaySchedule) {
        viewModelScope.launch {
            val state = uiState.value
            if (state is UiState.Success) {
                val currentSchedules = state.data.schedules.toMutableList()
                val index = currentSchedules.indexOfFirst { it.dayOfWeek == schedule.dayOfWeek }
                if (index != -1) {
                    currentSchedules[index] = schedule
                    blockedAppsRepository.saveSchedules(currentSchedules)
                }
            }
        }
    }

    fun showAppPicker() {
        _showAppPicker.value = true
    }

    fun hideAppPicker() {
        _showAppPicker.value = false
    }
}

/**
 * Data class representing the content of the Settings screen.
 */
data class SettingsUiContent(
    val installedApps: List<AppInfo>,
    val blockedApps: Set<String>,
    val schedules: List<DaySchedule>,
    val showAppPicker: Boolean
)
