package com.techlad.streakup.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techlad.streakup.domain.model.DayCompletion
import com.techlad.streakup.ui.theme.StreakTeal
import com.techlad.streakup.ui.theme.StreakUpThemeExtras
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = when (uiState.period) {
        StatsPeriod.WEEKLY -> uiState.weeklyData
        StatsPeriod.MONTHLY -> uiState.monthlyData
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.period == StatsPeriod.WEEKLY,
                    onClick = { viewModel.setPeriod(StatsPeriod.WEEKLY) },
                    label = { Text("Weekly") },
                )
                FilterChip(
                    selected = uiState.period == StatsPeriod.MONTHLY,
                    onClick = { viewModel.setPeriod(StatsPeriod.MONTHLY) },
                    label = { Text("Monthly") },
                )
            }

            Spacer(Modifier.height(24.dp))

            val avgCompletion = if (data.isEmpty()) 0f
            else data.map { if (it.totalCount > 0) it.completedCount.toFloat() / it.totalCount else 0f }.average().toFloat()

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Average Completion", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${(avgCompletion * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (avgCompletion >= 1f) {
                            StreakUpThemeExtras.colors.goalHit
                        } else {
                            StreakTeal
                        },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Daily Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            data.forEach { day ->
                DayCompletionBar(day)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DayCompletionBar(day: DayCompletion) {
    val percent = if (day.totalCount > 0) day.completedCount.toFloat() / day.totalCount else 0f
    val extras = StreakUpThemeExtras.colors
    val barColor = when {
        percent >= 1f -> extras.goalHit
        percent > 0f -> extras.streakTeal
        else -> extras.missedDay.copy(alpha = 0.35f)
    }
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = day.date.format(formatter),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(90.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor),
            )
        }
        Text(
            text = "${day.completedCount}/${day.totalCount}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(48.dp),
        )
    }
}
