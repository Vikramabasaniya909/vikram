package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WorkoutRecordEntity
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    records: List<WorkoutRecordEntity>,
    completedDaysCount: Int,
    currentStreak: Int,
    language: AppLanguage,
    onStartFirstWorkout: () -> Unit = {}
) {
    val isHindi = language == AppLanguage.HINDI
    val totalWorkouts = records.size
    val totalSeconds = records.sumOf { it.durationSeconds }
    val totalMinutes = totalSeconds / 60
    val totalExercises = records.sumOf { it.exercisesCompleted }
    val progressFraction = (completedDaysCount / 30f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "progress_ring"
    )
    val progressPercent = (progressFraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header
        Column {
            Text(
                text = "YOUR FITNESS JOURNEY",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = GymTextPrimary
            )
            Text(
                text = Localization.get("progress_desc", language),
                style = MaterialTheme.typography.bodySmall,
                color = GymTextSecondary
            )
        }

        // 2. Large 30-Day Progress Ring Card
        Card(
            colors = CardDefaults.cardColors(containerColor = GymCardBackground),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymCardBorder, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "30 DAY PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = GymTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedDaysCount / 30",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = GymTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedDaysCount of 30 days completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = GymCyanSecondary
                    )
                }

                // Large Circular Progress Ring
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = GymDarkSurface,
                            style = Stroke(width = 8.dp.toPx())
                        )
                        drawArc(
                            color = GymOrangePrimary,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = GymTextPrimary
                    )
                }
            }
        }

        // 3. Compact Stats Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = Localization.get("current_streak", language),
                value = "$currentStreak",
                unit = if (isHindi) "दिन" else "Days",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = GymOrangePrimary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = Localization.get("total_time", language),
                value = "$totalMinutes",
                unit = Localization.get("mins", language),
                icon = Icons.Default.Timer,
                iconTint = GymCyanSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = Localization.get("total_workouts", language),
                value = "$totalWorkouts",
                unit = "done",
                icon = Icons.Default.CheckCircle,
                iconTint = GymGreenSuccess,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = Localization.get("total_exercises", language),
                value = "$totalExercises",
                unit = "done",
                icon = Icons.Default.FitnessCenter,
                iconTint = Color(0xFFA78BFA),
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Workout History Section
        Text(
            text = "WORKOUT HISTORY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = GymTextSecondary
        )

        if (records.isEmpty()) {
            // Polished Empty State
            Card(
                colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, GymCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(GymCardBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = GymTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No workout history yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Localization.get("no_history_yet", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStartFirstWorkout,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "START WORKOUT",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryItemCard(record = record, language = language)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GymCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = GymTextPrimary)
                Text(text = unit, style = MaterialTheme.typography.labelSmall, color = GymTextSecondary, modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = GymTextSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun HistoryItemCard(
    record: WorkoutRecordEntity,
    language: AppLanguage
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateStr = sdf.format(Date(record.timestamp))
    val mins = record.durationSeconds / 60
    val secs = record.durationSeconds % 60

    Card(
        colors = CardDefaults.cardColors(containerColor = GymCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
            .testTag("history_card_${record.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (record.dayNumber in 1..30) "Day ${record.dayNumber}: ${record.workoutTitle}" else record.workoutTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
                Text(
                    text = "${mins}m ${secs}s",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GymOrangePrimary
                )
            }

            // Phase Breakdown
            if (record.warmupDurationSeconds > 0 || record.mainDurationSeconds > 0 || record.cooldownDurationSeconds > 0) {
                Text(
                    text = "Warm-Up: ${record.warmupDurationSeconds / 60}m ${record.warmupDurationSeconds % 60}s • Main: ${record.mainDurationSeconds / 60}m ${record.mainDurationSeconds % 60}s • Cooldown: ${record.cooldownDurationSeconds / 60}m ${record.cooldownDurationSeconds % 60}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = GymTextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = GymTextMuted)
                Text(
                    text = "${record.exercisesCompleted} exercises",
                    style = MaterialTheme.typography.labelSmall,
                    color = GymCyanSecondary
                )
            }
        }
    }
}
