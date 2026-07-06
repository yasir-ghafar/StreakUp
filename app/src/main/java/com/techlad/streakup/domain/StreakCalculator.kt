package com.techlad.streakup.domain

import com.techlad.streakup.domain.model.FrequencyType
import com.techlad.streakup.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object StreakCalculator {

    fun calculateStreaks(
        habit: Habit,
        checkInDates: Set<LocalDate>,
        today: LocalDate = LocalDate.now(),
    ): Pair<Int, Int> {
        if (checkInDates.isEmpty()) return 0 to 0

        val sorted = checkInDates.sortedDescending()
        val current = when (habit.frequencyType) {
            FrequencyType.DAILY -> calculateDailyCurrentStreak(sorted, today)
            FrequencyType.WEEKLY -> calculateWeeklyCurrentStreak(sorted, habit.frequencyTarget, today)
        }
        val longest = when (habit.frequencyType) {
            FrequencyType.DAILY -> calculateDailyLongestStreak(sorted)
            FrequencyType.WEEKLY -> calculateWeeklyLongestStreak(sorted, habit.frequencyTarget)
        }
        return current to longest
    }

    fun weeklyProgress(checkInDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return checkInDates.count { !it.isBefore(weekStart) && !it.isAfter(today) }
    }

    fun isCheckedOnDate(checkInDates: Set<LocalDate>, date: LocalDate): Boolean =
        date in checkInDates

    private fun calculateDailyCurrentStreak(sortedDates: List<LocalDate>, today: LocalDate): Int {
        var streak = 0
        var cursor = today
        if (sortedDates.firstOrNull() != today) {
            cursor = today.minusDays(1)
        }
        for (date in sortedDates) {
            if (date == cursor) {
                streak++
                cursor = cursor.minusDays(1)
            } else if (date.isBefore(cursor)) {
                break
            }
        }
        return streak
    }

    private fun calculateDailyLongestStreak(sortedDates: List<LocalDate>): Int {
        if (sortedDates.isEmpty()) return 0
        var longest = 1
        var current = 1
        val ascending = sortedDates.sorted()
        for (i in 1 until ascending.size) {
            if (ascending[i] == ascending[i - 1].plusDays(1)) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun calculateWeeklyCurrentStreak(
        sortedDates: List<LocalDate>,
        target: Int,
        today: LocalDate,
    ): Int {
        var streak = 0
        var weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        while (true) {
            val weekEnd = weekStart.plusDays(6)
            val count = sortedDates.count { !it.isBefore(weekStart) && !it.isAfter(weekEnd) }
            if (count >= target) {
                streak++
                weekStart = weekStart.minusWeeks(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateWeeklyLongestStreak(sortedDates: List<LocalDate>, target: Int): Int {
        if (sortedDates.isEmpty()) return 0
        val weeks = sortedDates
            .map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .groupingBy { it }
            .eachCount()
        val sortedWeeks = weeks.keys.sorted()
        var longest = 0
        var current = 0
        for (i in sortedWeeks.indices) {
            val met = weeks[sortedWeeks[i]]!! >= target
            if (met) {
                val consecutive = i == 0 || sortedWeeks[i] == sortedWeeks[i - 1].plusWeeks(1)
                current = if (consecutive) current + 1 else 1
                longest = maxOf(longest, current)
            } else {
                current = 0
            }
        }
        return longest
    }

    fun dailyCompletionPercent(
        habits: List<Habit>,
        checkInsByHabit: Map<String, Set<LocalDate>>,
        date: LocalDate = LocalDate.now(),
    ): Float {
        val active = habits.filter { !it.isArchived }
        if (active.isEmpty()) return 0f
        val done = active.count { habit ->
            checkInsByHabit[habit.id]?.contains(date) == true
        }
        return done.toFloat() / active.size
    }

    fun buildHeatmapData(
        checkInDates: Set<LocalDate>,
        startDate: LocalDate,
        endDate: LocalDate = LocalDate.now(),
    ): Map<LocalDate, Int> {
        val result = mutableMapOf<LocalDate, Int>()
        var cursor = startDate
        while (!cursor.isAfter(endDate)) {
            result[cursor] = if (cursor in checkInDates) 1 else 0
            cursor = cursor.plusDays(1)
        }
        return result
    }
}
