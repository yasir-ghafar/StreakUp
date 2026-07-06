package com.techlad.streakup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.streakup.domain.model.FrequencyType
import com.techlad.streakup.domain.model.HabitWithStatus
import com.techlad.streakup.ui.theme.StreakUpThemeExtras

@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    reorderMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithStatus.habit
    val extras = StreakUpThemeExtras.colors
    val isChecked = habitWithStatus.isCheckedToday

    val circleBackground = when {
        isChecked -> extras.completedCheck.copy(alpha = 0.2f)
        else -> extras.pendingHabit.copy(alpha = 0.15f)
    }
    val circleBorder = when {
        isChecked -> extras.completedCheck
        else -> extras.pendingHabit
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (reorderMode) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(circleBackground)
                    .border(2.dp, circleBorder, CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center,
            ) {
                if (isChecked) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = extras.completedCheck,
                    )
                } else {
                    Text(text = habit.icon, fontSize = 22.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when (habit.frequencyType) {
                        FrequencyType.DAILY -> "Daily"
                        FrequencyType.WEEKLY -> "${habit.frequencyTarget}x per week · ${habitWithStatus.weeklyProgress}/${habit.frequencyTarget}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            StreakBadge(streak = habitWithStatus.currentStreak)
        }
    }
}

@Composable
fun HeatmapCalendar(
    data: Map<java.time.LocalDate, Int>,
    habitColor: Color = StreakUpThemeExtras.colors.completedCheck,
    modifier: Modifier = Modifier,
) {
    val extras = StreakUpThemeExtras.colors
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
