package com.techlad.streakup.data.mapper

import com.techlad.streakup.data.local.entity.CheckInEntity
import com.techlad.streakup.data.local.entity.HabitEntity
import com.techlad.streakup.data.local.entity.UserSettingsEntity
import com.techlad.streakup.data.remote.dto.CheckInDto
import com.techlad.streakup.data.remote.dto.HabitDto
import com.techlad.streakup.data.remote.dto.UserSettingsDto
import com.techlad.streakup.domain.model.AppTheme
import com.techlad.streakup.domain.model.CheckIn
import com.techlad.streakup.domain.model.FrequencyType
import com.techlad.streakup.domain.model.Habit
import com.techlad.streakup.domain.model.UserSettings
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    frequencyType = frequencyType.name.lowercase(),
    frequencyTarget = frequencyTarget,
    sortOrder = sortOrder,
    reminderTime = reminderTime?.toString(),
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
)

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    frequencyType = if (frequencyType == "weekly") FrequencyType.WEEKLY else FrequencyType.DAILY,
    frequencyTarget = frequencyTarget,
    sortOrder = sortOrder,
    reminderTime = reminderTime?.let { LocalTime.parse(it) },
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
)

fun CheckIn.toEntity(): CheckInEntity = CheckInEntity(
    id = id,
    habitId = habitId,
    userId = userId,
    checkedDate = checkedDate.toString(),
    createdAt = createdAt,
    isSynced = isSynced,
)

fun CheckInEntity.toDomain(): CheckIn = CheckIn(
    id = id,
    habitId = habitId,
    userId = userId,
    checkedDate = LocalDate.parse(checkedDate),
    createdAt = createdAt,
    isSynced = isSynced,
)

fun UserSettings.toEntity(userId: String): UserSettingsEntity = UserSettingsEntity(
    userId = userId,
    notificationsEnabled = notificationsEnabled,
    dailySummaryEnabled = dailySummaryEnabled,
    dailySummaryTime = dailySummaryTime.toString(),
    theme = theme.name.lowercase(),
    isGuest = isGuest,
)

fun UserSettingsEntity.toDomain(): UserSettings = UserSettings(
    userId = userId,
    notificationsEnabled = notificationsEnabled,
    dailySummaryEnabled = dailySummaryEnabled,
    dailySummaryTime = LocalTime.parse(dailySummaryTime),
    theme = when (theme) {
        "light" -> AppTheme.LIGHT
        "dark" -> AppTheme.DARK
        else -> AppTheme.SYSTEM
    },
    isGuest = isGuest,
)

fun Habit.toDto(): HabitDto = HabitDto(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    frequencyType = frequencyType.name.lowercase(),
    frequencyTarget = frequencyTarget,
    sortOrder = sortOrder,
    reminderTime = reminderTime?.toString(),
    isArchived = isArchived,
    createdAt = Instant.ofEpochMilli(createdAt).toString(),
    updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
)

fun HabitDto.toDomain(): Habit = Habit(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    frequencyType = if (frequencyType == "weekly") FrequencyType.WEEKLY else FrequencyType.DAILY,
    frequencyTarget = frequencyTarget,
    sortOrder = sortOrder,
    reminderTime = reminderTime?.let { LocalTime.parse(it) },
    isArchived = isArchived,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    isSynced = true,
)

fun CheckIn.toDto(): CheckInDto = CheckInDto(
    id = id,
    habitId = habitId,
    userId = userId,
    checkedDate = checkedDate.toString(),
    createdAt = Instant.ofEpochMilli(createdAt).toString(),
)

fun CheckInDto.toDomain(): CheckIn = CheckIn(
    id = id,
    habitId = habitId,
    userId = userId,
    checkedDate = LocalDate.parse(checkedDate),
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    isSynced = true,
)

fun UserSettingsDto.toDomain(): UserSettings = UserSettings(
    userId = userId,
    notificationsEnabled = notificationsEnabled,
    dailySummaryEnabled = dailySummaryEnabled,
    dailySummaryTime = LocalTime.parse(dailySummaryTime),
    theme = when (theme) {
        "light" -> AppTheme.LIGHT
        "dark" -> AppTheme.DARK
        else -> AppTheme.SYSTEM
    },
    isGuest = isGuest,
)

fun parseInstantToMillis(value: String): Long =
    Instant.parse(value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
