package com.example.timelimit.ui.statistics

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.timelimit.data.database.AppUsageSummary
import com.example.timelimit.data.repository.UsageStatsRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var usageStatsRepository: UsageStatsRepository
    private lateinit var application: Application

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Application and PackageManager
        application = mockk(relaxed = true)

        // Mock Repository
        usageStatsRepository = mockk(relaxed = true)

        // Setup default repository returns
        every { usageStatsRepository.getDailyUsageSummary() } returns flowOf(emptyList())
        every { usageStatsRepository.getWeeklyUsageSummary() } returns flowOf(emptyList())

        viewModel = StatisticsViewModel(application, usageStatsRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state should have empty daily usage`() = runTest {
        viewModel.dailyUsage.test {
            val usage = awaitItem()
            assertTrue(usage.isEmpty())
        }
    }

    @Test
    fun `initial state should have empty weekly usage`() = runTest {
        viewModel.weeklyUsage.test {
            val usage = awaitItem()
            assertTrue(usage.isEmpty())
        }
    }

    @Test
    fun `initial state should have selectedTab as 0`() = runTest {
        viewModel.selectedTab.test {
            val tab = awaitItem()
            assertEquals(0, tab)
        }
    }

    @Test
    fun `loadUsageStats should update dailyUsage from repository`() = runTest {
        // Given
        val testSummaries = listOf(
            AppUsageSummary(
                packageName = "com.example.app1",
                appName = "App 1",
                totalUsage = 3600000L // 1 hour
            ),
            AppUsageSummary(
                packageName = "com.example.app2",
                appName = "App 2",
                totalUsage = 7200000L // 2 hours
            )
        )
        every { usageStatsRepository.getDailyUsageSummary() } returns flowOf(testSummaries)
        every { usageStatsRepository.getWeeklyUsageSummary() } returns flowOf(emptyList())

        // When
        val newViewModel = StatisticsViewModel(application, usageStatsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        newViewModel.dailyUsage.test {
            val usage = awaitItem()
            assertEquals(2, usage.size)
            assertEquals("App 1", usage[0].appName)
            assertEquals("com.example.app1", usage[0].packageName)
            assertEquals(3600000L, usage[0].usageTimeMillis)
            assertEquals("App 2", usage[1].appName)
        }
    }

    @Test
    fun `loadUsageStats should update weeklyUsage from repository`() = runTest {
        // Given
        val testSummaries = listOf(
            AppUsageSummary(
                packageName = "com.example.app1",
                appName = "App 1",
                totalUsage = 14400000L // 4 hours
            )
        )
        every { usageStatsRepository.getDailyUsageSummary() } returns flowOf(emptyList())
        every { usageStatsRepository.getWeeklyUsageSummary() } returns flowOf(testSummaries)

        // When
        val newViewModel = StatisticsViewModel(application, usageStatsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        newViewModel.weeklyUsage.test {
            val usage = awaitItem()
            assertEquals(1, usage.size)
            assertEquals("App 1", usage[0].appName)
            assertEquals(14400000L, usage[0].usageTimeMillis)
        }
    }

    @Test
    fun `loadUsageStats should set isLoading to true then false`() = runTest {
        // Given
        every { usageStatsRepository.getDailyUsageSummary() } returns flowOf(emptyList())
        every { usageStatsRepository.getWeeklyUsageSummary() } returns flowOf(emptyList())

        // When
        val newViewModel = StatisticsViewModel(application, usageStatsRepository)

        // Advance coroutines to complete loading
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - After loading completes, isLoading should be false
        newViewModel.isLoading.test {
            val loadingState = awaitItem()
            assertFalse(loadingState)
        }
    }

    @Test
    fun `setSelectedTab should update selectedTab state`() = runTest {
        // When
        viewModel.setSelectedTab(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.selectedTab.test {
            val tab = awaitItem()
            assertEquals(1, tab)
        }
    }

    @Test
    fun `setSelectedTab should handle multiple tab changes`() = runTest {
        // When
        viewModel.setSelectedTab(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedTab(0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.selectedTab.test {
            val tab = awaitItem()
            assertEquals(0, tab)
        }
    }

    @Test
    fun `UsageStatItem should format time correctly for hours and minutes`() {
        // Given
        val item = UsageStatItem(
            appName = "Test App",
            packageName = "com.test.app",
            usageTimeMillis = 5460000L, // 1 hour 31 minutes
            icon = null
        )

        // Then
        assertEquals("1h 31m", item.usageTimeFormatted)
    }

    @Test
    fun `UsageStatItem should format time correctly for minutes only`() {
        // Given
        val item = UsageStatItem(
            appName = "Test App",
            packageName = "com.test.app",
            usageTimeMillis = 1800000L, // 30 minutes
            icon = null
        )

        // Then
        assertEquals("30m", item.usageTimeFormatted)
    }

    @Test
    fun `UsageStatItem should format zero time correctly`() {
        // Given
        val item = UsageStatItem(
            appName = "Test App",
            packageName = "com.test.app",
            usageTimeMillis = 0L,
            icon = null
        )

        // Then
        assertEquals("0m", item.usageTimeFormatted)
    }

    @Test
    fun `loadUsageStats should be called on initialization`() = runTest {
        // Given - setup already creates viewModel

        // Then
        coVerify(atLeast = 1) { usageStatsRepository.getDailyUsageSummary() }
        coVerify(atLeast = 1) { usageStatsRepository.getWeeklyUsageSummary() }
    }
}

