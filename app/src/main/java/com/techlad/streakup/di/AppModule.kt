package com.techlad.streakup.di

import androidx.room.Room
import com.techlad.streakup.BuildConfig
import com.techlad.streakup.data.local.StreakUpDatabase
import com.techlad.streakup.data.remote.SupabaseProvider
import com.techlad.streakup.data.remote.createSupabaseClient
import com.techlad.streakup.data.repository.AuthRepository
import com.techlad.streakup.data.repository.HabitRepository
import com.techlad.streakup.data.repository.SettingsRepository
import com.techlad.streakup.data.repository.SyncRepository
import com.techlad.streakup.ui.screens.auth.AuthViewModel
import com.techlad.streakup.ui.screens.auth.ForgotPasswordViewModel
import com.techlad.streakup.ui.screens.auth.ResetPasswordViewModel
import com.techlad.streakup.ui.screens.auth.SignUpViewModel
import com.techlad.streakup.ui.screens.habit.HabitDetailViewModel
import com.techlad.streakup.ui.screens.habit.HabitFormViewModel
import com.techlad.streakup.ui.screens.home.HomeViewModel
import com.techlad.streakup.ui.screens.settings.GuestUpgradeViewModel
import com.techlad.streakup.ui.screens.settings.SettingsViewModel
import com.techlad.streakup.ui.screens.splash.SplashViewModel
import com.techlad.streakup.ui.screens.stats.StatsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), StreakUpDatabase::class.java, "streakup.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<StreakUpDatabase>().habitDao() }
    single { get<StreakUpDatabase>().checkInDao() }
    single { get<StreakUpDatabase>().userSettingsDao() }

    single {
        SupabaseProvider(
            if (BuildConfig.SUPABASE_URL.contains("your-project")) null
            else createSupabaseClient(),
        )
    }

    single { AuthRepository(get(), get()) }
    single { SettingsRepository(get(), get(), get()) }
    single { HabitRepository(get(), get(), get(), get()) }
    single { SyncRepository(get(), get(), get(), get(), get(), get()) }

    viewModel { SplashViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { HabitFormViewModel(get()) }
    viewModel { (habitId: String?) -> HabitDetailViewModel(get(), habitId) }
    viewModel { StatsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { GuestUpgradeViewModel(get()) }
}
