package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workout_records")
data class WorkoutRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayNumber: Int,
    val workoutTitle: String,
    val durationSeconds: Int,
    val warmupDurationSeconds: Int = 0,
    val mainDurationSeconds: Int = 0,
    val cooldownDurationSeconds: Int = 0,
    val exercisesCompleted: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "day_progress")
data class DayProgressEntity(
    @PrimaryKey val dayNumber: Int,
    val isCompleted: Boolean,
    val completedAtTimestamp: Long = 0L
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val fitnessLevel: String = "BEGINNER", // BEGINNER, INTERMEDIATE, ADVANCED
    val language: String = "ENGLISH",     // ENGLISH, HINDI
    val preferredDurationMinutes: Int = 20, // 10, 20, 30, 45
    val equipment: String = "NONE",       // NONE, DUMBBELLS, RESISTANCE_BAND, PULL_UP_BAR, FULL
    val restTimeSeconds: Int = 45,
    val voiceEnabled: Boolean = true,
    val autoCountEnabled: Boolean = true, // Default to AUTO COUNT
    val currentDay: Int = 1
)

@Entity(tableName = "learned_exercises")
data class LearnedExerciseEntity(
    @PrimaryKey val exerciseId: String,
    val hasLearned: Boolean = true,
    val feedbackEasy: Boolean = true
)

@Entity(tableName = "active_workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey val id: Int = 1,
    val dayNumber: Int,
    val title: String,
    val currentPhase: String, // "WARMUP", "MAIN_WORKOUT", "COOLDOWN"
    val exerciseIndex: Int,
    val currentSet: Int,
    val currentRep: Int,
    val elapsedWarmupSeconds: Int,
    val elapsedMainSeconds: Int,
    val elapsedCooldownSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GymDao {
    @Query("SELECT * FROM workout_records ORDER BY timestamp DESC")
    fun getAllWorkoutRecords(): Flow<List<WorkoutRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutRecord(record: WorkoutRecordEntity): Long

    @Query("SELECT * FROM day_progress")
    fun getAllDayProgress(): Flow<List<DayProgressEntity>>

    @Query("SELECT * FROM day_progress WHERE dayNumber = :day LIMIT 1")
    suspend fun getDayProgress(day: Int): DayProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDayProgress(progress: DayProgressEntity)

    @Query("DELETE FROM day_progress")
    suspend fun resetAllDayProgress()

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferences(): Flow<UserPreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferencesSync(): UserPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(preferences: UserPreferenceEntity)

    @Query("SELECT * FROM learned_exercises")
    fun getAllLearnedExercises(): Flow<List<LearnedExerciseEntity>>

    @Query("SELECT * FROM learned_exercises WHERE exerciseId = :id LIMIT 1")
    suspend fun getLearnedExercise(id: String): LearnedExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLearnedExercise(learned: LearnedExerciseEntity)

    @Query("SELECT * FROM active_workout_session WHERE id = 1 LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM active_workout_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM active_workout_session")
    suspend fun clearActiveSession()

    @Query("DELETE FROM workout_records")
    suspend fun clearAllHistory()
}

@Database(
    entities = [
        WorkoutRecordEntity::class,
        DayProgressEntity::class,
        UserPreferenceEntity::class,
        LearnedExerciseEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymDao(): GymDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getInstance(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "my_gym_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GymRepository(private val dao: GymDao) {
    val workoutRecords: Flow<List<WorkoutRecordEntity>> = dao.getAllWorkoutRecords()
    val dayProgress: Flow<List<DayProgressEntity>> = dao.getAllDayProgress()
    val userPreferences: Flow<UserPreferenceEntity?> = dao.getUserPreferences()
    val learnedExercises: Flow<List<LearnedExerciseEntity>> = dao.getAllLearnedExercises()
    val activeSession: Flow<WorkoutSessionEntity?> = dao.getActiveSession()

    suspend fun recordFullWorkoutCompleted(
        dayNumber: Int,
        workoutTitle: String,
        warmupDurationSeconds: Int,
        mainDurationSeconds: Int,
        cooldownDurationSeconds: Int,
        exercisesCompleted: Int
    ) {
        val totalDuration = warmupDurationSeconds + mainDurationSeconds + cooldownDurationSeconds

        dao.insertWorkoutRecord(
            WorkoutRecordEntity(
                dayNumber = dayNumber,
                workoutTitle = workoutTitle,
                durationSeconds = totalDuration,
                warmupDurationSeconds = warmupDurationSeconds,
                mainDurationSeconds = mainDurationSeconds,
                cooldownDurationSeconds = cooldownDurationSeconds,
                exercisesCompleted = exercisesCompleted
            )
        )

        // Clear in-progress session
        dao.clearActiveSession()

        // ONLY when the full workout (Warm-up + Main + Cooldown) finishes:
        if (dayNumber in 1..30) {
            dao.saveDayProgress(
                DayProgressEntity(
                    dayNumber = dayNumber,
                    isCompleted = true,
                    completedAtTimestamp = System.currentTimeMillis()
                )
            )
            // Advance current day
            val prefs = dao.getUserPreferencesSync() ?: UserPreferenceEntity()
            if (prefs.currentDay == dayNumber && dayNumber < 30) {
                dao.saveUserPreferences(prefs.copy(currentDay = dayNumber + 1))
            }
        }
    }

    suspend fun saveIncompleteSession(
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
        dao.saveActiveSession(
            WorkoutSessionEntity(
                id = 1,
                dayNumber = dayNumber,
                title = title,
                currentPhase = currentPhase,
                exerciseIndex = exerciseIndex,
                currentSet = currentSet,
                currentRep = currentRep,
                elapsedWarmupSeconds = warmupSeconds,
                elapsedMainSeconds = mainSeconds,
                elapsedCooldownSeconds = cooldownSeconds
            )
        )
    }

    suspend fun clearActiveSession() {
        dao.clearActiveSession()
    }

    suspend fun updateUserPreferences(prefs: UserPreferenceEntity) {
        dao.saveUserPreferences(prefs)
    }

    suspend fun setExerciseLearned(exerciseId: String, easy: Boolean) {
        dao.saveLearnedExercise(LearnedExerciseEntity(exerciseId, true, easy))
    }

    suspend fun resetProgram() {
        dao.resetAllDayProgress()
        dao.clearActiveSession()
        val prefs = dao.getUserPreferencesSync() ?: UserPreferenceEntity()
        dao.saveUserPreferences(prefs.copy(currentDay = 1))
    }
}
