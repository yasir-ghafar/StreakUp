package com.techlad.streakup.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object ResetPassword : Screen("reset_password")
    data object Home : Screen("home")
    data object AddHabit : Screen("add_habit")
    data object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: String) = "edit_habit/$habitId"
    }
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object GuestUpgrade : Screen("guest_upgrade")
}
