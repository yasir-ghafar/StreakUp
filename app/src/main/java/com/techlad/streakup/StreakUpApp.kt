package com.techlad.streakup

import android.app.Application
import com.techlad.streakup.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class StreakUpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@StreakUpApp)
            modules(appModule)
        }
    }
}
