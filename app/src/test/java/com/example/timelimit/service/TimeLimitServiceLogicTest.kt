package com.example.timelimit.service

import com.example.timelimit.model.DaySchedule
import com.example.timelimit.model.TimeRange
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.util.*

class TimeLimitServiceLogicTest {

    /**
     * Helper function to test time range logic
     * Extracted from TimeLimitService for unit testing
     */
    private fun isWithinTimeRange(schedule: DaySchedule, hour: Int, minute: Int): Boolean {
        val startTime = schedule.timeRange.startHour * 60 + schedule.timeRange.startMinute
        val endTime = schedule.timeRange.endHour * 60 + schedule.timeRange.endMinute
        val currentTime = hour * 60 + minute

        return if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            currentTime >= startTime || currentTime <= endTime
        }
    }

    @Test
    fun `isWithinTimeRange should return true when time is within range`() {
        // Given - Schedule from 9:00 AM to 5:00 PM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // Then - 12:30 PM should be within range
        assertTrue(isWithinTimeRange(schedule, 12, 30))
    }

    @Test
    fun `isWithinTimeRange should return false when time is before range`() {
        // Given - Schedule from 9:00 AM to 5:00 PM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // Then - 8:00 AM should be outside range
        assertFalse(isWithinTimeRange(schedule, 8, 0))
    }

    @Test
    fun `isWithinTimeRange should return false when time is after range`() {
        // Given - Schedule from 9:00 AM to 5:00 PM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // Then - 6:00 PM should be outside range
        assertFalse(isWithinTimeRange(schedule, 18, 0))
    }

    @Test
    fun `isWithinTimeRange should return true at start time`() {
        // Given - Schedule from 9:00 AM to 5:00 PM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // Then - 9:00 AM should be within range
        assertTrue(isWithinTimeRange(schedule, 9, 0))
    }

    @Test
    fun `isWithinTimeRange should return true at end time`() {
        // Given - Schedule from 9:00 AM to 5:00 PM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // Then - 5:00 PM should be within range
        assertTrue(isWithinTimeRange(schedule, 17, 0))
    }

    @Test
    fun `isWithinTimeRange should handle overnight schedules - before midnight`() {
        // Given - Schedule from 10:00 PM to 6:00 AM (overnight)
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(22, 0, 6, 0)
        )

        // Then - 11:00 PM should be within range
        assertTrue(isWithinTimeRange(schedule, 23, 0))
    }

    @Test
    fun `isWithinTimeRange should handle overnight schedules - after midnight`() {
        // Given - Schedule from 10:00 PM to 6:00 AM (overnight)
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(22, 0, 6, 0)
        )

        // Then - 3:00 AM should be within range
        assertTrue(isWithinTimeRange(schedule, 3, 0))
    }

    @Test
    fun `isWithinTimeRange should handle overnight schedules - outside range`() {
        // Given - Schedule from 10:00 PM to 6:00 AM (overnight)
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(22, 0, 6, 0)
        )

        // Then - 12:00 PM should be outside range
        assertFalse(isWithinTimeRange(schedule, 12, 0))
    }

    @Test
    fun `isWithinTimeRange should handle full day schedule`() {
        // Given - Schedule from 0:00 to 23:59
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(0, 0, 23, 59)
        )

        // Then - Any time should be within range
        assertTrue(isWithinTimeRange(schedule, 0, 0))
        assertTrue(isWithinTimeRange(schedule, 12, 0))
        assertTrue(isWithinTimeRange(schedule, 23, 59))
    }

    @Test
    fun `isWithinTimeRange should handle minute precision`() {
        // Given - Schedule from 9:15 to 17:45
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 15, 17, 45)
        )

        // Then
        assertFalse(isWithinTimeRange(schedule, 9, 14)) // Just before
        assertTrue(isWithinTimeRange(schedule, 9, 15)) // Exactly at start
        assertTrue(isWithinTimeRange(schedule, 9, 16)) // Just after start
        assertTrue(isWithinTimeRange(schedule, 17, 44)) // Just before end
        assertTrue(isWithinTimeRange(schedule, 17, 45)) // Exactly at end
        assertFalse(isWithinTimeRange(schedule, 17, 46)) // Just after end
    }

    @Test
    fun `isWithinTimeRange should handle edge case - same start and end time`() {
        // Given - Schedule from 9:00 to 9:00 (represents full 24 hours or no time)
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 9, 0)
        )

        // Then - Only 9:00 should be within range in this implementation
        assertTrue(isWithinTimeRange(schedule, 9, 0))
        assertFalse(isWithinTimeRange(schedule, 10, 0))
        assertFalse(isWithinTimeRange(schedule, 8, 0))
    }

    @Test
    fun `isWithinTimeRange should handle early morning schedule`() {
        // Given - Schedule from 6:00 AM to 8:00 AM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(6, 0, 8, 0)
        )

        // Then
        assertFalse(isWithinTimeRange(schedule, 5, 59))
        assertTrue(isWithinTimeRange(schedule, 6, 0))
        assertTrue(isWithinTimeRange(schedule, 7, 0))
        assertTrue(isWithinTimeRange(schedule, 8, 0))
        assertFalse(isWithinTimeRange(schedule, 8, 1))
    }

    @Test
    fun `isWithinTimeRange should handle late night schedule`() {
        // Given - Schedule from 9:00 PM to 11:59 PM
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(21, 0, 23, 59)
        )

        // Then
        assertFalse(isWithinTimeRange(schedule, 20, 59))
        assertTrue(isWithinTimeRange(schedule, 21, 0))
        assertTrue(isWithinTimeRange(schedule, 22, 30))
        assertTrue(isWithinTimeRange(schedule, 23, 59))
        assertFalse(isWithinTimeRange(schedule, 0, 0))
    }
}

