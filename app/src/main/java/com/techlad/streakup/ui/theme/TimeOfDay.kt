package com.techlad.streakup.ui.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalTime

enum class TimeOfDay {
    Morning,
    Afternoon,
    Evening,
    Night,
}

data class TimeOfDayPalette(
    val timeOfDay: TimeOfDay,
    val greeting: String,
    /** Deep base fill for the card. */
    val base: Color,
    /** Warm radial glow tint (from the sun side). */
    val glow: Color,
    val track: Color,
    val progress: Color,
    val sun: Color,
    val sunGlow: Color,
    val arcActiveStart: Color,
    val arcActiveEnd: Color,
    val arcRemaining: Color,
    val horizon: Color,
    val onCard: Color,
    val onCardMuted: Color,
    /** 0f = sunrise (left), 0.5f = noon (top), 1f = sunset (right). */
    val sunProgress: Float,
)

fun resolveTimeOfDay(time: LocalTime = LocalTime.now()): TimeOfDay = when (time.hour) {
    in 5 until 11 -> TimeOfDay.Morning
    in 11 until 16 -> TimeOfDay.Afternoon
    in 16 until 20 -> TimeOfDay.Evening
    else -> TimeOfDay.Night
}

fun timeOfDayPalette(time: LocalTime = LocalTime.now()): TimeOfDayPalette {
    val period = resolveTimeOfDay(time)
    val sunProgress = sunProgressAlongArc(time)

    return when (period) {
        TimeOfDay.Morning -> TimeOfDayPalette(
            timeOfDay = period,
            greeting = "Good morning",
            base = Color(0xFF1A5C4A),
            glow = Color(0xFFFFE08A).copy(alpha = 0.28f),
            track = Color(0xFF0E3F35),
            progress = Color(0xFFA7F3D0),
            sun = Color(0xFFFFD56A),
            sunGlow = Color(0xFFFFE9A8).copy(alpha = 0.5f),
            arcActiveStart = Color(0xFFFFC93C),
            arcActiveEnd = Color(0xFFFFE9A8),
            arcRemaining = Color.White.copy(alpha = 0.28f),
            horizon = Color.White.copy(alpha = 0.2f),
            onCard = Color.White,
            onCardMuted = Color.White.copy(alpha = 0.7f),
            sunProgress = sunProgress,
        )
        TimeOfDay.Afternoon -> TimeOfDayPalette(
            timeOfDay = period,
            greeting = "Good afternoon",
            base = Color(0xFF143D30),
            glow = Color(0xFFFDBA74).copy(alpha = 0.22f),
            track = Color(0xFF0C2F26),
            progress = Color(0xFFA7F3D0),
            sun = Color(0xFFFFC93C),
            sunGlow = Color(0xFFFDBA74).copy(alpha = 0.45f),
            arcActiveStart = Color(0xFFFDBA74),
            arcActiveEnd = Color(0xFFFFE08A),
            arcRemaining = Color.White.copy(alpha = 0.25f),
            horizon = Color.White.copy(alpha = 0.18f),
            onCard = Color.White,
            onCardMuted = Color.White.copy(alpha = 0.68f),
            sunProgress = sunProgress,
        )
        TimeOfDay.Evening -> TimeOfDayPalette(
            timeOfDay = period,
            greeting = "Good evening",
            base = Color(0xFF143D30),
            glow = Color(0xFFFDBA74).copy(alpha = 0.26f),
            track = Color(0xFF0C2F26),
            progress = Color(0xFFA7F3D0),
            sun = Color(0xFFFDBA74),
            sunGlow = Color(0xFFFDBA74).copy(alpha = 0.4f),
            arcActiveStart = Color(0xFFEF9F27),
            arcActiveEnd = Color(0xFFFFD56A),
            arcRemaining = Color.White.copy(alpha = 0.22f),
            horizon = Color.White.copy(alpha = 0.16f),
            onCard = Color.White,
            onCardMuted = Color.White.copy(alpha = 0.65f),
            sunProgress = sunProgress,
        )
        TimeOfDay.Night -> TimeOfDayPalette(
            timeOfDay = period,
            greeting = "Good night",
            base = Color(0xFF0A241C),
            glow = Color(0xFF8BA4B8).copy(alpha = 0.12f),
            track = Color(0xFF061812),
            progress = Color(0xFF6BB89A),
            sun = Color(0xFFE8E4D4),
            sunGlow = Color(0xFFE8E4D4).copy(alpha = 0.22f),
            arcActiveStart = Color(0xFFB8C4CE),
            arcActiveEnd = Color(0xFFE8E4D4),
            arcRemaining = Color.White.copy(alpha = 0.14f),
            horizon = Color.White.copy(alpha = 0.12f),
            onCard = Color.White,
            onCardMuted = Color.White.copy(alpha = 0.55f),
            sunProgress = sunProgress,
        )
    }
}

/**
 * Maps clock time onto a sunrise→sunset arc.
 * Daylight (5:00–20:00) spans 0→1; night holds near the end of the arc.
 */
private fun sunProgressAlongArc(time: LocalTime): Float {
    val minutes = time.hour * 60 + time.minute
    val sunrise = 5 * 60
    val sunset = 20 * 60
    return when {
        minutes < sunrise -> 0.92f
        minutes > sunset -> 0.95f
        else -> ((minutes - sunrise).toFloat() / (sunset - sunrise)).coerceIn(0f, 1f)
    }
}

fun encouragementForProgress(done: Int, total: Int): String = when {
    total == 0 -> "Add a habit"
    done == 0 -> "Start strong"
    done >= total -> "All done"
    done.toFloat() / total >= 0.6f -> "Keep going"
    else -> "Nice start"
}
