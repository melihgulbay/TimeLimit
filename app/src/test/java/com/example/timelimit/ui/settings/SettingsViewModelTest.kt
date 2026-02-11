package com.example.timelimit.ui.settings

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.timelimit.data.repository.BlockedAppsRepository
import com.example.timelimit.model.AppInfo
import com.example.timelimit.model.DaySchedule
import com.example.timelimit.model.TimeRange
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var blockedAppsRepository: BlockedAppsRepository
    private lateinit var application: Application

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Application and PackageManager
        application = mockk(relaxed = true)

        // Mock Repository
        blockedAppsRepository = mockk(relaxed = true)

        // Setup default repository returns
        every { blockedAppsRepository.getBlockedApps() } returns flowOf(emptySet())
        every { blockedAppsRepository.getSchedules() } returns flowOf(emptyList())
        coEvery { blockedAppsRepository.saveBlockedApps(any()) } just Runs
        coEvery { blockedAppsRepository.saveSchedules(any()) } just Runs

        viewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state should have empty blocked apps`() = runTest {
        viewModel.blockedApps.test {
            val blockedApps = awaitItem()
            assertTrue(blockedApps.isEmpty())
        }
    }

    @Test
    fun `initial state should have empty schedules`() = runTest {
        viewModel.schedules.test {
            val schedules = awaitItem()
            assertTrue(schedules.isEmpty())
        }
    }

    @Test
    fun `showAppPicker should be false initially`() = runTest {
        viewModel.showAppPicker.test {
            val showPicker = awaitItem()
            assertFalse(showPicker)
        }
    }

    @Test
    fun `getBlockedApps should update blockedApps state`() = runTest {
        // Given
        val testBlockedApps = setOf("com.example.app1", "com.example.app2")
        every { blockedAppsRepository.getBlockedApps() } returns flowOf(testBlockedApps)

        // When
        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        newViewModel.blockedApps.test {
            val blockedApps = awaitItem()
            assertEquals(2, blockedApps.size)
            assertTrue(blockedApps.contains("com.example.app1"))
            assertTrue(blockedApps.contains("com.example.app2"))
        }
    }

    @Test
    fun `getSchedules should update schedules state`() = runTest {
        // Given
        val testSchedules = listOf(
            DaySchedule(
                dayOfWeek = Calendar.MONDAY,
                isEnabled = true,
                timeRange = TimeRange(9, 0, 17, 0)
            )
        )
        every { blockedAppsRepository.getSchedules() } returns flowOf(testSchedules)

        // When
        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        newViewModel.schedules.test {
            val schedules = awaitItem()
            assertEquals(1, schedules.size)
            assertEquals(Calendar.MONDAY, schedules[0].dayOfWeek)
            assertTrue(schedules[0].isEnabled)
        }
    }

    @Test
    fun `toggleBlockedApp should add app when not blocked`() = runTest {
        // Given
        val packageName = "com.example.newapp"
        val currentBlocked = setOf("com.example.app1")
        every { blockedAppsRepository.getBlockedApps() } returns flowOf(currentBlocked)

        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        newViewModel.toggleBlockedApp(packageName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            blockedAppsRepository.saveBlockedApps(
                match { it.contains(packageName) && it.contains("com.example.app1") }
            )
        }
    }

    @Test
    fun `toggleBlockedApp should remove app when already blocked`() = runTest {
        // Given
        val packageName = "com.example.app1"
        val currentBlocked = setOf(packageName, "com.example.app2")
        every { blockedAppsRepository.getBlockedApps() } returns flowOf(currentBlocked)

        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        newViewModel.toggleBlockedApp(packageName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            blockedAppsRepository.saveBlockedApps(
                match { !it.contains(packageName) && it.contains("com.example.app2") }
            )
        }
    }

    @Test
    fun `updateSchedule should update existing schedule`() = runTest {
        // Given
        val existingSchedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = false,
            timeRange = TimeRange(9, 0, 17, 0)
        )
        val updatedSchedule = existingSchedule.copy(isEnabled = true)

        every { blockedAppsRepository.getSchedules() } returns flowOf(listOf(existingSchedule))

        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        newViewModel.updateSchedule(updatedSchedule)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            blockedAppsRepository.saveSchedules(
                match { list ->
                    list.size == 1 && list[0].isEnabled
                }
            )
        }
    }

    @Test
    fun `updateSchedule should not save if schedule not found`() = runTest {
        // Given
        val nonExistentSchedule = DaySchedule(
            dayOfWeek = Calendar.TUESDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        every { blockedAppsRepository.getSchedules() } returns flowOf(emptyList())

        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Reset mock to clear initialization calls
        clearMocks(blockedAppsRepository, answers = false)

        // When
        newViewModel.updateSchedule(nonExistentSchedule)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { blockedAppsRepository.saveSchedules(any()) }
    }

    @Test
    fun `showAppPicker should set showAppPicker to true`() = runTest {
        // When
        viewModel.showAppPicker()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.showAppPicker.test {
            val showPicker = awaitItem()
            assertTrue(showPicker)
        }
    }

    @Test
    fun `hideAppPicker should set showAppPicker to false`() = runTest {
        // Given
        viewModel.showAppPicker()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.hideAppPicker()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.showAppPicker.test {
            val showPicker = awaitItem()
            assertFalse(showPicker)
        }
    }

    @Test
    fun `multiple toggles should correctly update blocked apps`() = runTest {
        // Given
        val app1 = "com.example.app1"
        val app2 = "com.example.app2"
        every { blockedAppsRepository.getBlockedApps() } returns flowOf(emptySet())

        val newViewModel = SettingsViewModel(application, blockedAppsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - Add app1
        newViewModel.toggleBlockedApp(app1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Update the flow to reflect the change
        every { blockedAppsRepository.getBlockedApps() } returns flowOf(setOf(app1))

        // When - Add app2
        newViewModel.toggleBlockedApp(app2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Both should be saved
        coVerify {
            blockedAppsRepository.saveBlockedApps(match { it.contains(app1) })
            blockedAppsRepository.saveBlockedApps(match { it.contains(app2) })
        }
    }
}

