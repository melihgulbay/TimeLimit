package com.example.timelimit.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Insert
    suspend fun insertUsage(usage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY timestamp DESC")
    fun getUsageForDate(date: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getUsageForDateRange(startDate: String, endDate: String): Flow<List<AppUsageEntity>>

    @Query("SELECT packageName, appName, SUM(usageTimeMillis) as totalUsage FROM app_usage WHERE date = :date GROUP BY packageName ORDER BY totalUsage DESC")
    fun getDailyUsageSummary(date: String): Flow<List<AppUsageSummary>>

    @Query("SELECT packageName, appName, SUM(usageTimeMillis) as totalUsage FROM app_usage WHERE date BETWEEN :startDate AND :endDate GROUP BY packageName ORDER BY totalUsage DESC")
    fun getWeeklyUsageSummary(startDate: String, endDate: String): Flow<List<AppUsageSummary>>

    @Query("DELETE FROM app_usage WHERE timestamp < :timestamp")
    suspend fun deleteOldUsage(timestamp: Long)
}

data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val totalUsage: Long
)

