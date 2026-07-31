package com.techlad.streakup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.isSystemInDarkTheme
import com.techlad.streakup.domain.model.AppTheme
import com.techlad.streakup.domain.model.UserSettings
import com.techlad.streakup.data.repository.AuthRepository
import com.techlad.streakup.data.repository.SettingsRepository
import com.techlad.streakup.ui.screens.auth.ForgotPasswordScreen
import com.techlad.streakup.ui.screens.auth.LoginScreen
import com.techlad.streakup.ui.screens.auth.ResetPasswordScreen
import com.techlad.streakup.ui.screens.auth.SignUpScreen
import com.techlad.streakup.ui.screens.habit.HabitDetailScreen
import com.techlad.streakup.ui.screens.habit.HabitFormScreen
import com.techlad.streakup.ui.screens.home.HomeScreen
import com.techlad.streakup.ui.screens.settings.GuestUpgradeScreen
import com.techlad.streakup.ui.screens.settings.SettingsScreen
import com.techlad.streakup.ui.screens.splash.SplashScreen
import com.techlad.streakup.ui.screens.splash.SplashViewModel
import com.techlad.streakup.ui.screens.stats.StatsScreen
import com.techlad.streakup.ui.theme.StreakUpTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun StreakUpNavHost(
    splashViewModel: SplashViewModel,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val authRepository: AuthRepository = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val pendingPasswordReset by authRepository.pendingPasswordReset.collectAsState(initial = false)
    val settings by settingsRepository.getSettings().collectAsState(initial = UserSettings())

    LaunchedEffect(pendingPasswordReset) {
        if (pendingPasswordReset) {
            navController.navigate(Screen.ResetPassword.route) {
                launchSingleTop = true
            }
        }
    }

    val darkTheme = when (settings.theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    StreakUpTheme(darkTheme = darkTheme) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onAuthenticated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.ResetPassword.route) {
                ResetPasswordScreen(
                    onPasswordUpdated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        scope.launch { authRepository.cancelPasswordRecovery() }
                        navController.popBackStack()
                    },
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onAuthenticated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                    onHabitClick = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
                    onStats = { navController.navigate(Screen.Stats.route) },
                    onSettings = { navController.navigate(Screen.Settings.route) },
                )
            }

            composable(Screen.AddHabit.route) {
                HabitFormScreen(
                    habitId = null,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.EditHabit.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
            ) { backStack ->
                val habitId = backStack.arguments?.getString("habitId")
                HabitFormScreen(
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.HabitDetail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType }),
            ) { backStack ->
                val habitId = backStack.arguments?.getString("habitId") ?: return@composable
                HabitDetailScreen(
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.EditHabit.createRoute(id)) },
                    onArchived = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onUpgradeAccount = { navController.navigate(Screen.GuestUpgrade.route) },
                )
            }

            composable(Screen.GuestUpgrade.route) {
                GuestUpgradeScreen(
                    onBack = { navController.popBackStack() },
                    onUpgraded = { navController.popBackStack() },
                )
            }
        }
    }
}
