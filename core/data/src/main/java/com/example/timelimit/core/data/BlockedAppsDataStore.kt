package com.example.timelimit.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.timelimit.core.model.DaySchedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class BlockedAppsDataStore(private val context: Context) {

    private val blockedAppsKey = stringSetPreferencesKey("blocked_apps")
    private val schedulesKey = stringPreferencesKey("schedules")

    // Use a single shared Gson instance to avoid unnecessary allocations when creating multiple
    // instances of this data store.
    companion object {
        private val gson: Gson = Gson()
    }

    val getBlockedApps: Flow<Set<String>> = context.dataStore.data
        .map {
            it[blockedAppsKey] ?: emptySet()
        }

    suspend fun saveBlockedApps(blockedApps: Set<String>) {
        context.dataStore.edit {
            it[blockedAppsKey] = blockedApps
        }
    }

    val getSchedules: Flow<List<DaySchedule>> = context.dataStore.data
        .map {
            val json = it[schedulesKey]
            if (json == null) {
                getDefaultSchedules()
            } else {
                val type = object : TypeToken<List<DaySchedule>>() {}.type
                gson.fromJson(json, type)
            }
        }

    suspend fun saveSchedules(schedules: List<DaySchedule>) {
        val json = gson.toJson(schedules)
        context.dataStore.edit {
            it[schedulesKey] = json
        }
    }

    private fun getDefaultSchedules(): List<DaySchedule> {
        return listOf(
            DaySchedule(dayOfWeek = Calendar.MONDAY),
            DaySchedule(dayOfWeek = Calendar.TUESDAY),
            DaySchedule(dayOfWeek = Calendar.WEDNESDAY),
            DaySchedule(dayOfWeek = Calendar.THURSDAY),
            DaySchedule(dayOfWeek = Calendar.FRIDAY),
            DaySchedule(dayOfWeek = Calendar.SATURDAY, isEnabled = false),
            DaySchedule(dayOfWeek = Calendar.SUNDAY, isEnabled = false)
        )
    }
}
