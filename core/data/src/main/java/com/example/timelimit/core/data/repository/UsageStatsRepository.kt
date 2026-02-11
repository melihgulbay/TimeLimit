package com.example.timelimit.core.data.repository

import com.example.timelimit.core.data.database.AppUsageDao
import com.example.timelimit.core.data.database.AppUsageEntity
import com.example.timelimit.core.data.database.AppUsageSummary
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsRepository @Inject constructor(
    private val appUsageDao: AppUsageDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun recordUsage(packageName: String, appName: String, usageTimeMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val date = dateFormat.format(Date(currentTime))

        val usage = AppUsageEntity(
            packageName = packageName,
            appName = appName,
            usageTimeMillis = usageTimeMillis,
            timestamp = currentTime,
            date = date
        )

        appUsageDao.insertUsage(usage)
    }

    fun getTodayUsage(): Flow<List<AppUsageEntity>> {
        val today = dateFormat.format(Date())
        return appUsageDao.getUsageForDate(today)
    }

    fun getDailyUsageSummary(): Flow<List<AppUsageSummary>> {
        val today = dateFormat.format(Date())
        return appUsageDao.getDailyUsageSummary(today)
    }

    fun getWeeklyUsageSummary(): Flow<List<AppUsageSummary>> {
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = dateFormat.format(calendar.time)

        return appUsageDao.getWeeklyUsageSummary(startDate, endDate)
    }

    fun getUsageForDateRange(startDate: String, endDate: String): Flow<List<AppUsageEntity>> {
        return appUsageDao.getUsageForDateRange(startDate, endDate)
    }

    suspend fun cleanOldData() {
        // Delete data older than 30 days
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        appUsageDao.deleteOldUsage(calendar.timeInMillis)
    }
}

