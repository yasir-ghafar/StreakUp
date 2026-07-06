package com.techlad.streakup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.techlad.streakup.data.local.dao.CheckInDao
import com.techlad.streakup.data.local.dao.HabitDao
import com.techlad.streakup.data.local.dao.UserSettingsDao
import com.techlad.streakup.data.local.entity.CheckInEntity
import com.techlad.streakup.data.local.entity.HabitEntity
import com.techlad.streakup.data.local.entity.UserSettingsEntity

@Database(
    entities = [HabitEntity::class, CheckInEntity::class, UserSettingsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StreakUpDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao
    abstract fun userSettingsDao(): UserSettingsDao
}
