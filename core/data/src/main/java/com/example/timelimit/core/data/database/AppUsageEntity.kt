package com.example.timelimit.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val timestamp: Long,
    val date: String // Format: yyyy-MM-dd
)

