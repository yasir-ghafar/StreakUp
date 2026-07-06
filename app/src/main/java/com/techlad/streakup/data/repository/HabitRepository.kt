package com.techlad.streakup.data.repository

import com.techlad.streakup.data.local.dao.CheckInDao
import com.techlad.streakup.data.local.dao.HabitDao
import com.techlad.streakup.data.local.dao.UserSettingsDao
import com.techlad.streakup.data.mapper.toDomain
import com.techlad.streakup.data.mapper.toDto
import com.techlad.streakup.data.mapper.toEntity
import com.techlad.streakup.data.remote.dto.CheckInDto
import com.techlad.streakup.data.remote.dto.HabitDto
import com.techlad.streakup.data.remote.dto.UserSettingsDto
import com.techlad.streakup.domain.StreakCalculator
import com.techlad.streakup.domain.model.CheckIn
import com.techlad.streakup.domain.model.DayCompletion
import com.techlad.streakup.domain.model.Habit
import com.techlad.streakup.domain.model.HabitWithStatus
import com.techlad.streakup.domain.model.UserSettings
import com.techlad.streakup.data.remote.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao,
    private val supabaseProvider: SupabaseProvider,
    private val authRepository: AuthRepository,
) {
    private val supabase: SupabaseClient? get() = supabaseProvider.client

    fun getActiveHabitsWithStatus(today: LocalDate = LocalDate.now()): Flow<List<HabitWithStatus>> =
        combine(
            habitDao.getActiveHabits(),
            checkInDao.getByDate(today.toString()),
        ) { habits, todayCheckIns ->
            habits.map { entity ->
                val habit = entity.toDomain()
                HabitWithStatus(
                    habit = habit,
                    isCheckedToday = todayCheckIns.any { it.habitId == habit.id },
                    currentStreak = 0,
                    longestStreak = 0,
                    weeklyProgress = 0,
                )
            }
        }

    suspend fun enrichHabitsWithStreaks(
        habits: List<HabitWithStatus>,
        today: LocalDate = LocalDate.now(),
    ): List<HabitWithStatus> {
        val start = today.minusDays(365).toString()
        val end = today.toString()
        return habits.map { item ->
            val dates = checkInDao.getInRange(item.habit.id, start, end)
                .map { LocalDate.parse(it.checkedDate) }
                .toSet()
            val (current, longest) = StreakCalculator.calculateStreaks(item.habit, dates, today)
            item.copy(
                currentStreak = current,
                longestStreak = longest,
                weeklyProgress = StreakCalculator.weeklyProgress(dates, today),
            )
        }
    }

    fun getAllHabits(): Flow<List<Habit>> =
        habitDao.getAllHabits().map { list -> list.map { it.toDomain() } }

    suspend fun getHabitById(id: String): Habit? =
        habitDao.getById(id)?.toDomain()

    suspend fun getCheckInDates(habitId: String, days: Long = 365): Set<LocalDate> {
        val end = LocalDate.now()
        val start = end.minusDays(days)
        return checkInDao.getInRange(habitId, start.toString(), end.toString())
            .map { LocalDate.parse(it.checkedDate) }
            .toSet()
    }

    suspend fun saveHabit(habit: Habit): Habit {
        val userId = authRepository.getCurrentUserId()
        val maxOrder = habitDao.getAllHabits().first().maxOfOrNull { it.sortOrder } ?: -1
        val toSave = habit.copy(
            userId = userId,
            sortOrder = if (habit.sortOrder == 0 && maxOrder >= 0) maxOrder + 1 else habit.sortOrder,
            updatedAt = System.currentTimeMillis(),
            isSynced = false,
        )
        habitDao.insert(toSave.toEntity())
        syncHabitToRemote(toSave)
        return toSave
    }

    suspend fun archiveHabit(id: String) {
        val habit = habitDao.getById(id)?.toDomain() ?: return
        val archived = habit.copy(isArchived = true, updatedAt = System.currentTimeMillis(), isSynced = false)
        habitDao.insert(archived.toEntity())
        syncHabitToRemote(archived)
    }

    suspend fun deleteHabit(id: String) {
        habitDao.deleteById(id)
        val client = supabase
        if (client != null && authRepository.isAuthenticated()) {
            runCatching {
                client.postgrest["habits"].delete { filter { eq("id", id) } }
            }
        }
    }

    suspend fun reorderHabits(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id ->
            habitDao.updateSortOrder(id, index)
            habitDao.getById(id)?.let { entity ->
                val updated = entity.copy(sortOrder = index, isSynced = false)
                habitDao.insert(updated)
                syncHabitToRemote(updated.toDomain())
            }
        }
    }

    suspend fun toggleCheckIn(habitId: String, date: LocalDate = LocalDate.now()): Boolean {
        val existing = checkInDao.getByHabitAndDate(habitId, date.toString())
        return if (existing != null) {
            checkInDao.deleteByHabitAndDate(habitId, date.toString())
            val client = supabase
            if (client != null && authRepository.isAuthenticated()) {
                runCatching {
                    client.postgrest["check_ins"].delete {
                        filter {
                            eq("habit_id", habitId)
                            eq("checked_date", date.toString())
                        }
                    }
                }
            }
            false
        } else {
            val checkIn = CheckIn(
                habitId = habitId,
                userId = authRepository.getCurrentUserId(),
                checkedDate = date,
                isSynced = false,
            )
            checkInDao.insert(checkIn.toEntity())
            syncCheckInToRemote(checkIn)
            true
        }
    }

    fun getTodayCompletionPercent(today: LocalDate = LocalDate.now()): Flow<Float> =
        combine(habitDao.getActiveHabits(), checkInDao.getByDate(today.toString())) { habits, checkIns ->
            if (habits.isEmpty()) 0f
            else checkIns.count { ci -> habits.any { it.id == ci.habitId } }.toFloat() / habits.size
        }

    suspend fun getWeeklyStats(): List<DayCompletion> {
        val habits = habitDao.getActiveHabits().first()
        val total = habits.size
        val today = LocalDate.now()
        return (0..6).map { offset ->
            val date = today.minusDays(6L - offset)
            val checkIns = checkInDao.getByDate(date.toString()).first()
            val completed = checkIns.count { ci -> habits.any { it.id == ci.habitId } }
            DayCompletion(date, completed, total)
        }
    }

    suspend fun getMonthlyStats(): List<DayCompletion> {
        val habits = habitDao.getActiveHabits().first()
        val total = habits.size
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today.minusDays(29), today).toInt()
        return (0..days).map { offset ->
            val date = today.minusDays(days - offset.toLong())
            val checkIns = checkInDao.getByDate(date.toString()).first()
            val completed = checkIns.count { ci -> habits.any { it.id == ci.habitId } }
            DayCompletion(date, completed, total)
        }
    }

    suspend fun syncFromRemote() {
        val client = supabase ?: return
        if (!authRepository.isAuthenticated()) return
        val userId = authRepository.getCurrentUserId() ?: return

        runCatching {
            val remoteHabits = client.postgrest["habits"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<HabitDto>()
            habitDao.insertAll(remoteHabits.map { it.toDomain().toEntity() })

            val remoteCheckIns = client.postgrest["check_ins"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<CheckInDto>()
            checkInDao.insertAll(remoteCheckIns.map { it.toDomain().toEntity() })
        }
    }

    suspend fun syncPendingToRemote() {
        if (supabase == null || !authRepository.isAuthenticated()) return
        habitDao.getUnsynced().forEach { entity ->
            syncHabitToRemote(entity.toDomain())
        }
        checkInDao.getUnsynced().forEach { entity ->
            syncCheckInToRemote(entity.toDomain())
        }
    }

    private suspend fun syncHabitToRemote(habit: Habit) {
        val client = supabase ?: return
        if (!authRepository.isAuthenticated()) return
        runCatching {
            client.postgrest["habits"].upsert(habit.toDto())
            habitDao.markSynced(habit.id)
        }
    }

    private suspend fun syncCheckInToRemote(checkIn: CheckIn) {
        val client = supabase ?: return
        if (!authRepository.isAuthenticated()) return
        runCatching {
            client.postgrest["check_ins"].upsert(checkIn.toDto())
            checkInDao.markSynced(checkIn.id)
        }
    }
}
