package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.local.WorkoutSessionEntity
import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.Exercise
import com.example.data.model.FitnessLevel
import com.example.data.model.WorkoutDay
import com.example.data.model.WorkoutDayType
import com.example.data.repository.ExerciseData
import com.example.service.PoseRepCounter
import com.example.service.VoiceCoach
import com.example.ui.animation.ExerciseAnimator
import com.example.ui.components.CameraRepCounterView
import com.example.ui.components.ExerciseGuideModal
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class SessionPhase {
    WARMUP,
    MAIN_WORKOUT,
    COOLDOWN,
    COMPLETED
}

enum class StepState {
    TUTORIAL_GUIDE,
    PREPARE_COUNTDOWN,
    ACTIVE,
    REST_INTERVAL
}

@Composable
fun WorkoutPlayerScreen(
    workoutDay: WorkoutDay,
    language: AppLanguage,
    fitnessLevel: FitnessLevel,
    customRestSeconds: Int,
    voiceCoach: VoiceCoach,
    initialSession: WorkoutSessionEntity? = null,
    hasLearnedExercise: (String) -> Boolean,
    onSaveLearned: (String, Boolean) -> Unit,
    onSaveIncompleteSession: (Int, String, String, Int, Int, Int, Int, Int, Int) -> Unit,
    onFinishWorkoutSession: (Int, String, Int, Int, Int, Int) -> Unit,
    onClose: () -> Unit
) {
    val isHindi = language == AppLanguage.HINDI

    // 1. Curated Exercise Lists for all 3 phases
    val warmupExercises = remember(workoutDay) {
        if (workoutDay.type == WorkoutDayType.WORKOUT) {
            workoutDay.warmupExerciseIds.map { ExerciseData.getExercise(it) }
        } else {
            emptyList()
        }
    }
    val mainExercises = remember(workoutDay) {
        workoutDay.exerciseIds.map { ExerciseData.getExercise(it) }
    }
    val cooldownExercises = remember(workoutDay) {
        if (workoutDay.type == WorkoutDayType.WORKOUT) {
            workoutDay.cooldownExerciseIds.map { ExerciseData.getExercise(it) }
        } else {
            emptyList()
        }
    }

    // 2. Session Phase & Exercise Navigation State
    var currentPhase by remember {
        val initialPhaseStr = initialSession?.currentPhase ?: if (warmupExercises.isNotEmpty()) "WARMUP" else "MAIN_WORKOUT"
        mutableStateOf(
            try {
                SessionPhase.valueOf(initialPhaseStr)
            } catch (_: Exception) {
                if (warmupExercises.isNotEmpty()) SessionPhase.WARMUP else SessionPhase.MAIN_WORKOUT
            }
        )
    }

    var currentExerciseIndex by remember { mutableIntStateOf(initialSession?.exerciseIndex ?: 0) }

    // List of exercises for current active phase
    val currentPhaseExercises = when (currentPhase) {
        SessionPhase.WARMUP -> warmupExercises
        SessionPhase.MAIN_WORKOUT -> mainExercises
        SessionPhase.COOLDOWN -> cooldownExercises
        SessionPhase.COMPLETED -> emptyList()
    }

    val currentExercise = currentPhaseExercises.getOrNull(currentExerciseIndex) ?: mainExercises.first()

    // 3. Sets, Reps & Timers
    val totalSetsForExercise = remember(currentExercise, fitnessLevel, currentPhase) {
        if (currentPhase != SessionPhase.MAIN_WORKOUT) 1
        else when (fitnessLevel) {
            FitnessLevel.BEGINNER -> (currentExercise.defaultSets - 1).coerceAtLeast(2)
            FitnessLevel.INTERMEDIATE -> currentExercise.defaultSets
            FitnessLevel.ADVANCED -> currentExercise.defaultSets + 1
        }
    }

    val targetReps = remember(currentExercise, fitnessLevel, currentPhase) {
        if (currentExercise.isTimed) 0
        else when (fitnessLevel) {
            FitnessLevel.BEGINNER -> (currentExercise.defaultReps - 2).coerceAtLeast(6)
            FitnessLevel.INTERMEDIATE -> currentExercise.defaultReps
            FitnessLevel.ADVANCED -> currentExercise.defaultReps + 4
        }
    }

    var currentSetNumber by remember { mutableIntStateOf(initialSession?.currentSet ?: 1) }
    var currentRepCount by remember { mutableIntStateOf(initialSession?.currentRep ?: 0) }
    var timedSecondsRemaining by remember {
        mutableIntStateOf(
            if (currentExercise.defaultDurationSeconds > 0) currentExercise.defaultDurationSeconds else 30
        )
    }
    var restSecondsRemaining by remember { mutableIntStateOf(customRestSeconds) }
    var prepCountdown by remember { mutableIntStateOf(3) }

    // Duration counters
    var elapsedWarmupSeconds by remember { mutableIntStateOf(initialSession?.elapsedWarmupSeconds ?: 0) }
    var elapsedMainSeconds by remember { mutableIntStateOf(initialSession?.elapsedMainSeconds ?: 0) }
    var elapsedCooldownSeconds by remember { mutableIntStateOf(initialSession?.elapsedCooldownSeconds ?: 0) }

    var isPaused by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isAutoCountEnabled by remember { mutableStateOf(true) }

    var stepState by remember {
        val needsTutorial = currentPhase == SessionPhase.MAIN_WORKOUT && !hasLearnedExercise(currentExercise.id)
        mutableStateOf(if (needsTutorial) StepState.TUTORIAL_GUIDE else StepState.PREPARE_COUNTDOWN)
    }

    // Pose detector for auto-counting
    val poseCounter = remember {
        PoseRepCounter(
            onRepCounted = { countedReps ->
                currentRepCount = countedReps
                if (countedReps > 0) {
                    if (countedReps == targetReps - 1) {
                        voiceCoach.speak(if (isHindi) "बस एक और!" else "One more!", flush = false)
                    } else if (countedReps % 3 == 0 || countedReps == targetReps) {
                        voiceCoach.speak(if (isHindi) "$countedReps रेप हो गए" else "$countedReps reps", flush = false)
                    }
                }
            }
        )
    }

    BackHandler {
        if (currentPhase == SessionPhase.COMPLETED) {
            onClose()
        } else {
            showExitDialog = true
        }
    }

    // Phase Timer Loop
    LaunchedEffect(currentPhase, stepState, isPaused) {
        while (currentPhase != SessionPhase.COMPLETED) {
            delay(1000)
            if (!isPaused && stepState == StepState.ACTIVE) {
                when (currentPhase) {
                    SessionPhase.WARMUP -> elapsedWarmupSeconds++
                    SessionPhase.MAIN_WORKOUT -> elapsedMainSeconds++
                    SessionPhase.COOLDOWN -> elapsedCooldownSeconds++
                    SessionPhase.COMPLETED -> {}
                }
            }
        }
    }

    // Save Incomplete session periodically for seamless resuming
    LaunchedEffect(currentPhase, currentExerciseIndex, currentSetNumber, currentRepCount) {
        if (currentPhase != SessionPhase.COMPLETED) {
            onSaveIncompleteSession(
                workoutDay.day,
                workoutDay.titleEn,
                currentPhase.name,
                currentExerciseIndex,
                currentSetNumber,
                currentRepCount,
                elapsedWarmupSeconds,
                elapsedMainSeconds,
                elapsedCooldownSeconds
            )
        }
    }

    // PREPARE COUNTDOWN (3, 2, 1, START)
    LaunchedEffect(stepState, currentPhase, currentExerciseIndex, currentSetNumber) {
        if (stepState == StepState.PREPARE_COUNTDOWN) {
            voiceCoach.speakGetReady(language)
            prepCountdown = 3
            while (prepCountdown > 0) {
                delay(1000)
                prepCountdown--
                if (prepCountdown > 0) {
                    voiceCoach.speakCountdown(prepCountdown)
                }
            }
            voiceCoach.speakStart(language)
            currentRepCount = 0
            poseCounter.reset()
            timedSecondsRemaining = if (currentExercise.defaultDurationSeconds > 0) currentExercise.defaultDurationSeconds else 30
            stepState = StepState.ACTIVE
        }
    }

    // AUTOMATIC TIMED EXERCISE LOOP (Warm-up, Cooldown, or Timed Main e.g. Plank)
    LaunchedEffect(stepState, isPaused, timedSecondsRemaining, currentPhase) {
        val isTimedActivity = currentExercise.isTimed || currentPhase == SessionPhase.WARMUP || currentPhase == SessionPhase.COOLDOWN
        if (stepState == StepState.ACTIVE && isTimedActivity && !isPaused) {
            if (timedSecondsRemaining > 0) {
                delay(1000)
                timedSecondsRemaining--
                if (timedSecondsRemaining in 1..3) {
                    voiceCoach.speakCountdown(timedSecondsRemaining)
                }
                if (timedSecondsRemaining == 15 && currentExercise.defaultDurationSeconds >= 30) {
                    voiceCoach.speakHalfway(language)
                }
            } else {
                handleExerciseStepCompletion(
                    currentPhase = currentPhase,
                    exerciseIndex = currentExerciseIndex,
                    exercisesList = currentPhaseExercises,
                    currentSet = currentSetNumber,
                    totalSets = totalSetsForExercise,
                    onNextWarmupOrCooldown = { nextIndex ->
                        currentExerciseIndex = nextIndex
                        stepState = StepState.PREPARE_COUNTDOWN
                    },
                    onWarmupCompleteMoveToMain = {
                        voiceCoach.speak(if (isHindi) "वार्म-अप पूरा हुआ! अब मुख्य वर्कआउट शुरू करते हैं।" else "Warm-up complete! Let's start the main workout.", flush = true)
                        currentPhase = SessionPhase.MAIN_WORKOUT
                        currentExerciseIndex = 0
                        currentSetNumber = 1
                        stepState = StepState.PREPARE_COUNTDOWN
                    },
                    onNextSet = {
                        currentSetNumber++
                        currentRepCount = 0
                        restSecondsRemaining = customRestSeconds
                        voiceCoach.speakRest(customRestSeconds, language)
                        stepState = StepState.REST_INTERVAL
                    },
                    onNextMainExercise = { nextIndex ->
                        onSaveLearned(currentExercise.id, true)
                        currentExerciseIndex = nextIndex
                        currentSetNumber = 1
                        currentRepCount = 0
                        restSecondsRemaining = customRestSeconds
                        val next = mainExercises[nextIndex]
                        voiceCoach.speakNextExercise(if (isHindi) next.nameHi else next.nameEn, language)
                        val needsTutorial = !hasLearnedExercise(next.id)
                        stepState = if (needsTutorial) StepState.TUTORIAL_GUIDE else StepState.REST_INTERVAL
                    },
                    onMainCompleteMoveToCooldown = {
                        onSaveLearned(currentExercise.id, true)
                        if (cooldownExercises.isNotEmpty()) {
                            voiceCoach.speak(if (isHindi) "मुख्य वर्कआउट पूरा हुआ! अब शांत कूल-डाउन शुरू करते हैं।" else "Main workout complete! Starting cooldown stretches.", flush = true)
                            currentPhase = SessionPhase.COOLDOWN
                            currentExerciseIndex = 0
                            stepState = StepState.PREPARE_COUNTDOWN
                        } else {
                            currentPhase = SessionPhase.COMPLETED
                            voiceCoach.speakWorkoutComplete(language)
                            onFinishWorkoutSession(
                                workoutDay.day,
                                workoutDay.titleEn,
                                elapsedWarmupSeconds,
                                elapsedMainSeconds,
                                elapsedCooldownSeconds,
                                mainExercises.size + warmupExercises.size
                            )
                        }
                    },
                    onCooldownCompleteFinishDay = {
                        currentPhase = SessionPhase.COMPLETED
                        voiceCoach.speakWorkoutComplete(language)
                        onFinishWorkoutSession(
                            workoutDay.day,
                            workoutDay.titleEn,
                            elapsedWarmupSeconds,
                            elapsedMainSeconds,
                            elapsedCooldownSeconds,
                            mainExercises.size + warmupExercises.size + cooldownExercises.size
                        )
                    }
                )
            }
        }
    }

    // AUTOMATIC TARGET REPS COMPLETION
    LaunchedEffect(stepState, currentRepCount, targetReps) {
        if (stepState == StepState.ACTIVE && currentPhase == SessionPhase.MAIN_WORKOUT && !currentExercise.isTimed) {
            if (targetReps > 0 && currentRepCount >= targetReps) {
                voiceCoach.speak(if (isHindi) "सेट पूरा हुआ!" else "Set complete!", flush = true)
                handleExerciseStepCompletion(
                    currentPhase = currentPhase,
                    exerciseIndex = currentExerciseIndex,
                    exercisesList = currentPhaseExercises,
                    currentSet = currentSetNumber,
                    totalSets = totalSetsForExercise,
                    onNextWarmupOrCooldown = {},
                    onWarmupCompleteMoveToMain = {},
                    onNextSet = {
                        currentSetNumber++
                        currentRepCount = 0
                        restSecondsRemaining = customRestSeconds
                        voiceCoach.speakRest(customRestSeconds, language)
                        stepState = StepState.REST_INTERVAL
                    },
                    onNextMainExercise = { nextIndex ->
                        onSaveLearned(currentExercise.id, true)
                        currentExerciseIndex = nextIndex
                        currentSetNumber = 1
                        currentRepCount = 0
                        restSecondsRemaining = customRestSeconds
                        val next = mainExercises[nextIndex]
                        voiceCoach.speakNextExercise(if (isHindi) next.nameHi else next.nameEn, language)
                        val needsTutorial = !hasLearnedExercise(next.id)
                        stepState = if (needsTutorial) StepState.TUTORIAL_GUIDE else StepState.REST_INTERVAL
                    },
                    onMainCompleteMoveToCooldown = {
                        onSaveLearned(currentExercise.id, true)
                        if (cooldownExercises.isNotEmpty()) {
                            voiceCoach.speak(if (isHindi) "मुख्य वर्कआउट पूरा हुआ! अब शांत कूल-डाउन शुरू करते हैं।" else "Main workout complete! Starting cooldown stretches.", flush = true)
                            currentPhase = SessionPhase.COOLDOWN
                            currentExerciseIndex = 0
                            stepState = StepState.PREPARE_COUNTDOWN
                        } else {
                            currentPhase = SessionPhase.COMPLETED
                            voiceCoach.speakWorkoutComplete(language)
                            onFinishWorkoutSession(
                                workoutDay.day,
                                workoutDay.titleEn,
                                elapsedWarmupSeconds,
                                elapsedMainSeconds,
                                elapsedCooldownSeconds,
                                mainExercises.size + warmupExercises.size
                            )
                        }
                    },
                    onCooldownCompleteFinishDay = {}
                )
            }
        }
    }

    // REST INTERVAL COUNTDOWN
    LaunchedEffect(stepState, isPaused, restSecondsRemaining) {
        if (stepState == StepState.REST_INTERVAL && !isPaused) {
            if (restSecondsRemaining > 0) {
                delay(1000)
                restSecondsRemaining--
                if (restSecondsRemaining in 1..3) {
                    voiceCoach.speakCountdown(restSecondsRemaining)
                }
            } else {
                stepState = StepState.PREPARE_COUNTDOWN
            }
        }
    }

    // UI RENDER
    when (currentPhase) {
        SessionPhase.COMPLETED -> {
            WorkoutCompleteSummaryScreen(
                dayNumber = workoutDay.day,
                warmupSeconds = elapsedWarmupSeconds,
                mainSeconds = elapsedMainSeconds,
                cooldownSeconds = elapsedCooldownSeconds,
                exercisesCount = mainExercises.size + warmupExercises.size + cooldownExercises.size,
                language = language,
                onHomeClicked = onClose
            )
        }

        else -> {
            when (stepState) {
                StepState.TUTORIAL_GUIDE -> {
                    ExerciseGuideModal(
                        exercise = currentExercise,
                        language = language,
                        fitnessLevel = fitnessLevel,
                        onReadyClicked = {
                            onSaveLearned(currentExercise.id, true)
                            stepState = StepState.PREPARE_COUNTDOWN
                        },
                        onCloseClicked = {
                            stepState = StepState.PREPARE_COUNTDOWN
                        }
                    )
                }

                StepState.PREPARE_COUNTDOWN -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GymDarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Phase badge
                            Surface(
                                color = when (currentPhase) {
                                    SessionPhase.WARMUP -> GymCyanSecondary.copy(alpha = 0.2f)
                                    SessionPhase.MAIN_WORKOUT -> GymOrangePrimary.copy(alpha = 0.2f)
                                    SessionPhase.COOLDOWN -> GymGreenSuccess.copy(alpha = 0.2f)
                                    else -> GymDarkSurface
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        when (currentPhase) {
                                            SessionPhase.WARMUP -> GymCyanSecondary
                                            SessionPhase.MAIN_WORKOUT -> GymOrangePrimary
                                            SessionPhase.COOLDOWN -> GymGreenSuccess
                                            else -> GymCardBorder
                                        }
                                    )
                                )
                            ) {
                                Text(
                                    text = when (currentPhase) {
                                        SessionPhase.WARMUP -> if (isHindi) "चरण 1: वार्म-अप" else "STEP 1: WARM-UP"
                                        SessionPhase.MAIN_WORKOUT -> if (isHindi) "चरण 2: मुख्य वर्कआउट" else "STEP 2: MAIN WORKOUT"
                                        SessionPhase.COOLDOWN -> if (isHindi) "चरण 3: कूल-डाउन" else "STEP 3: COOLDOWN"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }

                            Text(
                                text = Localization.get("get_ready", language),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = GymOrangePrimary
                            )

                            Text(
                                text = if (isHindi) currentExercise.nameHi else currentExercise.nameEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GymTextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(GymDarkSurface)
                                    .border(2.dp, GymOrangePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (prepCountdown > 0) "$prepCountdown" else Localization.get("start", language),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GymOrangePrimary
                                )
                            }
                        }
                    }
                }

                StepState.REST_INTERVAL -> {
                    val nextExercise = currentPhaseExercises.getOrNull(currentExerciseIndex)
                    val restProgress = (restSecondsRemaining.toFloat() / customRestSeconds).coerceIn(0f, 1f)

                    Scaffold(containerColor = GymDarkBackground) { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "REST",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = GymCyanSecondary
                            )

                            // Circular Rest Countdown Ring
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(190.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color = GymDarkSurface,
                                            style = Stroke(width = 10.dp.toPx())
                                        )
                                        drawArc(
                                            color = GymCyanSecondary,
                                            startAngle = -90f,
                                            sweepAngle = 360f * restProgress,
                                            useCenter = false,
                                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "00:${restSecondsRemaining.toString().padStart(2, '0')}",
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GymTextPrimary
                                        )
                                        Text(
                                            text = "seconds",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GymTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedButton(
                                        onClick = { restSecondsRemaining += 10 },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GymTextPrimary),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(GymCardBorder)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("rest_add_10s_btn")
                                    ) {
                                        Text("+10s")
                                    }
                                    Button(
                                        onClick = {
                                            restSecondsRemaining = 0
                                            stepState = StepState.PREPARE_COUNTDOWN
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("rest_skip_btn")
                                    ) {
                                        Text("SKIP REST", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            if (nextExercise != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GymCardBackground),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "NEXT EXERCISE:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GymTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isHindi) nextExercise.nameHi else nextExercise.nameEn,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = GymTextPrimary
                                            )
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GymOrangePrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                StepState.ACTIVE -> {
                    Scaffold(
                        topBar = {
                            Surface(
                                color = GymDarkSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GymDivider)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { showExitDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = GymTextSecondary)
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        // Section Badge
                                        Surface(
                                            color = when (currentPhase) {
                                                SessionPhase.WARMUP -> GymCyanSecondary.copy(alpha = 0.2f)
                                                SessionPhase.MAIN_WORKOUT -> GymOrangePrimary.copy(alpha = 0.2f)
                                                SessionPhase.COOLDOWN -> GymGreenSuccess.copy(alpha = 0.2f)
                                                else -> GymCardBackground
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = when (currentPhase) {
                                                    SessionPhase.WARMUP -> if (isHindi) "वार्म-अप" else "SECTION: WARM-UP"
                                                    SessionPhase.MAIN_WORKOUT -> if (isHindi) "मुख्य वर्कआउट" else "SECTION: MAIN WORKOUT"
                                                    SessionPhase.COOLDOWN -> if (isHindi) "कूल-डाउन" else "SECTION: COOLDOWN"
                                                    else -> ""
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (currentPhase) {
                                                    SessionPhase.WARMUP -> GymCyanSecondary
                                                    SessionPhase.MAIN_WORKOUT -> GymOrangePrimary
                                                    SessionPhase.COOLDOWN -> GymGreenSuccess
                                                    else -> GymTextSecondary
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "Exercise ${currentExerciseIndex + 1} / ${currentPhaseExercises.size}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GymTextPrimary
                                        )
                                    }

                                    IconButton(
                                        onClick = { stepState = StepState.TUTORIAL_GUIDE },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Guide", tint = GymCyanSecondary)
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            Surface(
                                color = GymDarkSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GymDivider)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    // Primary Step Completion Button
                                    Button(
                                        onClick = {
                                            if (currentExercise.isTimed || currentPhase != SessionPhase.MAIN_WORKOUT) {
                                                timedSecondsRemaining = 0
                                            } else {
                                                currentRepCount = targetReps
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("action_workout_button")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val btnLabel = when {
                                            currentPhase == SessionPhase.WARMUP -> if (isHindi) "अगला वार्म-अप व्यायाम" else "Next Warm-Up Exercise"
                                            currentPhase == SessionPhase.COOLDOWN -> if (isHindi) "अगला स्ट्रेच" else "Next Cooldown Stretch"
                                            currentExercise.isTimed -> Localization.get("complete_exercise", language)
                                            else -> "${Localization.get("complete_set", language)} (${currentRepCount}/$targetReps)"
                                        }
                                        Text(text = btnLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Secondary Nav Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (currentExerciseIndex > 0) {
                                                    currentExerciseIndex--
                                                    stepState = StepState.PREPARE_COUNTDOWN
                                                }
                                            },
                                            enabled = currentExerciseIndex > 0
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Previous",
                                                tint = if (currentExerciseIndex > 0) GymTextPrimary else GymTextMuted
                                            )
                                        }

                                        TextButton(onClick = { isPaused = !isPaused }) {
                                            Icon(
                                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                contentDescription = null,
                                                tint = GymOrangePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isPaused) Localization.get("resume_workout_btn", language) else Localization.get("pause_workout", language),
                                                color = GymOrangePrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (currentExercise.isTimed || currentPhase != SessionPhase.MAIN_WORKOUT) {
                                                    timedSecondsRemaining = 0
                                                } else {
                                                    currentRepCount = targetReps
                                                }
                                            }
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Skip", tint = GymTextPrimary)
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = GymDarkBackground
                    ) { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Exercise Name Header
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isHindi) currentExercise.nameHi else currentExercise.nameEn,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GymTextPrimary,
                                    textAlign = TextAlign.Center
                                )

                                if (currentPhase == SessionPhase.MAIN_WORKOUT) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format(
                                            Localization.get("set_x_of_y", language),
                                            currentSetNumber,
                                            totalSetsForExercise
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GymOrangePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Exercise Animation Container
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GymCardBackground),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .border(1.dp, GymCardBorder, RoundedCornerShape(20.dp))
                            ) {
                                ExerciseAnimator(
                                    animationType = currentExercise.animationType,
                                    isStaticTimed = currentExercise.isTimed || currentPhase != SessionPhase.MAIN_WORKOUT,
                                    showControls = true,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Dynamic Counter Section
                            if (currentPhase == SessionPhase.MAIN_WORKOUT && !currentExercise.isTimed) {
                                // REPETITION EXERCISE: Live Camera Preview & Auto Rep Counter
                                CameraRepCounterView(
                                    exerciseId = currentExercise.id,
                                    targetReps = targetReps,
                                    currentReps = currentRepCount,
                                    isAutoCountEnabled = isAutoCountEnabled,
                                    poseCounter = poseCounter,
                                    language = language,
                                    onAutoCountToggled = { isAutoCountEnabled = it },
                                    onManualIncrement = {
                                        currentRepCount = (currentRepCount + 1).coerceAtMost(targetReps)
                                        poseCounter.setManualRep(currentRepCount)
                                    },
                                    onManualDecrement = {
                                        currentRepCount = (currentRepCount - 1).coerceAtLeast(0)
                                        poseCounter.setManualRep(currentRepCount)
                                    }
                                )

                                // Digital Rep Counter Display
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
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$currentRepCount",
                                                    fontSize = 40.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GymOrangePrimary
                                                )
                                                Text(
                                                    text = Localization.get("reps", language),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GymTextSecondary
                                                )
                                            }
                                            Text(text = "/", fontSize = 32.sp, color = GymTextMuted)
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$targetReps",
                                                    fontSize = 40.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GymTextPrimary
                                                )
                                                Text(
                                                    text = "Target Reps",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GymTextSecondary
                                                )
                                            }
                                        }

                                        // Rep Progress Bar
                                        val repProgressFraction = if (targetReps > 0) (currentRepCount.toFloat() / targetReps).coerceIn(0f, 1f) else 0f
                                        LinearProgressIndicator(
                                            progress = { repProgressFraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = GymOrangePrimary,
                                            trackColor = GymDarkSurface
                                        )
                                    }
                                }
                            } else {
                                // TIMED EXERCISE: Countdown clock
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
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "00:${timedSecondsRemaining.toString().padStart(2, '0')}",
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GymGreenSuccess
                                        )
                                        Text(
                                            text = if (currentPhase == SessionPhase.WARMUP) "Warm-Up Timer"
                                            else if (currentPhase == SessionPhase.COOLDOWN) "Cooldown Stretch Timer"
                                            else "Hold Time",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GymTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = Localization.get("quit_workout", language),
                    fontWeight = FontWeight.Bold,
                    color = GymTextPrimary
                )
            },
            text = {
                Text(
                    text = if (isHindi)
                        "क्या आप वाकई रुकना चाहते हैं? आपकी प्रगति सुरक्षित है, आप बाद में इसे यहीं से जारी रख सकते हैं।"
                    else
                        "Your progress is saved! You can resume from this exact exercise anytime.",
                    color = GymTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onClose()
                    }
                ) {
                    Text(Localization.get("confirm", language), color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(Localization.get("cancel", language), color = GymTextSecondary)
                }
            },
            containerColor = GymCardBackground
        )
    }
}

private inline fun handleExerciseStepCompletion(
    currentPhase: SessionPhase,
    exerciseIndex: Int,
    exercisesList: List<Exercise>,
    currentSet: Int,
    totalSets: Int,
    crossinline onNextWarmupOrCooldown: (Int) -> Unit,
    crossinline onWarmupCompleteMoveToMain: () -> Unit,
    crossinline onNextSet: () -> Unit,
    crossinline onNextMainExercise: (Int) -> Unit,
    crossinline onMainCompleteMoveToCooldown: () -> Unit,
    crossinline onCooldownCompleteFinishDay: () -> Unit
) {
    when (currentPhase) {
        SessionPhase.WARMUP -> {
            if (exerciseIndex < exercisesList.size - 1) {
                onNextWarmupOrCooldown(exerciseIndex + 1)
            } else {
                onWarmupCompleteMoveToMain()
            }
        }

        SessionPhase.MAIN_WORKOUT -> {
            if (currentSet < totalSets) {
                onNextSet()
            } else {
                if (exerciseIndex < exercisesList.size - 1) {
                    onNextMainExercise(exerciseIndex + 1)
                } else {
                    onMainCompleteMoveToCooldown()
                }
            }
        }

        SessionPhase.COOLDOWN -> {
            if (exerciseIndex < exercisesList.size - 1) {
                onNextWarmupOrCooldown(exerciseIndex + 1)
            } else {
                onCooldownCompleteFinishDay()
            }
        }

        SessionPhase.COMPLETED -> {}
    }
}

@Composable
fun WorkoutCompleteSummaryScreen(
    dayNumber: Int,
    warmupSeconds: Int,
    mainSeconds: Int,
    cooldownSeconds: Int,
    exercisesCount: Int,
    language: AppLanguage,
    onHomeClicked: () -> Unit
) {
    val totalSeconds = warmupSeconds + mainSeconds + cooldownSeconds
    val totalMins = totalSeconds / 60
    val totalSecs = totalSeconds % 60

    Scaffold(containerColor = GymDarkBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GymGreenSuccess.copy(alpha = 0.15f))
                        .border(2.dp, GymGreenSuccess, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = GymGreenSuccess,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WORKOUT COMPLETE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = GymTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DAY ${dayNumber.toString().padStart(2, '0')} / 30",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GymOrangePrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“Another workout done. Keep building the habit.”",
                    style = MaterialTheme.typography.bodySmall,
                    color = GymTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Summary Stats Card
            Card(
                colors = CardDefaults.cardColors(containerColor = GymCardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GymCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Time", color = GymTextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${totalMins}:${totalSecs.toString().padStart(2, '0')}",
                            color = GymTextPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Exercises Completed", color = GymTextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "$exercisesCount",
                            color = GymTextPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(color = GymDivider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Warm-Up", color = GymTextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(text = "${warmupSeconds / 60}m ${warmupSeconds % 60}s", color = GymCyanSecondary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Main Workout", color = GymTextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(text = "${mainSeconds / 60}m ${mainSeconds % 60}s", color = GymOrangePrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Cooldown", color = GymTextSecondary, style = MaterialTheme.typography.bodySmall)
                        Text(text = "${cooldownSeconds / 60}m ${cooldownSeconds % 60}s", color = GymGreenSuccess, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Action Buttons: DONE and VIEW PROGRESS
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onHomeClicked,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("summary_back_home_btn")
                ) {
                    Text(
                        text = "DONE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
