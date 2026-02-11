package com.example.timelimit.core.service

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder

import com.example.timelimit.core.data.repository.BlockedAppsRepository
import com.example.timelimit.core.data.repository.UsageStatsRepository
import com.example.timelimit.core.model.DaySchedule
import com.example.timelimit.core.service.util.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class TimeLimitService : Service() {

    @Inject
    lateinit var blockedAppsRepository: BlockedAppsRepository

    @Inject
    lateinit var usageStatsRepository: UsageStatsRepository

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // Cache latest values from DataStore so the hot loop doesn't re-query the datastore every iteration
    @Volatile
    private var cachedBlockedApps: Set<String> = emptySet()

    @Volatile
    private var cachedSchedules: List<DaySchedule> = emptyList()

    private var lastTrackedApp: String? = null
    private var lastTrackTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationUtil.createNotificationChannel(this)
        startForeground(1, NotificationUtil.createNotification(this, "Time Limit is running"))

        // Start collectors to keep local caches up to date instead of calling `.first()` in the loop
        scope.launch {
            blockedAppsRepository.getBlockedApps().collect { set ->
                cachedBlockedApps = set
            }
        }

        scope.launch {
            blockedAppsRepository.getSchedules().collect { list ->
                cachedSchedules = list
            }
        }

        scope.launch {
            while (true) {
                val foregroundApp = getForegroundApp()

                // Track usage statistics
                if (foregroundApp != null) {
                    trackAppUsage(foregroundApp)
                }

                val calendar = Calendar.getInstance()
                val today = cachedSchedules.find { it.dayOfWeek == calendar.get(Calendar.DAY_OF_WEEK) }

                if (foregroundApp != null && today != null && today.isEnabled && cachedBlockedApps.contains(foregroundApp) && isWithinTimeRange(today)) {
                    openTimeLimitApp()
                }
                delay(1000L)
            }
        }

        return START_STICKY
    }

    private fun trackAppUsage(packageName: String) {
        scope.launch {
            val currentTime = System.currentTimeMillis()

            // If it's a new app or enough time has passed, record the usage
            if (lastTrackedApp != packageName) {
                // Save previous app usage if exists
                if (lastTrackedApp != null && lastTrackTime > 0) {
                    val usageTime = currentTime - lastTrackTime
                    if (usageTime > 0) {
                        val appName = getAppName(lastTrackedApp!!)
                        usageStatsRepository.recordUsage(lastTrackedApp!!, appName, usageTime)
                    }
                }
                lastTrackedApp = packageName
                lastTrackTime = currentTime
            }
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getForegroundApp(): String? {
        // Use the type-safe getSystemService overload to avoid string constants and remove warnings
        val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return null
        val time = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10_000L, time) ?: emptyList()
        return usageStats.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun openTimeLimitApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun isWithinTimeRange(schedule: DaySchedule): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val startTime = schedule.timeRange.startHour * 60 + schedule.timeRange.startMinute
        val endTime = schedule.timeRange.endHour * 60 + schedule.timeRange.endMinute
        val currentTime = currentHour * 60 + currentMinute

        return if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            currentTime >= startTime || currentTime <= endTime
        }
    }
}
