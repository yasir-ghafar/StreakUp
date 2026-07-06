package com.techlad.streakup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.techlad.streakup.ui.navigation.StreakUpNavHost
import com.techlad.streakup.ui.screens.splash.SplashViewModel
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinAndroidContext {
                val activity = LocalContext.current as ComponentActivity
                val splashViewModel: SplashViewModel = koinViewModel(viewModelStoreOwner = activity)
                val authState by splashViewModel.authState.collectAsState()

                DisposableEffect(authState.isLoading) {
                    splashScreen.setKeepOnScreenCondition { authState.isLoading }
                    onDispose { }
                }

                StreakUpNavHost(splashViewModel = splashViewModel)
            }
        }
    }
}
