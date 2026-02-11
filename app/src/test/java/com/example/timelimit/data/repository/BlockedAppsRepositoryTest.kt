package com.example.timelimit.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.timelimit.data.BlockedAppsDataStore
import com.example.timelimit.model.DaySchedule
import com.example.timelimit.model.TimeRange
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class BlockedAppsRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: BlockedAppsRepository
    private lateinit var dataStore: BlockedAppsDataStore

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        repository = BlockedAppsRepository(dataStore)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getBlockedApps should return flow from dataStore`() = runTest {
        // Given
        val testBlockedApps = setOf("com.example.app1", "com.example.app2", "com.example.app3")
        every { dataStore.getBlockedApps } returns flowOf(testBlockedApps)

        // When
        val result = repository.getBlockedApps()

        // Then
        result.test {
            val blockedApps = awaitItem()
            assertEquals(3, blockedApps.size)
            assertTrue(blockedApps.contains("com.example.app1"))
            assertTrue(blockedApps.contains("com.example.app2"))
            assertTrue(blockedApps.contains("com.example.app3"))
            awaitComplete()
        }
    }

    @Test
    fun `getBlockedApps should return empty set when no apps blocked`() = runTest {
        // Given
        every { dataStore.getBlockedApps } returns flowOf(emptySet())

        // When
        val result = repository.getBlockedApps()

        // Then
        result.test {
            val blockedApps = awaitItem()
            assertTrue(blockedApps.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `saveBlockedApps should call dataStore saveBlockedApps`() = runTest {
        // Given
        val blockedApps = setOf("com.example.app1", "com.example.app2")
        coEvery { dataStore.saveBlockedApps(any()) } just Runs

        // When
        repository.saveBlockedApps(blockedApps)

        // Then
        coVerify(exactly = 1) { dataStore.saveBlockedApps(blockedApps) }
    }

    @Test
    fun `saveBlockedApps should handle empty set`() = runTest {
        // Given
        val emptySet = emptySet<String>()
        coEvery { dataStore.saveBlockedApps(any()) } just Runs

        // When
        repository.saveBlockedApps(emptySet)

        // Then
        coVerify(exactly = 1) { dataStore.saveBlockedApps(emptySet) }
    }

    @Test
    fun `getSchedules should return flow from dataStore`() = runTest {
        // Given
        val testSchedules = listOf(
            DaySchedule(
                dayOfWeek = Calendar.MONDAY,
                isEnabled = true,
                timeRange = TimeRange(9, 0, 17, 0)
            ),
            DaySchedule(
                dayOfWeek = Calendar.TUESDAY,
                isEnabled = false,
                timeRange = TimeRange(10, 0, 18, 0)
            )
        )
        every { dataStore.getSchedules } returns flowOf(testSchedules)

        // When
        val result = repository.getSchedules()

        // Then
        result.test {
            val schedules = awaitItem()
            assertEquals(2, schedules.size)
            assertEquals(Calendar.MONDAY, schedules[0].dayOfWeek)
            assertTrue(schedules[0].isEnabled)
            assertEquals(Calendar.TUESDAY, schedules[1].dayOfWeek)
            awaitComplete()
        }
    }

    @Test
    fun `getSchedules should return empty list when no schedules configured`() = runTest {
        // Given
        every { dataStore.getSchedules } returns flowOf(emptyList())

        // When
        val result = repository.getSchedules()

        // Then
        result.test {
            val schedules = awaitItem()
            assertTrue(schedules.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `saveSchedules should call dataStore saveSchedules`() = runTest {
        // Given
        val schedules = listOf(
            DaySchedule(
                dayOfWeek = Calendar.WEDNESDAY,
                isEnabled = true,
                timeRange = TimeRange(8, 30, 16, 30)
            )
        )
        coEvery { dataStore.saveSchedules(any()) } just Runs

        // When
        repository.saveSchedules(schedules)

        // Then
        coVerify(exactly = 1) { dataStore.saveSchedules(schedules) }
    }

    @Test
    fun `saveSchedules should handle empty list`() = runTest {
        // Given
        val emptyList = emptyList<DaySchedule>()
        coEvery { dataStore.saveSchedules(any()) } just Runs

        // When
        repository.saveSchedules(emptyList)

        // Then
        coVerify(exactly = 1) { dataStore.saveSchedules(emptyList) }
    }

    @Test
    fun `repository should handle multiple schedule updates`() = runTest {
        // Given
        val firstSchedules = listOf(
            DaySchedule(Calendar.MONDAY, true, TimeRange(9, 0, 17, 0))
        )
        val secondSchedules = listOf(
            DaySchedule(Calendar.MONDAY, true, TimeRange(9, 0, 17, 0)),
            DaySchedule(Calendar.TUESDAY, true, TimeRange(9, 0, 17, 0))
        )
        coEvery { dataStore.saveSchedules(any()) } just Runs

        // When
        repository.saveSchedules(firstSchedules)
        repository.saveSchedules(secondSchedules)

        // Then
        coVerify(exactly = 1) { dataStore.saveSchedules(firstSchedules) }
        coVerify(exactly = 1) { dataStore.saveSchedules(secondSchedules) }
    }
}

