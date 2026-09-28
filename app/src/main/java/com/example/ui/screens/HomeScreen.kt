package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WorkoutSessionEntity
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.WorkoutDay
import com.example.data.model.WorkoutDayType
import com.example.data.repository.ExerciseData
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    currentDayNumber: Int,
    isCurrentDayCompleted: Boolean,
    completedDaysCount: Int,
    currentStreak: Int,
    totalWorkoutTimeMinutes: Int,
    totalWorkoutsCount: Int,
    totalExercisesCount: Int,
    activeSession: WorkoutSessionEntity?,
    language: AppLanguage,
    onStartWorkout: (WorkoutDay) -> Unit,
    onResumeSession: (WorkoutSessionEntity) -> Unit,
    onStartQuickWorkout: () -> Unit,
    onNavigateToPlan: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isHindi = language == AppLanguage.HINDI
    val todayWorkout = ExerciseData.getDay(currentDayNumber)

    // Greeting by time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "good_morning"
            in 12..16 -> "good_afternoon"
            else -> "good_evening"
        }
    }

    // Dynamic quote
    val quotes = listOf("quote_1", "quote_2", "quote_3", "quote_4")
    val selectedQuote = remember(currentDayNumber) {
        quotes[(currentDayNumber - 1).coerceAtLeast(0) % quotes.size]
    }

    val progressFraction = (completedDaysCount / 30f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "progress_anim"
    )
    val progressPercent = (progressFraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. TOP HEADER SECTION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MY GYM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = GymTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Localization.get(greeting, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextSecondary
                )
                Text(
                    text = "“${Localization.get(selectedQuote, language)}”",
                    style = MaterialTheme.typography.labelSmall,
                    color = GymCyanSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Streak Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GymDarkSurface,
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GymOrangePrimary, Color(0xFFFF9800)))),
                modifier = Modifier.testTag("streak_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = GymOrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentStreak ${if (isHindi) "दिन" else "Days"}",
                        fontWeight = FontWeight.Bold,
                        color = GymOrangePrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // 2. CONTINUE WORKOUT CARD (If user has an active unfinished workout)
        if (activeSession != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GymOrangePrimary.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .clickable { onResumeSession(activeSession) }
                    .testTag("resume_workout_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GymOrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = GymOrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Localization.get("continue_workout", language).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GymOrangePrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Day ${activeSession.dayNumber} • ${activeSession.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary
                        )
                        val phaseLabel = when (activeSession.currentPhase) {
                            "WARMUP" -> Localization.get("step_warmup", language)
                            "MAIN_WORKOUT" -> "${Localization.get("step_main", language)} (${activeSession.exerciseIndex + 1})"
                            "COOLDOWN" -> Localization.get("step_cooldown", language)
                            else -> "In Progress"
                        }
                        Text(
                            text = phaseLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = GymTextSecondary
                        )
                    }
                    FilledTonalButton(
                        onClick = { onResumeSession(activeSession) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = GymOrangePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = Localization.get("resume_workout_btn", language),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 3. TODAY'S WORKOUT HERO CARD (The most prominent element)
        Card(
            colors = CardDefaults.cardColors(containerColor = GymCardBackground),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymCardBorder, RoundedCornerShape(20.dp))
                .testTag("todays_workout_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                GymCardBackground,
                                Color(0xFF16253B)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Header Tag Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GymOrangePrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(GymOrangePrimary.copy(alpha = 0.4f), GymOrangePrimary.copy(alpha = 0.4f))))
                        ) {
                            Text(
                                text = "TODAY'S WORKOUT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GymOrangePrimary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (isCurrentDayCompleted) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GymGreenSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Localization.get("completed", language),
                                    color = GymGreenSuccess,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "Day $currentDayNumber / 30",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GymTextSecondary
                            )
                        }
                    }

                    // Workout Title
                    Text(
                        text = if (isHindi) todayWorkout.titleHi else todayWorkout.titleEn,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )

                    // Meta Chips: Duration, Level, Exercises
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GymDarkSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = GymCyanSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${todayWorkout.estimatedDurationMinutes} ${Localization.get("mins", language)}",
                                    color = GymTextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Surface(
                            color = GymDarkSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = todayWorkout.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = GymTextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        if (todayWorkout.type == WorkoutDayType.WORKOUT) {
                            Surface(
                                color = GymDarkSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${todayWorkout.exerciseIds.size} ${if (isHindi) "व्यायाम" else "exercises"}",
                                    color = GymTextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Unified Session Flow Indicators (Warm-up -> Main -> Cooldown)
                    if (todayWorkout.type == WorkoutDayType.WORKOUT) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GymDarkSurface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Warm-Up", color = GymCyanSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GymTextMuted, modifier = Modifier.size(12.dp))
                            Text(text = "Main Workout", color = GymOrangePrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GymTextMuted, modifier = Modifier.size(12.dp))
                            Text(text = "Cooldown", color = GymGreenSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // START WORKOUT BUTTON (Strongest CTA)
                    Button(
                        onClick = { onStartWorkout(todayWorkout) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GymOrangePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_today_workout_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCurrentDayCompleted)
                                if (isHindi) "दिन $currentDayNumber फिर से करें" else "Repeat Day $currentDayNumber"
                            else
                                "START WORKOUT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 4. DAILY 30-DAY PROGRESS SECTION (Circular Progress Indicator)
        Card(
            colors = CardDefaults.cardColors(containerColor = GymCardBackground),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymCardBorder, RoundedCornerShape(18.dp))
                .clickable { onNavigateToPlan() }
                .testTag("program_progress_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
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
                        text = "$completedDaysCount of 30 days completed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current: Day $currentDayNumber",
                        style = MaterialTheme.typography.bodySmall,
                        color = GymCyanSecondary
                    )
                }

                // Smooth Circular Progress Ring
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background track
                        drawCircle(
                            color = GymDarkSurface,
                            style = Stroke(width = 6.dp.toPx())
                        )
                        // Progress sweep
                        drawArc(
                            color = GymOrangePrimary,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                }
            }
        }

        // 5. QUICK STATS (4 compact, clean cards)
        Text(
            text = Localization.get("quick_stats", language).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = GymTextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatItem(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = GymOrangePrimary,
                value = "$currentStreak",
                unit = if (isHindi) "दिन" else "Days",
                label = Localization.get("day_streak", language),
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.Timer,
                iconTint = GymCyanSecondary,
                value = "$totalWorkoutTimeMinutes",
                unit = Localization.get("mins", language),
                label = Localization.get("workout_time", language),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatItem(
                icon = Icons.Default.CheckCircle,
                iconTint = GymGreenSuccess,
                value = "$totalWorkoutsCount",
                unit = "done",
                label = Localization.get("total_workouts", language),
                modifier = Modifier.weight(1f)
            )
            StatItem(
                icon = Icons.Default.FitnessCenter,
                iconTint = Color(0xFFA78BFA),
                value = "$totalExercisesCount",
                unit = "reps",
                label = Localization.get("total_exercises", language),
                modifier = Modifier.weight(1f)
            )
        }

        // 6. MOTIVATION CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GymOrangePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = GymOrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DAILY FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymOrangePrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Localization.get(selectedQuote, language),
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 7. QUICK 10-MIN WORKOUT OPTION
        Card(
            colors = CardDefaults.cardColors(containerColor = GymCardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
                .clickable { onStartQuickWorkout() }
                .testTag("quick_workout_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262014)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Localization.get("quick_workout", language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Text(
                        text = Localization.get("quick_workout_sub", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextSecondary
                    )
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GymTextMuted, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    label: String,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = GymTextPrimary
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = GymTextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = GymTextSecondary,
                maxLines = 1
            )
        }
    }
}
