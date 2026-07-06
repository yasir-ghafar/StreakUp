package com.techlad.streakup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.techlad.streakup.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId ORDER BY checkedDate DESC")
    fun getByHabit(habitId: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND checkedDate = :date")
    suspend fun getByHabitAndDate(habitId: String, date: String): CheckInEntity?

    @Query("SELECT * FROM check_ins WHERE checkedDate = :date")
    fun getByDate(date: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND checkedDate BETWEEN :start AND :end")
    suspend fun getInRange(habitId: String, start: String, end: String): List<CheckInEntity>

    @Query("SELECT * FROM check_ins WHERE isSynced = 0")
    suspend fun getUnsynced(): List<CheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkIns: List<CheckInEntity>)

    @Query("DELETE FROM check_ins WHERE habitId = :habitId AND checkedDate = :date")
    suspend fun deleteByHabitAndDate(habitId: String, date: String)

    @Query("SELECT * FROM check_ins")
    suspend fun getAll(): List<CheckInEntity>

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE check_ins SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
