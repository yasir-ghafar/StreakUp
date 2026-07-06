package com.techlad.streakup.data.repository

import com.techlad.streakup.data.local.dao.CheckInDao
import com.techlad.streakup.data.local.dao.HabitDao
import com.techlad.streakup.data.local.dao.UserSettingsDao
import com.techlad.streakup.data.mapper.toDomain
import com.techlad.streakup.data.mapper.toEntity
import com.techlad.streakup.domain.model.UserSettings
import kotlinx.coroutines.flow.first

class SyncRepository(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    private val settingsRepository: SettingsRepository,
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao,
    private val userSettingsDao: UserSettingsDao,
) {

    suspend fun upgradeGuestToCloud(
        email: String,
        password: String,
        isSignUp: Boolean,
    ): Result<Unit> = runCatching {
        require(authRepository.isGuest()) { "Only available in guest mode" }

        val authResult = if (isSignUp) {
            authRepository.signUp(email, password)
        } else {
            authRepository.signIn(email, password)
        }
        authResult.getOrThrow()

        val userId = authRepository.getCurrentUserId()
            ?: error("Sign-in succeeded but no session. Check your email for confirmation.")

        migrateLocalDataToUser(userId)
        habitRepository.syncPendingToRemote()
        settingsRepository.syncSettingsToRemote()
    }

    private suspend fun migrateLocalDataToUser(userId: String) {
        val currentSettings = userSettingsDao.getSettingsOnce()?.toDomain()
            ?: UserSettings(isGuest = true)

        habitDao.getAllHabits().first().forEach { entity ->
            habitDao.insert(entity.copy(userId = userId, isSynced = false))
        }

        checkInDao.getAll().forEach { entity ->
            checkInDao.insert(entity.copy(userId = userId, isSynced = false))
        }

        val upgradedSettings = currentSettings.copy(userId = userId, isGuest = false)
        userSettingsDao.insert(upgradedSettings.toEntity(userId))
    }
}
