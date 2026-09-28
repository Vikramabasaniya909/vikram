package com.example.data.model

enum class MuscleGroup {
    FULL_BODY,
    CHEST,
    BACK,
    SHOULDERS,
    ARMS,
    LEGS,
    GLUTES,
    CORE,
    WARMUP,
    COOLDOWN
}

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class FitnessLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class EquipmentType {
    NONE,
    DUMBBELLS,
    RESISTANCE_BAND,
    PULL_UP_BAR,
    MAT,
    FULL
}

enum class WorkoutDayType {
    WORKOUT,
    ACTIVE_RECOVERY,
    REST_DAY
}

enum class ExerciseAnimationType {
    PUSH_UP,
    INCLINE_PUSH_UP,
    KNEE_PUSH_UP,
    WIDE_PUSH_UP,
    DIAMOND_PUSH_UP,
    SQUAT,
    SUMO_SQUAT,
    LUNGE,
    WALKING_LUNGE,
    BULGARIAN_SPLIT_SQUAT,
    GLUTE_BRIDGE,
    SINGLE_LEG_GLUTE_BRIDGE,
    SUPERMAN,
    BIRD_DOG,
    PLANK,
    SIDE_PLANK,
    DEAD_BUG,
    SHOULDER_TAP,
    PIKE_PUSH_UP,
    ROW,
    SHOULDER_PRESS,
    LATERAL_RAISE,
    FRONT_RAISE,
    CALF_RAISE,
    ROMANIAN_DEADLIFT,
    MOUNTAIN_CLIMBERS,
    JUMPING_JACKS,
    HIGH_KNEES,
    BURPEES,
    BICEPS_CURL,
    TRICEPS_EXTENSION,
    CRUNCH,
    LEG_RAISE,
    BICYCLE_CRUNCH,
    BEAR_CRAWL,
    ARM_CIRCLES,
    MARCH_IN_PLACE,
    SHOULDER_ROLLS,
    TORSO_ROTATIONS,
    HIP_CIRCLES,
    WARMUP_STRETCH,
    COOLDOWN_STRETCH
}

data class StartingPositionInfo(
    val feetEn: String,
    val feetHi: String,
    val handsEn: String,
    val handsHi: String,
    val backEn: String,
    val backHi: String,
    val headNeckEn: String,
    val headNeckHi: String,
    val coreEn: String,
    val coreHi: String
)

data class ExerciseGuide(
    val startingPosition: StartingPositionInfo,
    val stepsEn: List<String>,
    val stepsHi: List<String>,
    val breathingEn: String,
    val breathingHi: String,
    val commonMistakesEn: List<String>,
    val commonMistakesHi: List<String>,
    val safetyTipsEn: List<String>,
    val safetyTipsHi: List<String>,
    val beginnerVariationEn: String,
    val beginnerVariationHi: String,
    val progressionEn: String,
    val progressionHi: String
)

data class Exercise(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val category: MuscleGroup,
    val primaryMusclesEn: List<String>,
    val primaryMusclesHi: List<String>,
    val secondaryMusclesEn: List<String>,
    val secondaryMusclesHi: List<String>,
    val difficulty: Difficulty,
    val equipment: EquipmentType,
    val defaultSets: Int = 3,
    val defaultReps: Int = 12,
    val defaultDurationSeconds: Int = 0, // >0 for timed exercises like Plank
    val defaultRestSeconds: Int = 45,
    val isTimed: Boolean = false,
    val equipmentAlternativeId: String? = null,
    val animationType: ExerciseAnimationType,
    val guide: ExerciseGuide
)

data class WorkoutDay(
    val day: Int,
    val type: WorkoutDayType,
    val titleEn: String,
    val titleHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val exerciseIds: List<String> = emptyList(),
    val warmupExerciseIds: List<String> = listOf(
        "warmup_arm_circles",
        "warmup_march_in_place",
        "warmup_shoulder_rolls",
        "warmup_torso_rotations",
        "warmup_hip_circles"
    ),
    val cooldownExerciseIds: List<String> = listOf(
        "cooldown_shoulder_stretch",
        "cooldown_quad_stretch",
        "cooldown_hamstring_stretch",
        "cooldown_calm_breathing"
    ),
    val estimatedDurationMinutes: Int = 20,
    val difficulty: Difficulty = Difficulty.BEGINNER
)
