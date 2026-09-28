package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GymDatabase
import com.example.data.local.GymRepository
import com.example.data.local.UserPreferenceEntity
import com.example.data.local.WorkoutRecordEntity
import com.example.data.local.WorkoutSessionEntity
import com.example.data.localization.AppLanguage
import com.example.data.model.Exercise
import com.example.data.model.FitnessLevel
import com.example.data.model.WorkoutDay
import com.example.data.repository.ExerciseData
import com.example.service.VoiceCoach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME,
    PLAN,
    LIBRARY,
    LEARN,
    PROGRESS,
    SETTINGS
}

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GymRepository
    val voiceCoach: VoiceCoach = VoiceCoach(application)

    init {
        val database = GymDatabase.getInstance(application)
        repository = GymRepository(database.gymDao())
    }

    val workoutRecords: StateFlow<List<WorkoutRecordEntity>> = repository.workoutRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayProgressList = repository.dayProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferenceEntity())

    val learnedExercises = repository.learnedExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<WorkoutSessionEntity?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _activeWorkout = MutableStateFlow<WorkoutDay?>(null)
    val activeWorkout: StateFlow<WorkoutDay?> = _activeWorkout.asStateFlow()

    private val _activeGuideExercise = MutableStateFlow<Exercise?>(null)
    val activeGuideExercise: StateFlow<Exercise?> = _activeGuideExercise.asStateFlow()

    fun switchTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun startWorkout(day: WorkoutDay) {
        _activeWorkout.value = day
    }

    fun resumeWorkoutSession(session: WorkoutSessionEntity) {
        val day = ExerciseData.getDay(session.dayNumber)
        _activeWorkout.value = day
    }

    fun closeWorkout() {
        voiceCoach.stop()
        _activeWorkout.value = null
    }

    fun discardSession() {
        viewModelScope.launch {
            repository.clearActiveSession()
        }
        closeWorkout()
    }

    fun openExerciseGuide(exercise: Exercise) {
        _activeGuideExercise.value = exercise
    }

    fun closeExerciseGuide() {
        _activeGuideExercise.value = null
    }

    fun saveIncompleteSession(
        dayNumber: Int,
        title: String,
        currentPhase: String,
        exerciseIndex: Int,
        currentSet: Int,
        currentRep: Int,
        warmupSeconds: Int,
        mainSeconds: Int,
        cooldownSeconds: Int
    ) {
        viewModelScope.launch {
            repository.saveIncompleteSession(
                dayNumber,
                title,
                currentPhase,
                exerciseIndex,
                currentSet,
                currentRep,
                warmupSeconds,
                mainSeconds,
                cooldownSeconds
            )
        }
    }

    fun finishWorkoutSession(
        dayNumber: Int,
        title: String,
        warmupSeconds: Int,
        mainSeconds: Int,
        cooldownSeconds: Int,
        exercisesCount: Int
    ) {
        viewModelScope.launch {
            repository.recordFullWorkoutCompleted(
                dayNumber,
                title,
                warmupSeconds,
                mainSeconds,
                cooldownSeconds,
                exercisesCount
            )
        }
    }

    fun updatePreferences(prefs: UserPreferenceEntity) {
        viewModelScope.launch {
            repository.updateUserPreferences(prefs)
            try {
                val lang = AppLanguage.valueOf(prefs.language)
                voiceCoach.setLanguage(lang)
            } catch (_: Exception) {}
            voiceCoach.isEnabled = prefs.voiceEnabled
        }
    }

    fun setExerciseLearned(exerciseId: String, easy: Boolean) {
        viewModelScope.launch {
            repository.setExerciseLearned(exerciseId, easy)
        }
    }

    fun resetProgram() {
        viewModelScope.launch {
            repository.resetProgram()
        }
    }

    fun isExerciseLearned(exerciseId: String): Boolean {
        return learnedExercises.value.any { it.exerciseId == exerciseId && it.hasLearned }
    }

    override fun onCleared() {
        super.onCleared()
        voiceCoach.shutdown()
    }
}
