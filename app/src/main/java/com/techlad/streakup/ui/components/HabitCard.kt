package com.techlad.streakup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.streakup.domain.model.FrequencyType
import com.techlad.streakup.domain.model.Habit
import com.techlad.streakup.domain.model.HabitWithStatus
import com.techlad.streakup.ui.theme.InkText
import com.techlad.streakup.ui.theme.StreakTeal
import com.techlad.streakup.ui.theme.StreakUpTheme
import com.techlad.streakup.ui.theme.StreakUpThemeExtras
import com.techlad.streakup.ui.theme.SurfaceLight

@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    reorderMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithStatus.habit
    val habitColor = parseColor(habit.color)
    val recentDays = habitWithStatus.recentDays
        .let { if (it.size == 7) it else List(7) { index -> index == 6 && habitWithStatus.isCheckedToday } }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !reorderMode, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (reorderMode) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 14.dp, end = 8.dp)
                        .align(Alignment.CenterVertically),
                )
            }

            HabitIconTile(
                icon = habit.icon,
                tint = habitColor,
                checked = habitWithStatus.isCheckedToday,
                onClick = onToggle,
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkText,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = frequencyLabel(habit.frequencyType, habit.frequencyTarget, habitWithStatus.weeklyProgress),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    StreakBadge(streak = habitWithStatus.currentStreak)
                }

                Spacer(Modifier.height(14.dp))

                WeekCheckRow(
                    days = recentDays,
                    checkedColor = StreakTeal,
                    onTodayClick = onToggle,
                )
            }
        }
    }
}

@Composable
private fun HabitIconTile(
    icon: String,
    tint: Color,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = icon, fontSize = 28.sp)
        if (checked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(StreakTeal),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed today",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekCheckRow(
    days: List<Boolean>,
    checkedColor: Color,
    onTodayClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEachIndexed { index, done ->
            val isToday = index == days.lastIndex
            DayCheckDot(
                done = done,
                checkedColor = checkedColor,
                modifier = if (isToday) {
                    Modifier.clickable(onClick = onTodayClick)
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
private fun DayCheckDot(
    done: Boolean,
    checkedColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                if (done) checkedColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun frequencyLabel(
    type: FrequencyType,
    target: Int,
    weeklyProgress: Int,
): String = when (type) {
    FrequencyType.DAILY -> "Daily"
    FrequencyType.WEEKLY -> "${target}x per week · $weeklyProgress/$target"
}

@Composable
fun HeatmapCalendar(
    data: Map<java.time.LocalDate, Int>,
    habitColor: Color = StreakUpThemeExtras.colors.completedCheck,
    modifier: Modifier = Modifier,
) {
    val sortedDates = data.keys.sorted()
    val weeks = sortedDates.chunked(7)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    val value = data[date] ?: 0
                    val color = when (value) {
                        1 -> habitColor
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
private fun HabitCardPreview() {
    StreakUpTheme {
        HabitCard(
            habitWithStatus = HabitWithStatus(
                habit = Habit(
                    name = "Drink water",
                    icon = "💧",
                    color = "#5B9BD5",
                ),
                isCheckedToday = true,
                currentStreak = 10,
                longestStreak = 12,
                recentDays = List(7) { true },
            ),
            onToggle = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
