package com.techlad.streakup.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

enum class FrequencyType { DAILY, WEEKLY }

enum class AppTheme { LIGHT, DARK, SYSTEM }

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val name: String,
    val icon: String = "✅",
    val color: String = "#5DCAA5",
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyTarget: Int = 1,
    val sortOrder: Int = 0,
    val reminderTime: LocalTime? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
)

data class CheckIn(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val userId: String? = null,
    val checkedDate: LocalDate,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
)

data class HabitWithStatus(
    val habit: Habit,
    val isCheckedToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyProgress: Int = 0,
)

data class UserSettings(
    val userId: String? = null,
    val notificationsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val dailySummaryTime: LocalTime = LocalTime.of(20, 0),
    val theme: AppTheme = AppTheme.SYSTEM,
    val isGuest: Boolean = false,
)

data class DayCompletion(
    val date: LocalDate,
    val completedCount: Int,
    val totalCount: Int,
)

data class AuthState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isGuest: Boolean = false,
    val userId: String? = null,
    val email: String? = null,
)
