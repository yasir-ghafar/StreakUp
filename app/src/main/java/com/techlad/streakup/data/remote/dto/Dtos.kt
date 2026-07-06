package com.techlad.streakup.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HabitDto(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val icon: String = "✅",
    val color: String = "#6366F1",
    @SerialName("frequency_type") val frequencyType: String = "daily",
    @SerialName("frequency_target") val frequencyTarget: Int = 1,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("reminder_time") val reminderTime: String? = null,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class CheckInDto(
    val id: String,
    @SerialName("habit_id") val habitId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("checked_date") val checkedDate: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class UserSettingsDto(
    @SerialName("user_id") val userId: String,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("daily_summary_enabled") val dailySummaryEnabled: Boolean = false,
    @SerialName("daily_summary_time") val dailySummaryTime: String = "20:00",
    val theme: String = "system",
    @SerialName("is_guest") val isGuest: Boolean = false,
)
