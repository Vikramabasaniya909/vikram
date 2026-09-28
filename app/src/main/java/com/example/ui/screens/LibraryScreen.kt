package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.Difficulty
import com.example.data.model.EquipmentType
import com.example.data.model.Exercise
import com.example.data.model.MuscleGroup
import com.example.data.repository.ExerciseData
import com.example.ui.components.ExerciseGuideModal
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    language: AppLanguage,
    onOpenExerciseGuide: (Exercise) -> Unit
) {
    val isHindi = language == AppLanguage.HINDI
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MuscleGroup?>(null) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    var selectedEquipment by remember { mutableStateOf<EquipmentType?>(null) }

    var activeGuideExercise by remember { mutableStateOf<Exercise?>(null) }

    val filteredExercises = remember(searchQuery, selectedCategory, selectedDifficulty, selectedEquipment) {
        ExerciseData.exercises.filter { ex ->
            val matchesQuery = searchQuery.isBlank() ||
                    ex.nameEn.contains(searchQuery, ignoreCase = true) ||
                    ex.nameHi.contains(searchQuery, ignoreCase = true) ||
                    ex.primaryMusclesEn.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = selectedCategory == null || ex.category == selectedCategory
            val matchesDifficulty = selectedDifficulty == null || ex.difficulty == selectedDifficulty
            val matchesEquipment = selectedEquipment == null || ex.equipment == selectedEquipment

            matchesQuery && matchesCategory && matchesDifficulty && matchesEquipment
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "EXERCISE LIBRARY",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = GymTextPrimary
            )
            Text(
                text = "Explore technique, breathing, and muscle activation",
                style = MaterialTheme.typography.bodySmall,
                color = GymTextSecondary
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(Localization.get("search_exercises", language), color = GymTextMuted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = GymTextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = GymTextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GymDarkSurface,
                unfocusedContainerColor = GymDarkSurface,
                focusedBorderColor = GymOrangePrimary,
                unfocusedBorderColor = GymCardBorder,
                focusedTextColor = GymTextPrimary,
                unfocusedTextColor = GymTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("exercise_search_field")
        )

        // Categories Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(Localization.get("all", language)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymOrangePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = GymDarkSurface,
                        labelColor = GymTextSecondary
                    )
                )
            }
            items(MuscleGroup.values()) { cat ->
                val label = when (cat) {
                    MuscleGroup.FULL_BODY -> Localization.get("cat_full_body", language)
                    MuscleGroup.CHEST -> Localization.get("cat_chest", language)
                    MuscleGroup.BACK -> Localization.get("cat_back", language)
                    MuscleGroup.SHOULDERS -> Localization.get("cat_shoulders", language)
                    MuscleGroup.ARMS -> Localization.get("cat_arms", language)
                    MuscleGroup.LEGS -> Localization.get("cat_legs", language)
                    MuscleGroup.GLUTES -> Localization.get("cat_glutes", language)
                    MuscleGroup.CORE -> Localization.get("cat_core", language)
                    MuscleGroup.WARMUP -> Localization.get("cat_warmup", language)
                    MuscleGroup.COOLDOWN -> Localization.get("cat_cooldown", language)
                    else -> cat.name
                }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = if (selectedCategory == cat) null else cat
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymOrangePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = GymDarkSurface,
                        labelColor = GymTextSecondary
                    )
                )
            }
        }

        // Exercises List
        if (filteredExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.get("no_exercises_found", language),
                    color = GymTextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredExercises, key = { it.id }) { exercise ->
                    ExerciseLibraryCard(
                        exercise = exercise,
                        language = language,
                        onClick = {
                            activeGuideExercise = exercise
                        }
                    )
                }
            }
        }
    }

    // Modal Guide if user clicked an exercise
    activeGuideExercise?.let { ex ->
        ExerciseGuideModal(
            exercise = ex,
            language = language,
            onCloseClicked = { activeGuideExercise = null }
        )
    }
}

@Composable
private fun ExerciseLibraryCard(
    exercise: Exercise,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val isHindi = language == AppLanguage.HINDI

    Card(
        colors = CardDefaults.cardColors(containerColor = GymCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("exercise_card_${exercise.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isHindi) exercise.nameHi else exercise.nameEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = when (exercise.difficulty) {
                            Difficulty.BEGINNER -> GymGreenSuccess.copy(alpha = 0.15f)
                            Difficulty.INTERMEDIATE -> GymOrangePrimary.copy(alpha = 0.15f)
                            Difficulty.ADVANCED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isHindi) {
                                when (exercise.difficulty) {
                                    Difficulty.BEGINNER -> "शुरुआती"
                                    Difficulty.INTERMEDIATE -> "मध्यम"
                                    Difficulty.ADVANCED -> "उन्नत"
                                }
                            } else exercise.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = when (exercise.difficulty) {
                                Difficulty.BEGINNER -> GymGreenSuccess
                                Difficulty.INTERMEDIATE -> GymOrangePrimary
                                Difficulty.ADVANCED -> Color(0xFFF87171)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${if (isHindi) "लक्षित: " else "Target: "}${exercise.primaryMusclesEn.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GymTextSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = GymCyanSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
