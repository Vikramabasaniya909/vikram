package com.example

import com.example.data.localization.AppLanguage
import com.example.data.localization.Localization
import com.example.data.model.WorkoutDayType
import com.example.data.repository.ExerciseData
import com.example.data.repository.LearnData
import com.example.service.PoseRepCounter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun test30DayProgramComplete() {
        assertEquals(30, ExerciseData.programDays.size)
        // Verify days 1 through 30 in exact sequence
        for (i in 1..30) {
            val day = ExerciseData.getDay(i)
            assertEquals(i, day.day)
            assertTrue(day.titleEn.isNotEmpty())
            assertTrue(day.titleHi.isNotEmpty())
        }
    }

    @Test
    fun testUnifiedSessionHasWarmupAndCooldown() {
        // Every workout day has dedicated warm-up and cooldown sequences
        val day1 = ExerciseData.getDay(1)
        assertEquals(WorkoutDayType.WORKOUT, day1.type)
        assertTrue("Day 1 must have warm-up exercises", day1.warmupExerciseIds.isNotEmpty())
        assertTrue("Day 1 must have cooldown exercises", day1.cooldownExerciseIds.isNotEmpty())

        // Verify warm-up exercises have real names
        day1.warmupExerciseIds.forEach { id ->
            val ex = ExerciseData.getExercise(id)
            assertNotEquals("Full Body Warm-Up", ex.nameEn)
            assertTrue("Warm-up exercise name must be specific", ex.nameEn.isNotBlank())
            assertTrue("Warm-up exercise nameHi must be specific", ex.nameHi.isNotBlank())
        }

        // Verify cooldown exercises have real names
        day1.cooldownExerciseIds.forEach { id ->
            val ex = ExerciseData.getExercise(id)
            assertTrue("Cooldown exercise name must be specific", ex.nameEn.isNotBlank())
            assertTrue("Cooldown exercise nameHi must be specific", ex.nameHi.isNotBlank())
        }
    }

    @Test
    fun testAutoRepCounterFullRepCounts() {
        var countedReps = 0
        val counter = PoseRepCounter(
            onRepCounted = { countedReps = it }
        )

        // 1. Calibrate neutral position (feed 15 stationary frames)
        for (i in 1..15) {
            counter.processFrame(
                exerciseId = "push_up",
                detectedMotionLevel = 0.5f,
                verticalCentroid = 0.5f,
                bodyAreaFraction = 0.25f
            )
        }
        assertTrue("Body should be detected", counter.currentStatus.isBodyDetected)
        assertEquals(0, counter.totalReps)

        // 2. Perform a partial movement (delta = 0.04, less than downThreshold 0.09)
        counter.processFrame(
            exerciseId = "push_up",
            detectedMotionLevel = 0.5f,
            verticalCentroid = 0.54f,
            bodyAreaFraction = 0.25f
        )
        // Return to neutral
        counter.processFrame(
            exerciseId = "push_up",
            detectedMotionLevel = 0.5f,
            verticalCentroid = 0.50f,
            bodyAreaFraction = 0.25f
        )
        // Partial movement must NOT count as a rep!
        assertEquals("Partial movement must not count", 0, counter.totalReps)

        // 3. Perform a FULL Push-Up movement (TOP -> DOWN past threshold -> UP to top)
        // Move DOWN over multiple frames (simulating natural smooth video motion)
        for (i in 1..5) {
            counter.processFrame(
                exerciseId = "push_up",
                detectedMotionLevel = 0.8f,
                verticalCentroid = 0.68f, // delta = 0.18 > downThreshold
                bodyAreaFraction = 0.25f
            )
        }
        // Return UP to top over multiple frames
        for (i in 1..5) {
            counter.processFrame(
                exerciseId = "push_up",
                detectedMotionLevel = 0.8f,
                verticalCentroid = 0.50f, // returned to neutral
                bodyAreaFraction = 0.25f
            )
        }

        // Should have counted 1 full repetition!
        assertEquals(1, counter.totalReps)
        assertEquals(1, countedReps)
    }

    @Test
    fun testAutoRepCounterLossOfBodyDoesNotResetReps() {
        val counter = PoseRepCounter(onRepCounted = {})
        counter.setManualRep(5)
        assertEquals(5, counter.totalReps)

        // Simulate camera losing body (bodyAreaFraction near 0 for 12 frames)
        for (i in 1..15) {
            counter.processFrame(
                exerciseId = "push_up",
                detectedMotionLevel = 0f,
                verticalCentroid = 0f,
                bodyAreaFraction = 0.02f
            )
        }

        assertFalse("Body should not be detected", counter.currentStatus.isBodyDetected)
        // Rep count must be preserved!
        assertEquals("Completed reps must never be reset on camera loss", 5, counter.totalReps)
    }

    @Test
    fun testRecoveryDays() {
        // Recovery days per specification: Days 4, 7, 11, 14, 18, 21, 25, 28
        assertEquals(WorkoutDayType.ACTIVE_RECOVERY, ExerciseData.getDay(4).type)
        assertEquals(WorkoutDayType.REST_DAY, ExerciseData.getDay(7).type)
        assertEquals(WorkoutDayType.ACTIVE_RECOVERY, ExerciseData.getDay(11).type)
        assertEquals(WorkoutDayType.REST_DAY, ExerciseData.getDay(14).type)
        assertEquals(WorkoutDayType.ACTIVE_RECOVERY, ExerciseData.getDay(18).type)
        assertEquals(WorkoutDayType.REST_DAY, ExerciseData.getDay(21).type)
        assertEquals(WorkoutDayType.ACTIVE_RECOVERY, ExerciseData.getDay(25).type)
        assertEquals(WorkoutDayType.REST_DAY, ExerciseData.getDay(28).type)
    }

    @Test
    fun testLocalizationBilingualParity() {
        val testKeys = listOf(
            "app_title", "todays_workout", "quick_workout", "program_progress",
            "exercise_details", "im_ready_btn", "how_to_do_it", "starting_position",
            "breathing", "common_mistakes", "get_ready", "start", "rest_time"
        )
        testKeys.forEach { key ->
            val en = Localization.get(key, AppLanguage.ENGLISH)
            val hi = Localization.get(key, AppLanguage.HINDI)
            assertNotEquals("Missing EN translation for $key", key, en)
            assertNotEquals("Missing HI translation for $key", key, hi)
            assertNotEquals("Hindi translation should differ from English for $key", en, hi)
        }
    }
}
