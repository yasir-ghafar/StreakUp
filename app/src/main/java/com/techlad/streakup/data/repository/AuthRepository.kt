package com.techlad.streakup.data.repository

import com.techlad.streakup.data.local.dao.UserSettingsDao
import com.techlad.streakup.data.mapper.toDomain
import com.techlad.streakup.data.mapper.toEntity
import com.techlad.streakup.data.remote.dto.UserSettingsDto
import com.techlad.streakup.domain.model.AppTheme
import com.techlad.streakup.domain.model.AuthState
import com.techlad.streakup.domain.model.UserSettings
import com.techlad.streakup.data.remote.AuthDeepLink
import com.techlad.streakup.data.remote.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepository(
    private val supabaseProvider: SupabaseProvider,
    private val userSettingsDao: UserSettingsDao,
) {
    private val supabase: SupabaseClient? get() = supabaseProvider.client
    private val _authState = MutableStateFlow(AuthState())
    val authState: Flow<AuthState> = _authState.asStateFlow()

    private val _pendingPasswordReset = MutableStateFlow(false)
    val pendingPasswordReset: Flow<Boolean> = _pendingPasswordReset.asStateFlow()

    suspend fun checkSession() {
        if (_pendingPasswordReset.value) {
            _authState.value = AuthState(isLoading = false)
            return
        }
        _authState.value = _authState.value.copy(isLoading = true)
        val settings = userSettingsDao.getSettingsOnce()?.toDomain()

        if (settings?.isGuest == true) {
            _authState.value = AuthState(
                isLoading = false,
                isAuthenticated = false,
                isGuest = true,
                userId = settings.userId,
            )
            return
        }

        val client = supabase ?: run {
            _authState.value = AuthState(isLoading = false)
            return
        }

        runCatching {
            client.auth.refreshCurrentSession()
            val session = client.auth.currentSessionOrNull()
            if (session != null) {
                _authState.value = AuthState(
                    isLoading = false,
                    isAuthenticated = true,
                    userId = session.user?.id,
                    email = session.user?.email,
                )
            } else {
                _authState.value = AuthState(isLoading = false)
            }
        }.onFailure {
            _authState.value = AuthState(isLoading = false)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        val client = requireNotNull(supabase) { "Supabase not configured" }
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val session = client.auth.currentSessionOrNull()
            ?: error("Sign-in failed. Check your email and password.")
        _authState.value = AuthState(
            isLoading = false,
            isAuthenticated = true,
            isGuest = false,
            userId = session?.user?.id,
            email = session?.user?.email,
        )
    }

    suspend fun signUp(
        email: String,
        password: String,
        name: String = "",
        gender: String = "",
    ): Result<Unit> = runCatching {
        val client = requireNotNull(supabase) { "Supabase not configured" }
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("name", name)
                put("display_name", name)
                put("gender", gender)
            }
        }
        val session = client.auth.currentSessionOrNull()
        if (session == null) {
            error("Account created. Confirm your email, then sign in to sync.")
        }
        _authState.value = AuthState(
            isLoading = false,
            isAuthenticated = true,
            isGuest = false,
            userId = session.user?.id,
            email = session.user?.email,
        )
    }

    suspend fun continueAsGuest() {
        val guestId = "guest_${UUID.randomUUID()}"
        val settings = UserSettings(userId = guestId, isGuest = true)
        userSettingsDao.insert(settings.toEntity(guestId))
        _authState.value = AuthState(
            isLoading = false,
            isAuthenticated = false,
            isGuest = true,
            userId = guestId,
        )
    }

    suspend fun signOut() {
        try {
            supabase?.auth?.signOut()
        } catch (_: Exception) {
            // Clear local auth state even if remote sign-out fails.
        }
        _pendingPasswordReset.value = false
        _authState.value = AuthState(isLoading = false)
    }

    suspend fun resetPasswordForEmail(email: String): Result<Unit> = runCatching {
        val client = requireNotNull(supabase) { "Supabase not configured" }
        client.auth.resetPasswordForEmail(
            email = email.trim(),
            redirectUrl = AuthDeepLink.URI,
        )
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        val client = requireNotNull(supabase) { "Supabase not configured" }
        client.auth.updateUser {
            password = newPassword
        }
        val session = client.auth.currentSessionOrNull()
            ?: error("Password update failed. Open the reset link from your email again.")
        _pendingPasswordReset.value = false
        _authState.value = AuthState(
            isLoading = false,
            isAuthenticated = true,
            isGuest = false,
            userId = session.user?.id,
            email = session.user?.email,
        )
    }

    fun markPasswordRecoveryPending() {
        _pendingPasswordReset.value = true
    }

    fun clearPasswordRecoveryPending() {
        _pendingPasswordReset.value = false
    }

    suspend fun cancelPasswordRecovery() {
        signOut()
    }

    fun hasRecoverySession(): Boolean {
        return _pendingPasswordReset.value &&
            supabase?.auth?.currentSessionOrNull() != null
    }

    suspend fun refreshAuthFromSession() {
        val client = supabase ?: return
        val session = client.auth.currentSessionOrNull() ?: return
        _authState.value = AuthState(
            isLoading = false,
            isAuthenticated = true,
            isGuest = false,
            userId = session.user?.id,
            email = session.user?.email,
        )
    }

    fun getCurrentUserId(): String? = _authState.value.userId

    fun isAuthenticated(): Boolean = _authState.value.isAuthenticated

    fun isGuest(): Boolean = _authState.value.isGuest
}

class SettingsRepository(
    private val userSettingsDao: UserSettingsDao,
    private val supabaseProvider: SupabaseProvider,
    private val authRepository: AuthRepository,
) {
    private val supabase: SupabaseClient? get() = supabaseProvider.client
    private val guestSettings = UserSettings(userId = "guest", isGuest = true)

    fun getSettings(): Flow<UserSettings> =
        userSettingsDao.getSettings().map { it?.toDomain() ?: guestSettings }

    suspend fun updateSettings(settings: UserSettings) {
        val userId = authRepository.getCurrentUserId() ?: "guest"
        userSettingsDao.insert(settings.copy(userId = userId).toEntity(userId))
        val client = supabase
        if (client != null && authRepository.isAuthenticated()) {
            runCatching {
                client.postgrest["user_settings"].upsert(
                    UserSettingsDto(
                        userId = userId,
                        notificationsEnabled = settings.notificationsEnabled,
                        dailySummaryEnabled = settings.dailySummaryEnabled,
                        dailySummaryTime = settings.dailySummaryTime.toString(),
                        theme = settings.theme.name.lowercase(),
                        isGuest = false,
                    )
                )
            }
        }
    }

    suspend fun updateTheme(theme: AppTheme) {
        val current = userSettingsDao.getSettingsOnce()?.toDomain() ?: guestSettings
        updateSettings(current.copy(theme = theme))
    }

    suspend fun syncSettingsToRemote() {
        val settings = userSettingsDao.getSettingsOnce()?.toDomain() ?: return
        updateSettings(settings)
    }
}
