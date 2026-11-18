package com.techlad.streakup.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.streakup.ui.theme.StreakUpTheme
import com.techlad.streakup.ui.theme.TimeOfDayPalette
import com.techlad.streakup.ui.theme.encouragementForProgress
import com.techlad.streakup.ui.theme.timeOfDayPalette
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

/**
 * Home hero card showing today's habit completion, stats, and a sun arc
 * whose position and card shades follow the current time of day.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodaysStreakCard(
    doneCount: Int,
    totalCount: Int,
    bestStreak: Int,
    activeCount: Int,
    modifier: Modifier = Modifier,
    time: LocalTime = LocalTime.now(),
) {
    val palette = remember(time.hour, time.minute) { timeOfDayPalette(time) }
    val progress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount
    val encouragement = encouragementForProgress(doneCount, totalCount)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(palette.base)
            .drawBehind {
                // Soft warm glow from the sun side (top-right)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.glow, Color.Transparent),
                        center = Offset(size.width * 0.82f, size.height * 0.28f),
                        radius = size.width * 0.85f,
                    ),
                )
            },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Today's streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = palette.onCard,
                )
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("h:mm a")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.onCard,
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusProgressRing(
                    doneCount = doneCount,
                    totalCount = totalCount,
                    progress = progress,
                    caption = encouragement,
                    palette = palette,
                )

                DayArcGraphic(
                    palette = palette,
                    modifier = Modifier.size(width = 150.dp, height = 90.dp),
                )
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatColumn(
                    label = "Habits",
                    value = "$doneCount/$totalCount",
                    onCard = palette.onCard,
                    muted = palette.onCardMuted,
                    modifier = Modifier.weight(1f),
                    alignment = Alignment.Start,
                )
                StatColumn(
                    label = "Best streak",
                    value = bestStreak.toString(),
                    onCard = palette.onCard,
                    muted = palette.onCardMuted,
                    modifier = Modifier.weight(1f),
                    alignment = Alignment.CenterHorizontally,
                )
                StatColumn(
                    label = "Active",
                    value = activeCount.toString(),
                    onCard = palette.onCard,
                    muted = palette.onCardMuted,
                    modifier = Modifier.weight(1f),
                    alignment = Alignment.End,
                )
            }
        }
    }
}

@Composable
private fun StatusProgressRing(
    doneCount: Int,
    totalCount: Int,
    progress: Float,
    caption: String,
    palette: TimeOfDayPalette,
    size: Dp = 112.dp,
    strokeWidth: Dp = 10.dp,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val inset = strokeWidth.toPx() / 2f
                val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = palette.track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                drawArc(
                    color = palette.progress,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$doneCount/$totalCount",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.onCard,
                    lineHeight = 28.sp,
                )
                Text(
                    text = "DONE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    color = palette.onCardMuted,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.onCard,
        )
    }
}

@Composable
private fun DayArcGraphic(
    palette: TimeOfDayPalette,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val paddingX = 2.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val paddingBottom = 14.dp.toPx()
        val arcWidth = size.width - paddingX * 2
        val arcHeight = size.height - paddingTop - paddingBottom
        val sunProgress = palette.sunProgress.coerceIn(0f, 1f)

        // Canvas angles: 0° = 3 o'clock, positive = clockwise, Y grows downward.
        // So 90° is the BOTTOM and 270° is the TOP of the oval.
        // Upper day arch (left → top → right) = start 180°, sweep +180°.
        val ovalTopLeft = Offset(paddingX, paddingTop)
        val ovalSize = Size(arcWidth, arcHeight * 2f)
        val horizonY = paddingTop + arcHeight
        // Angle of the sun along that upper arch: 180° (sunrise) → 270° (noon) → 360° (sunset)
        val sunAngle = 180f + 180f * sunProgress

        drawLine(
            color = palette.horizon,
            start = Offset(paddingX, horizonY),
            end = Offset(size.width - paddingX, horizonY),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )

        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val dashStroke = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 7.dp.toPx())),
        )

        // Remaining daylight (sun → sunset), dashed
        val remainingSweep = 180f * (1f - sunProgress)
        if (remainingSweep != 0f) {
            drawArc(
                color = palette.arcRemaining,
                startAngle = sunAngle,
                sweepAngle = remainingSweep,
                useCenter = false,
                topLeft = ovalTopLeft,
                size = ovalSize,
                style = dashStroke,
            )
        }

        // Elapsed day (sunrise → sun), warm gradient
        val activeSweep = 180f * sunProgress
        if (activeSweep != 0f) {
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(palette.arcActiveStart, palette.arcActiveEnd),
                    start = Offset(paddingX, paddingTop),
                    end = Offset(paddingX + arcWidth, paddingTop),
                ),
                startAngle = 180f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = ovalTopLeft,
                size = ovalSize,
                style = stroke,
            )
        }

        // Place the sun on the same upper arch (sin is +down in canvas space)
        val angleRad = Math.toRadians(sunAngle.toDouble())
        val cx = paddingX + arcWidth / 2f
        val cy = paddingTop + arcHeight
        val rx = arcWidth / 2f
        val ry = arcHeight
        val sunCenter = Offset(
            x = (cx + rx * cos(angleRad)).toFloat(),
            y = (cy + ry * sin(angleRad)).toFloat(),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.sunGlow, Color.Transparent),
                center = sunCenter,
                radius = 36.dp.toPx(),
            ),
            radius = 36.dp.toPx(),
            center = sunCenter,
        )

        val sunRadius = 11.dp.toPx()
        drawCircle(color = palette.sun, radius = sunRadius, center = sunCenter)
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = sunRadius * 0.32f,
            center = sunCenter + Offset(-sunRadius * 0.22f, -sunRadius * 0.22f),
        )
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    onCard: Color,
    muted: Color,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = onCard,
            textAlign = TextAlign.Center,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
private fun TodaysStreakCardEveningPreview() {
    StreakUpTheme {
        TodaysStreakCard(
            doneCount = 3,
            totalCount = 5,
            bestStreak = 12,
            activeCount = 5,
            time = LocalTime.of(18, 30),
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFF1EFE8)
@Composable
private fun TodaysStreakCardMorningPreview() {
    StreakUpTheme {
        TodaysStreakCard(
            doneCount = 1,
            totalCount = 5,
            bestStreak = 4,
            activeCount = 5,
            time = LocalTime.of(15, 15),
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
        )
    }
}
