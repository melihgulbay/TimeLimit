package com.example.timelimit.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppUsageEntity::class, QuoteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TimeLimitDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun quoteDao(): QuoteDao
}

