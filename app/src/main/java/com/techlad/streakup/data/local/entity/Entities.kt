package com.techlad.streakup.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val name: String,
    val icon: String,
    val color: String,
    val frequencyType: String,
    val frequencyTarget: Int,
    val sortOrder: Int,
    val reminderTime: String?,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean,
)

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId"), Index(value = ["habitId", "checkedDate"], unique = true)],
)
data class CheckInEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val userId: String?,
    val checkedDate: String,
    val createdAt: Long,
    val isSynced: Boolean,
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val userId: String,
    val notificationsEnabled: Boolean,
    val dailySummaryEnabled: Boolean,
    val dailySummaryTime: String,
    val theme: String,
    val isGuest: Boolean,
)
