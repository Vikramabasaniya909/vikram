package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.WorkoutDay
import com.example.data.model.WorkoutDayType
import com.example.data.repository.ExerciseData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    currentDayNumber: Int,
    completedDaySet: Set<Int>,
    language: AppLanguage,
    onStartWorkoutDay: (WorkoutDay) -> Unit
) {
    val isHindi = language == AppLanguage.HINDI
    var selectedDayForModal by remember { mutableStateOf<WorkoutDay?>(null) }
    var isGridView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "30 DAY PROGRAM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = GymTextPrimary
                )
                Text(
                    text = if (isHindi) "आपकी संपूर्ण शरीर फिटनेस यात्रा" else "Your Full Body Journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = GymTextSecondary
                )
            }

            // View Mode Toggle (Timeline vs Grid)
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymDarkSurface)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                    contentDescription = "Toggle View",
                    tint = GymOrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Legend Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GymDarkSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = GymGreenSuccess, label = Localization.get("completed", language))
            LegendItem(color = GymOrangePrimary, label = Localization.get("current", language))
            LegendItem(color = GymCyanSecondary, label = Localization.get("active_recovery", language))
            LegendItem(color = GymTextMuted, label = Localization.get("locked", language))
        }

        if (isGridView) {
            // 30-Day Compact Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ExerciseData.programDays) { day ->
                    val isCompleted = completedDaySet.contains(day.day)
                    val isCurrent = day.day == currentDayNumber
                    val isLocked = day.day > currentDayNumber && !isCompleted

                    DayGridTile(
                        day = day,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        isLocked = isLocked,
                        onClick = { selectedDayForModal = day }
                    )
                }
            }
        } else {
            // Vertical Timeline View
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ExerciseData.programDays) { day ->
                    val isCompleted = completedDaySet.contains(day.day)
                    val isCurrent = day.day == currentDayNumber
                    val isLocked = day.day > currentDayNumber && !isCompleted

                    DayTimelineCard(
                        day = day,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        isLocked = isLocked,
                        language = language,
                        onClick = { selectedDayForModal = day },
                        onStartWorkout = { onStartWorkoutDay(day) }
                    )
                }
            }
        }
    }

    // Day Details Modal
    if (selectedDayForModal != null) {
        val day = selectedDayForModal!!
        val isCompleted = completedDaySet.contains(day.day)

        ModalBottomSheet(
            onDismissRequest = { selectedDayForModal = null },
            containerColor = GymDarkSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day ${day.day}: ${if (isHindi) day.titleHi else day.titleEn}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    if (isCompleted) {
                        Surface(color = GymGreenSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = Localization.get("completed", language),
                                color = GymGreenSuccess,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isHindi) day.descriptionHi else day.descriptionEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextSecondary
                )

                if (day.type == WorkoutDayType.WORKOUT) {
                    Text(
                        text = "${day.exerciseIds.size} Exercises Planned:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GymCyanSecondary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        day.exerciseIds.forEachIndexed { index, exId ->
                            val ex = ExerciseData.getExercise(exId)
                            Text(
                                text = "${index + 1}. ${if (isHindi) ex.nameHi else ex.nameEn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GymTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val target = day
                        selectedDayForModal = null
                        onStartWorkoutDay(target)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("plan_modal_start_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCompleted) "REPEAT WORKOUT" else "START WORKOUT",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DayTimelineCard(
    day: WorkoutDay,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    onStartWorkout: () -> Unit
) {
    val isHindi = language == AppLanguage.HINDI
    val cardBg = if (isCurrent) GymCardBackground else GymDarkSurface
    val borderColor = when {
        isCurrent -> GymOrangePrimary.copy(alpha = 0.8f)
        isCompleted -> GymGreenSuccess.copy(alpha = 0.4f)
        day.type == WorkoutDayType.ACTIVE_RECOVERY -> GymCyanSecondary.copy(alpha = 0.4f)
        else -> GymCardBorder
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("timeline_day_${day.day}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge Icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> GymGreenSuccess.copy(alpha = 0.2f)
                                    isCurrent -> GymOrangePrimary.copy(alpha = 0.2f)
                                    day.type == WorkoutDayType.ACTIVE_RECOVERY -> GymCyanSecondary.copy(alpha = 0.2f)
                                    else -> Color(0xFF1E293B)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCompleted -> Icon(Icons.Default.Check, contentDescription = null, tint = GymGreenSuccess, modifier = Modifier.size(16.dp))
                            isCurrent -> Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(16.dp))
                            day.type == WorkoutDayType.ACTIVE_RECOVERY -> Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = GymCyanSecondary, modifier = Modifier.size(16.dp))
                            isLocked -> Icon(Icons.Default.Lock, contentDescription = null, tint = GymTextMuted, modifier = Modifier.size(14.dp))
                            else -> Text(text = "${day.day}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GymTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "DAY ${day.day.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) GymOrangePrimary else GymTextPrimary
                    )

                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GymOrangePrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CURRENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Type Badge
                Text(
                    text = when (day.type) {
                        WorkoutDayType.WORKOUT -> "${day.estimatedDurationMinutes} min"
                        WorkoutDayType.ACTIVE_RECOVERY -> "Recovery"
                        WorkoutDayType.REST_DAY -> "Rest"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (day.type) {
                        WorkoutDayType.WORKOUT -> GymTextSecondary
                        WorkoutDayType.ACTIVE_RECOVERY -> GymCyanSecondary
                        WorkoutDayType.REST_DAY -> GymGreenSuccess
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = if (isHindi) day.titleHi else day.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GymTextPrimary
            )

            // Description / Exercises overview
            Text(
                text = if (isHindi) day.descriptionHi else day.descriptionEn,
                style = MaterialTheme.typography.bodySmall,
                color = GymTextSecondary,
                maxLines = 2
            )

            // Current day prominent inline CTA button
            if (isCurrent) {
                Button(
                    onClick = onStartWorkout,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "START TODAY'S WORKOUT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DayGridTile(
    day: WorkoutDay,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isCompleted -> GymGreenSuccess.copy(alpha = 0.15f)
        isCurrent -> GymOrangePrimary.copy(alpha = 0.2f)
        day.type == WorkoutDayType.ACTIVE_RECOVERY -> GymCyanSecondary.copy(alpha = 0.15f)
        isLocked -> GymDarkSurface
        else -> GymCardBackground
    }

    val borderColor = when {
        isCompleted -> GymGreenSuccess.copy(alpha = 0.5f)
        isCurrent -> GymOrangePrimary
        day.type == WorkoutDayType.ACTIVE_RECOVERY -> GymCyanSecondary.copy(alpha = 0.5f)
        else -> GymCardBorder
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("day_tile_${day.day}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                isCompleted -> Icon(Icons.Default.Check, contentDescription = null, tint = GymGreenSuccess, modifier = Modifier.size(18.dp))
                isLocked -> Icon(Icons.Default.Lock, contentDescription = null, tint = GymTextMuted, modifier = Modifier.size(16.dp))
                else -> {
                    Text(
                        text = "${day.day}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isCurrent) GymOrangePrimary else GymTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = GymTextSecondary)
    }
}
