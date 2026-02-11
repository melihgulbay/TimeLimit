package com.example.timelimit.core.data.repository

import com.example.timelimit.core.data.BlockedAppsDataStore
import com.example.timelimit.core.model.DaySchedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppsRepository @Inject constructor(
    private val dataStore: BlockedAppsDataStore
) {

    fun getBlockedApps(): Flow<Set<String>> {
        return dataStore.getBlockedApps
    }

    suspend fun saveBlockedApps(blockedApps: Set<String>) {
        dataStore.saveBlockedApps(blockedApps)
    }

    fun getSchedules(): Flow<List<DaySchedule>> {
        return dataStore.getSchedules
    }

    suspend fun saveSchedules(schedules: List<DaySchedule>) {
        dataStore.saveSchedules(schedules)
    }
}

