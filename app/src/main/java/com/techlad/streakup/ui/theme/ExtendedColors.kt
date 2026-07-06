package com.techlad.streakup.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class StreakUpExtendedColors(
    val streakTeal: Color = StreakTeal,
    val flameAmber: Color = FlameAmber,
    val flameCoral: Color = FlameCoral,
    val goalHit: Color = GoalHit,
    val missedDay: Color = MissedDay,
    val pendingHabit: Color = FlameAmber,
    val completedCheck: Color = StreakTeal,
    val cta: Color = FlameCoral,
)

val LocalStreakUpColors = staticCompositionLocalOf { StreakUpExtendedColors() }

object StreakUpThemeExtras {
    val colors: StreakUpExtendedColors
        @Composable
        get() = LocalStreakUpColors.current
}
