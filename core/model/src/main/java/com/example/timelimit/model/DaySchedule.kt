package com.example.timelimit.core.model

import java.util.Calendar

data class DaySchedule(
    val dayOfWeek: Int = Calendar.MONDAY,
    val isEnabled: Boolean = true,
    val timeRange: TimeRange = TimeRange()
)
