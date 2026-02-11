package com.example.timelimit.model

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import java.util.*

class ModelTest {

    @Test
    fun `TimeRange should create correct instance`() {
        // Given
        val timeRange = TimeRange(9, 30, 17, 45)

        // Then
        assertEquals(9, timeRange.startHour)
        assertEquals(30, timeRange.startMinute)
        assertEquals(17, timeRange.endHour)
        assertEquals(45, timeRange.endMinute)
    }

    @Test
    fun `TimeRange should support copy with modifications`() {
        // Given
        val original = TimeRange(9, 0, 17, 0)

        // When
        val modified = original.copy(endHour = 18)

        // Then
        assertEquals(9, modified.startHour)
        assertEquals(18, modified.endHour)
        assertNotEquals(original, modified)
    }

    @Test
    fun `TimeRange equality should work correctly`() {
        // Given
        val range1 = TimeRange(9, 0, 17, 0)
        val range2 = TimeRange(9, 0, 17, 0)
        val range3 = TimeRange(10, 0, 17, 0)

        // Then
        assertEquals(range1, range2)
        assertNotEquals(range1, range3)
    }

    @Test
    fun `DaySchedule should create correct instance`() {
        // Given
        val timeRange = TimeRange(9, 0, 17, 0)
        val schedule = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = timeRange
        )

        // Then
        assertEquals(Calendar.MONDAY, schedule.dayOfWeek)
        assertTrue(schedule.isEnabled)
        assertEquals(timeRange, schedule.timeRange)
    }

    @Test
    fun `DaySchedule should support copy with modifications`() {
        // Given
        val original = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = false,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // When
        val modified = original.copy(isEnabled = true)

        // Then
        assertFalse(original.isEnabled)
        assertTrue(modified.isEnabled)
        assertEquals(original.dayOfWeek, modified.dayOfWeek)
    }

    @Test
    fun `DaySchedule should support changing time range`() {
        // Given
        val original = DaySchedule(
            dayOfWeek = Calendar.TUESDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // When
        val newTimeRange = TimeRange(10, 0, 18, 0)
        val modified = original.copy(timeRange = newTimeRange)

        // Then
        assertEquals(10, modified.timeRange.startHour)
        assertEquals(18, modified.timeRange.endHour)
    }

    @Test
    fun `DaySchedule equality should work correctly`() {
        // Given
        val schedule1 = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )
        val schedule2 = DaySchedule(
            dayOfWeek = Calendar.MONDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )
        val schedule3 = DaySchedule(
            dayOfWeek = Calendar.TUESDAY,
            isEnabled = true,
            timeRange = TimeRange(9, 0, 17, 0)
        )

        // Then
        assertEquals(schedule1, schedule2)
        assertNotEquals(schedule1, schedule3)
    }

    @Test
    fun `DaySchedule should handle all days of week`() {
        // When/Then
        val days = listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )

        days.forEach { day ->
            val schedule = DaySchedule(
                dayOfWeek = day,
                isEnabled = true,
                timeRange = TimeRange(9, 0, 17, 0)
            )
            assertEquals(day, schedule.dayOfWeek)
        }
    }

    @Test
    fun `AppInfo should create correct instance`() {
        // Given/When
        val appInfo = AppInfo(
            appName = "Test App",
            packageName = "com.test.app",
            icon = null
        )

        // Then
        assertEquals("Test App", appInfo.appName)
        assertEquals("com.test.app", appInfo.packageName)
        assertEquals(null, appInfo.icon)
    }

    @Test
    fun `AppInfo equality should work correctly`() {
        // Given
        val app1 = AppInfo("App 1", "com.app1", null)
        val app2 = AppInfo("App 1", "com.app1", null)
        val app3 = AppInfo("App 2", "com.app2", null)

        // Then
        assertEquals(app1, app2)
        assertNotEquals(app1, app3)
    }

    @Test
    fun `TimeRange should validate time boundaries`() {
        // Valid ranges
        val validRange1 = TimeRange(0, 0, 23, 59)
        assertEquals(0, validRange1.startHour)
        assertEquals(23, validRange1.endHour)

        val validRange2 = TimeRange(12, 30, 13, 45)
        assertEquals(12, validRange2.startHour)
        assertEquals(13, validRange2.endHour)
    }

    @Test
    fun `TimeRange should handle overnight ranges`() {
        // Given - overnight range (22:00 to 06:00)
        val overnightRange = TimeRange(22, 0, 6, 0)

        // Then
        assertEquals(22, overnightRange.startHour)
        assertEquals(6, overnightRange.endHour)
        assertTrue(overnightRange.startHour > overnightRange.endHour)
    }

    @Test
    fun `DaySchedule should handle disabled schedules`() {
        // Given
        val disabledSchedule = DaySchedule(
            dayOfWeek = Calendar.SATURDAY,
            isEnabled = false,
            timeRange = TimeRange(0, 0, 0, 0)
        )

        // Then
        assertFalse(disabledSchedule.isEnabled)
    }

    @Test
    fun `TimeRange should support 24-hour format`() {
        // Midnight
        val midnight = TimeRange(0, 0, 0, 0)
        assertEquals(0, midnight.startHour)
        assertEquals(0, midnight.startMinute)

        // End of day
        val endOfDay = TimeRange(23, 59, 23, 59)
        assertEquals(23, endOfDay.startHour)
        assertEquals(59, endOfDay.startMinute)
    }
}

