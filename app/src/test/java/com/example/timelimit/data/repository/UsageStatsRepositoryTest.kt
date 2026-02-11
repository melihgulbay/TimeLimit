package com.example.timelimit.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.timelimit.data.database.AppUsageDao
import com.example.timelimit.data.database.AppUsageEntity
import com.example.timelimit.data.database.AppUsageSummary
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class UsageStatsRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UsageStatsRepository
    private lateinit var appUsageDao: AppUsageDao
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Before
    fun setup() {
        appUsageDao = mockk(relaxed = true)
        repository = UsageStatsRepository(appUsageDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `recordUsage should insert usage entity with correct data`() = runTest {
        // Given
        val packageName = "com.example.app"
        val appName = "Test App"
        val usageTime = 60000L // 1 minute
        coEvery { appUsageDao.insertUsage(any()) } just Runs

        // When
        repository.recordUsage(packageName, appName, usageTime)

        // Then
        coVerify {
            appUsageDao.insertUsage(
                match { entity ->
                    entity.packageName == packageName &&
                    entity.appName == appName &&
                    entity.usageTimeMillis == usageTime &&
                    entity.date == dateFormat.format(Date())
                }
            )
        }
    }

    @Test
    fun `getTodayUsage should return flow from dao for today's date`() = runTest {
        // Given
        val today = dateFormat.format(Date())
        val testUsages = listOf(
            AppUsageEntity(
                id = 1,
                packageName = "com.example.app1",
                appName = "App 1",
                usageTimeMillis = 3600000L,
                timestamp = System.currentTimeMillis(),
                date = today
            )
        )
        every { appUsageDao.getUsageForDate(today) } returns flowOf(testUsages)

        // When
        val result = repository.getTodayUsage()

        // Then
        result.test {
            val usages = awaitItem()
            assertEquals(1, usages.size)
            assertEquals("App 1", usages[0].appName)
            awaitComplete()
        }
        verify { appUsageDao.getUsageForDate(today) }
    }

    @Test
    fun `getDailyUsageSummary should return summary for today`() = runTest {
        // Given
        val today = dateFormat.format(Date())
        val testSummary = listOf(
            AppUsageSummary(
                packageName = "com.example.app1",
                appName = "App 1",
                totalUsage = 7200000L
            )
        )
        every { appUsageDao.getDailyUsageSummary(today) } returns flowOf(testSummary)

        // When
        val result = repository.getDailyUsageSummary()

        // Then
        result.test {
            val summary = awaitItem()
            assertEquals(1, summary.size)
            assertEquals("App 1", summary[0].appName)
            assertEquals(7200000L, summary[0].totalUsage)
            awaitComplete()
        }
    }

    @Test
    fun `getWeeklyUsageSummary should return summary for last 7 days`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = dateFormat.format(calendar.time)

        val testSummary = listOf(
            AppUsageSummary(
                packageName = "com.example.app1",
                appName = "App 1",
                totalUsage = 25200000L // 7 hours
            )
        )
        every { appUsageDao.getWeeklyUsageSummary(any(), any()) } returns flowOf(testSummary)

        // When
        val result = repository.getWeeklyUsageSummary()

        // Then
        result.test {
            val summary = awaitItem()
            assertEquals(1, summary.size)
            assertEquals(25200000L, summary[0].totalUsage)
            awaitComplete()
        }
        verify { appUsageDao.getWeeklyUsageSummary(startDate, endDate) }
    }

    @Test
    fun `getUsageForDateRange should return usage for specified range`() = runTest {
        // Given
        val startDate = "2024-01-01"
        val endDate = "2024-01-07"
        val testUsages = listOf(
            AppUsageEntity(
                id = 1,
                packageName = "com.example.app1",
                appName = "App 1",
                usageTimeMillis = 3600000L,
                timestamp = System.currentTimeMillis(),
                date = "2024-01-05"
            )
        )
        every { appUsageDao.getUsageForDateRange(startDate, endDate) } returns flowOf(testUsages)

        // When
        val result = repository.getUsageForDateRange(startDate, endDate)

        // Then
        result.test {
            val usages = awaitItem()
            assertEquals(1, usages.size)
            awaitComplete()
        }
    }

    @Test
    fun `cleanOldData should delete data older than 30 days`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val expectedTimestamp = calendar.timeInMillis
        coEvery { appUsageDao.deleteOldUsage(any()) } just Runs

        // When
        repository.cleanOldData()

        // Then
        coVerify {
            appUsageDao.deleteOldUsage(
                match { timestamp ->
                    // Allow 1 second tolerance for timing differences
                    Math.abs(timestamp - expectedTimestamp) < 1000
                }
            )
        }
    }

    @Test
    fun `recordUsage should handle multiple consecutive records`() = runTest {
        // Given
        coEvery { appUsageDao.insertUsage(any()) } just Runs

        // When
        repository.recordUsage("com.app1", "App 1", 1000L)
        repository.recordUsage("com.app2", "App 2", 2000L)
        repository.recordUsage("com.app3", "App 3", 3000L)

        // Then
        coVerify(exactly = 3) { appUsageDao.insertUsage(any()) }
    }

    @Test
    fun `getTodayUsage should return empty list when no data`() = runTest {
        // Given
        val today = dateFormat.format(Date())
        every { appUsageDao.getUsageForDate(today) } returns flowOf(emptyList())

        // When
        val result = repository.getTodayUsage()

        // Then
        result.test {
            val usages = awaitItem()
            assertTrue(usages.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getDailyUsageSummary should return empty list when no data`() = runTest {
        // Given
        val today = dateFormat.format(Date())
        every { appUsageDao.getDailyUsageSummary(today) } returns flowOf(emptyList())

        // When
        val result = repository.getDailyUsageSummary()

        // Then
        result.test {
            val summary = awaitItem()
            assertTrue(summary.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getWeeklyUsageSummary should return empty list when no data`() = runTest {
        // Given
        every { appUsageDao.getWeeklyUsageSummary(any(), any()) } returns flowOf(emptyList())

        // When
        val result = repository.getWeeklyUsageSummary()

        // Then
        result.test {
            val summary = awaitItem()
            assertTrue(summary.isEmpty())
            awaitComplete()
        }
    }
}

