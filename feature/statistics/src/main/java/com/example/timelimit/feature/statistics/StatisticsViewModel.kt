package com.example.timelimit.feature.statistics

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timelimit.core.data.database.AppUsageSummary
import com.example.timelimit.core.data.repository.UsageStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.timelimit.core.model.UiState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    application: Application,
    private val usageStatsRepository: UsageStatsRepository
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    private val _selectedTab = MutableStateFlow(0)

    /**
     * Unified UI state for the Statistics screen.
     */
    val uiState: StateFlow<UiState<StatisticsUiContent>> = combine(
        usageStatsRepository.getDailyUsageSummary(),
        usageStatsRepository.getWeeklyUsageSummary(),
        _selectedTab,
        _isLoading
    ) { dailySummaries, weeklySummaries, selectedTab, isLoading ->
        if (isLoading && dailySummaries.isEmpty() && weeklySummaries.isEmpty()) {
            UiState.Loading
        } else {
            UiState.Success(
                StatisticsUiContent(
                    dailyUsage = dailySummaries.map { summary ->
                        UsageStatItem(
                            appName = summary.appName,
                            packageName = summary.packageName,
                            usageTimeMillis = summary.totalUsage,
                            icon = getAppIcon(summary.packageName)
                        )
                    },
                    weeklyUsage = weeklySummaries.map { summary ->
                        UsageStatItem(
                            appName = summary.appName,
                            packageName = summary.packageName,
                            usageTimeMillis = summary.totalUsage,
                            icon = getAppIcon(summary.packageName)
                        )
                    },
                    selectedTab = selectedTab
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun refreshStats() {
        _isLoading.value = true
        // The repository flows will emit new values automatically if the underlying data changes,
        // but it's good practice to provide a manual refresh trigger.
        viewModelScope.launch {
            // Simulate loading or wait for repository refresh if applicable
            _isLoading.value = false
        }
    }

    private fun getAppIcon(packageName: String): android.graphics.drawable.Drawable? {
        return try {
            getApplication<Application>().packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}

/**
 * Data class representing the content of the Statistics screen.
 */
data class StatisticsUiContent(
    val dailyUsage: List<UsageStatItem>,
    val weeklyUsage: List<UsageStatItem>,
    val selectedTab: Int
)

data class UsageStatItem(
    val appName: String,
    val packageName: String,
    val usageTimeMillis: Long,
    val icon: android.graphics.drawable.Drawable?
) {
    val usageTimeFormatted: String
        get() {
            val hours = TimeUnit.MILLISECONDS.toHours(usageTimeMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(usageTimeMillis) % 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }
}

