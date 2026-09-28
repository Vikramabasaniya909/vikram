package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.FitnessLevel
import com.example.data.repository.ExerciseData
import com.example.ui.components.ExerciseGuideModal
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppTab
import com.example.viewmodel.GymViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GymAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GymAppRoot(viewModel: GymViewModel) {
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val dayProgressList by viewModel.dayProgressList.collectAsStateWithLifecycle()
    val workoutRecords by viewModel.workoutRecords.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val activeGuideExercise by viewModel.activeGuideExercise.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()

    val currentLang = remember(userPrefs?.language) {
        try {
            AppLanguage.valueOf(userPrefs?.language ?: "ENGLISH")
        } catch (_: Exception) {
            AppLanguage.ENGLISH
        }
    }

    val fitnessLevel = remember(userPrefs?.fitnessLevel) {
        try {
            FitnessLevel.valueOf(userPrefs?.fitnessLevel ?: "BEGINNER")
        } catch (_: Exception) {
            FitnessLevel.BEGINNER
        }
    }

    val currentDay = userPrefs?.currentDay ?: 1
    val completedDaySet = remember(dayProgressList) {
        dayProgressList.filter { it.isCompleted }.map { it.dayNumber }.toSet()
    }
    val isCurrentDayCompleted = completedDaySet.contains(currentDay)
    val completedDaysCount = completedDaySet.size

    // Calculate streak and stats from workout records
    val currentStreak = remember(workoutRecords) {
        if (workoutRecords.isEmpty()) 0 else {
            val uniqueDays = workoutRecords.map { it.timestamp / (1000 * 60 * 60 * 24) }.distinct()
            uniqueDays.size.coerceAtLeast(1)
        }
    }

    val totalWorkoutTimeMinutes = remember(workoutRecords) {
        workoutRecords.sumOf { it.durationSeconds } / 60
    }
    val totalWorkoutsCount = workoutRecords.size
    val totalExercisesCount = remember(workoutRecords) {
        workoutRecords.sumOf { it.exercisesCompleted }
    }

    // 1. ACTIVE WORKOUT OVERLAY
    if (activeWorkout != null) {
        WorkoutPlayerScreen(
            workoutDay = activeWorkout!!,
            language = currentLang,
            fitnessLevel = fitnessLevel,
            customRestSeconds = userPrefs?.restTimeSeconds ?: 45,
            voiceCoach = viewModel.voiceCoach,
            initialSession = activeSession,
            hasLearnedExercise = { viewModel.isExerciseLearned(it) },
            onSaveLearned = { id, easy -> viewModel.setExerciseLearned(id, easy) },
            onSaveIncompleteSession = { day, title, phase, exIdx, set, rep, wSec, mSec, cSec ->
                viewModel.saveIncompleteSession(day, title, phase, exIdx, set, rep, wSec, mSec, cSec)
            },
            onFinishWorkoutSession = { day, title, wSec, mSec, cSec, count ->
                viewModel.finishWorkoutSession(day, title, wSec, mSec, cSec, count)
            },
            onClose = {
                viewModel.closeWorkout()
            }
        )
        return
    }

    // 2. ACTIVE STANDALONE GUIDE OVERLAY
    if (activeGuideExercise != null) {
        ExerciseGuideModal(
            exercise = activeGuideExercise!!,
            language = currentLang,
            fitnessLevel = fitnessLevel,
            onCloseClicked = { viewModel.closeExerciseGuide() }
        )
        return
    }

    // 3. MAIN TABBED INTERFACE
    BackHandler(enabled = currentTab != AppTab.HOME) {
        viewModel.switchTab(AppTab.HOME)
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = GymDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GymDivider)
            ) {
                NavigationBar(
                    containerColor = GymDarkSurface,
                    contentColor = GymTextPrimary,
                    tonalElevation = 0.dp
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { viewModel.switchTab(AppTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = Localization.get("nav_home", currentLang), modifier = Modifier.size(22.dp)) },
                        label = { Text(Localization.get("nav_home", currentLang), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = GymTextMuted,
                            unselectedTextColor = GymTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // 30-Day Plan
                    NavigationBarItem(
                        selected = currentTab == AppTab.PLAN,
                        onClick = { viewModel.switchTab(AppTab.PLAN) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = Localization.get("nav_plan", currentLang), modifier = Modifier.size(22.dp)) },
                        label = { Text(Localization.get("nav_plan", currentLang), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = GymTextMuted,
                            unselectedTextColor = GymTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_plan")
                    )

                    // Exercise Library
                    NavigationBarItem(
                        selected = currentTab == AppTab.LIBRARY,
                        onClick = { viewModel.switchTab(AppTab.LIBRARY) },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = Localization.get("nav_library", currentLang), modifier = Modifier.size(22.dp)) },
                        label = { Text(Localization.get("nav_library", currentLang), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = GymTextMuted,
                            unselectedTextColor = GymTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_library")
                    )

                    // Learn
                    NavigationBarItem(
                        selected = currentTab == AppTab.LEARN,
                        onClick = { viewModel.switchTab(AppTab.LEARN) },
                        icon = { Icon(Icons.Default.School, contentDescription = Localization.get("nav_learn", currentLang), modifier = Modifier.size(22.dp)) },
                        label = { Text(Localization.get("nav_learn", currentLang), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = GymTextMuted,
                            unselectedTextColor = GymTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_learn")
                    )

                    // Progress
                    NavigationBarItem(
                        selected = currentTab == AppTab.PROGRESS,
                        onClick = { viewModel.switchTab(AppTab.PROGRESS) },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = Localization.get("nav_progress", currentLang), modifier = Modifier.size(22.dp)) },
                        label = { Text(Localization.get("nav_progress", currentLang), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = GymTextMuted,
                            unselectedTextColor = GymTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_progress")
                    )

                    // Settings
                    NavigationBarItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.switchTab(AppTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = Localization.get("nav_settings", currentLang), modifier = Modifier.size(22.dp)) },
                        label = { Text(Localization.get("nav_settings", currentLang), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = GymTextMuted,
                            unselectedTextColor = GymTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        },
        containerColor = GymDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        currentDayNumber = currentDay,
                        isCurrentDayCompleted = isCurrentDayCompleted,
                        completedDaysCount = completedDaysCount,
                        currentStreak = currentStreak,
                        totalWorkoutTimeMinutes = totalWorkoutTimeMinutes,
                        totalWorkoutsCount = totalWorkoutsCount,
                        totalExercisesCount = totalExercisesCount,
                        activeSession = activeSession,
                        language = currentLang,
                        onStartWorkout = { day -> viewModel.startWorkout(day) },
                        onResumeSession = { session -> viewModel.resumeWorkoutSession(session) },
                        onStartQuickWorkout = { viewModel.startWorkout(ExerciseData.quickWorkout) },
                        onNavigateToPlan = { viewModel.switchTab(AppTab.PLAN) }
                    )
                }

                AppTab.PLAN -> {
                    PlanScreen(
                        currentDayNumber = currentDay,
                        completedDaySet = completedDaySet,
                        language = currentLang,
                        onStartWorkoutDay = { day -> viewModel.startWorkout(day) }
                    )
                }

                AppTab.LIBRARY -> {
                    LibraryScreen(
                        language = currentLang,
                        onOpenExerciseGuide = { ex -> viewModel.openExerciseGuide(ex) }
                    )
                }

                AppTab.LEARN -> {
                    LearnScreen(
                        language = currentLang
                    )
                }

                AppTab.PROGRESS -> {
                    ProgressScreen(
                        records = workoutRecords,
                        completedDaysCount = completedDaysCount,
                        currentStreak = currentStreak,
                        language = currentLang
                    )
                }

                AppTab.SETTINGS -> {
                    SettingsScreen(
                        currentPrefs = userPrefs ?: com.example.data.local.UserPreferenceEntity(),
                        onUpdatePrefs = { updated -> viewModel.updatePreferences(updated) },
                        onResetProgram = { viewModel.resetProgram() }
                    )
                }
            }
        }
    }
}
