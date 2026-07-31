package com.techlad.streakup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.techlad.streakup.data.remote.SupabaseProvider
import com.techlad.streakup.data.repository.AuthRepository
import com.techlad.streakup.ui.navigation.StreakUpNavHost
import com.techlad.streakup.ui.screens.splash.SplashViewModel
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val supabaseProvider: SupabaseProvider by inject()
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        handleAuthDeepLink(intent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {
        if (intent?.data == null) return
        val client = supabaseProvider.client ?: return
        val isRecovery = isPasswordRecoveryDeepLink(intent)
        client.handleDeeplinks(intent) {
            lifecycleScope.launch {
                if (isRecovery) {
                    authRepository.markPasswordRecoveryPending()
                } else {
                    authRepository.refreshAuthFromSession()
                }
            }
        }
    }

    private fun isPasswordRecoveryDeepLink(intent: Intent): Boolean {
        val data = intent.data ?: return false
        val fragment = data.fragment.orEmpty()
        val type = data.getQueryParameter("type")
        return fragment.contains("type=recovery") || type == "recovery"
    }
}
