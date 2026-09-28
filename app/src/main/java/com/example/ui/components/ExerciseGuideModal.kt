package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.Exercise
import com.example.data.model.FitnessLevel
import com.example.ui.animation.ExerciseAnimator
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseGuideModal(
    exercise: Exercise,
    language: AppLanguage,
    fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    onReadyClicked: (() -> Unit)? = null,
    onCloseClicked: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isHindi = language == AppLanguage.HINDI
    val guide = exercise.guide

    // Volume calculation based on fitness level
    val sets = when (fitnessLevel) {
        FitnessLevel.BEGINNER -> (exercise.defaultSets - 1).coerceAtLeast(2)
        FitnessLevel.INTERMEDIATE -> exercise.defaultSets
        FitnessLevel.ADVANCED -> exercise.defaultSets + 1
        else -> exercise.defaultSets
    }
    val reps = when (fitnessLevel) {
        FitnessLevel.BEGINNER -> (exercise.defaultReps - 2).coerceAtLeast(6)
        FitnessLevel.INTERMEDIATE -> exercise.defaultReps
        FitnessLevel.ADVANCED -> exercise.defaultReps + 4
        else -> exercise.defaultReps
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) exercise.nameHi else exercise.nameEn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCloseClicked,
                        modifier = Modifier.testTag("close_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.get("close", language),
                            tint = GymTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GymDarkSurface
                )
            )
        },
        bottomBar = {
            if (onReadyClicked != null) {
                Surface(
                    color = GymDarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GymDivider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReadyClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GymOrangePrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("im_ready_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Localization.get("im_ready_btn", language),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. VISUAL DEMONSTRATION WITH CONTROLS
            Card(
                colors = CardDefaults.cardColors(containerColor = GymCardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(1.dp, GymCardBorder, RoundedCornerShape(20.dp))
            ) {
                ExerciseAnimator(
                    animationType = exercise.animationType,
                    isStaticTimed = exercise.isTimed,
                    showControls = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Quick Stats Badges (Difficulty, Target Muscles, Volume, Rest)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Difficulty
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (isHindi) {
                                when (exercise.difficulty.name) {
                                    "BEGINNER" -> "शुरुआती"
                                    "INTERMEDIATE" -> "मध्यम"
                                    else -> "उन्नत"
                                }
                            } else exercise.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = Color(0xFF67E8F9),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF67E8F9), modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF16253B))
                )

                // Sets & Reps
                AssistChip(
                    onClick = {},
                    label = {
                        val volText = if (exercise.isTimed) {
                            "${exercise.defaultDurationSeconds}s"
                        } else {
                            "$sets x $reps"
                        }
                        Text(text = volText, color = Color(0xFFFDE047), style = MaterialTheme.typography.labelSmall)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFFFDE047), modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF262314))
                )

                // Rest
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "${exercise.defaultRestSeconds}s ${Localization.get("rest_time", language)}",
                            color = Color(0xFF86EFAC),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF86EFAC), modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF14291B))
                )
            }

            // 2. TARGET MUSCLES
            GuideSectionCard(
                title = Localization.get("target_muscles", language),
                icon = Icons.Default.AccessibilityNew,
                iconTint = Color(0xFF38BDF8)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${Localization.get("primary_muscles", language)}:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        val primary = if (isHindi) exercise.primaryMusclesHi else exercise.primaryMusclesEn
                        Text(
                            text = primary.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${Localization.get("secondary_muscles", language)}:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        val secondary = if (isHindi) exercise.secondaryMusclesHi else exercise.secondaryMusclesEn
                        Text(
                            text = secondary.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // 3. HOW TO DO IT (Numbered Steps)
            GuideSectionCard(
                title = Localization.get("how_to_do_it", language),
                icon = Icons.Default.FormatListNumbered,
                iconTint = Color(0xFFFF9800)
            ) {
                val steps = if (isHindi) guide.stepsHi else guide.stepsEn
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5722)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. STARTING POSITION (Feet, Hands, Back, Head/Neck, Core)
            GuideSectionCard(
                title = Localization.get("starting_position", language),
                icon = Icons.Default.Place,
                iconTint = Color(0xFF4ADE80)
            ) {
                val pos = guide.startingPosition
                PositionRow(label = Localization.get("feet_pos", language), text = if (isHindi) pos.feetHi else pos.feetEn)
                PositionRow(label = Localization.get("hands_pos", language), text = if (isHindi) pos.handsHi else pos.handsEn)
                PositionRow(label = Localization.get("back_pos", language), text = if (isHindi) pos.backHi else pos.backEn)
                PositionRow(label = Localization.get("head_neck_pos", language), text = if (isHindi) pos.headNeckHi else pos.headNeckEn)
                PositionRow(label = Localization.get("core_pos", language), text = if (isHindi) pos.coreHi else pos.coreEn)
            }

            // 5. BREATHING
            GuideSectionCard(
                title = Localization.get("breathing", language),
                icon = Icons.Default.Air,
                iconTint = Color(0xFF60A5FA)
            ) {
                Text(
                    text = if (isHindi) guide.breathingHi else guide.breathingEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0)
                )
            }

            // 6. COMMON MISTAKES
            GuideSectionCard(
                title = Localization.get("common_mistakes", language),
                icon = Icons.Default.WarningAmber,
                iconTint = Color(0xFFEF4444)
            ) {
                val mistakes = if (isHindi) guide.commonMistakesHi else guide.commonMistakesEn
                mistakes.forEach { mistake ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mistake,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFCA5A5),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 7. VARIATIONS (Beginner & Progression)
            GuideSectionCard(
                title = "${Localization.get("beginner_variation", language)} & ${Localization.get("progression_variation", language)}",
                icon = Icons.Default.SwapCalls,
                iconTint = Color(0xFFA855F7)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${Localization.get("beginner_variation", language)}: ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = if (isHindi) guide.beginnerVariationHi else guide.beginnerVariationEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${Localization.get("progression_variation", language)}: ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF97316)
                        )
                        Text(
                            text = if (isHindi) guide.progressionHi else guide.progressionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // 8. SAFETY TIPS
            GuideSectionCard(
                title = Localization.get("safety_tips", language),
                icon = Icons.Default.HealthAndSafety,
                iconTint = Color(0xFF10B981)
            ) {
                val tips = if (isHindi) guide.safetyTipsHi else guide.safetyTipsEn
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Localization.get("medical_disclaimer", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GuideSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GymCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun PositionRow(label: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• $label: ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E8F0)
        )
    }
}
