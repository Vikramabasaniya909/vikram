package com.example.service

import androidx.compose.runtime.*
import com.example.data.model.Exercise

enum class RepState {
    STARTING_POSITION,
    INFLECTION_POSITION, // e.g. bottom of push-up / bottom of squat
    RETURNED_POSITION
}

data class PoseDetectionStatus(
    val isBodyDetected: Boolean = false,
    val feedbackMessageEn: String = "Position yourself in front of camera",
    val feedbackMessageHi: String = "कैमरे के सामने पूरी तरह आएं",
    val currentMovementProgress: Float = 0f // 0f to 1f
)

class PoseRepCounter(
    private val onRepCounted: (Int) -> Unit
) {
    private var repState = RepState.STARTING_POSITION
    private var lastRepTimestamp = 0L
    private val minRepDurationMs = 800L // Debounce cooldown
    private var baseCentroidY = -1f
    private var smoothedDeltaY = 0f
    private var isCalibrated = false
    private var calibrationFrames = 0
    private var framesWithoutBody = 0

    var totalReps = 0
        private set

    var currentStatus by mutableStateOf(PoseDetectionStatus())
        private set

    fun reset() {
        repState = RepState.STARTING_POSITION
        lastRepTimestamp = 0L
        baseCentroidY = -1f
        smoothedDeltaY = 0f
        isCalibrated = false
        calibrationFrames = 0
        framesWithoutBody = 0
        totalReps = 0
        currentStatus = PoseDetectionStatus(
            isBodyDetected = false,
            feedbackMessageEn = "Position yourself in camera view",
            feedbackMessageHi = "कैमरे के सामने आएं"
        )
    }

    fun setManualRep(reps: Int) {
        totalReps = reps.coerceAtLeast(0)
    }

    fun processFrame(
        exerciseId: String,
        detectedMotionLevel: Float, // 0f to 1f
        verticalCentroid: Float,    // 0f (top) to 1f (bottom)
        bodyAreaFraction: Float     // fraction of frame occupied by human body
    ) {
        val now = System.currentTimeMillis()

        // 1. Check if body is in frame
        if (bodyAreaFraction < 0.08f) {
            framesWithoutBody++
            if (framesWithoutBody > 10) {
                currentStatus = PoseDetectionStatus(
                    isBodyDetected = false,
                    feedbackMessageEn = "Body not detected • Step back slightly",
                    feedbackMessageHi = "शरीर दिखाई नहीं दे रहा • थोड़ा पीछे हटें",
                    currentMovementProgress = 0f
                )
            }
            return
        }

        framesWithoutBody = 0

        // 2. Calibration of neutral starting position
        if (!isCalibrated) {
            calibrationFrames++
            if (baseCentroidY < 0f) baseCentroidY = verticalCentroid
            else baseCentroidY = (baseCentroidY * 0.85f) + (verticalCentroid * 0.15f)

            if (calibrationFrames >= 12) {
                isCalibrated = true
                currentStatus = PoseDetectionStatus(
                    isBodyDetected = true,
                    feedbackMessageEn = "Ready! Begin movement",
                    feedbackMessageHi = "तैयार! व्यायाम शुरू करें",
                    currentMovementProgress = 0f
                )
            } else {
                currentStatus = PoseDetectionStatus(
                    isBodyDetected = true,
                    feedbackMessageEn = "Body detected • Stand still",
                    feedbackMessageHi = "शरीर डिटेक्ट हुआ • स्थिर रहें",
                    currentMovementProgress = 0f
                )
            }
            return
        }

        // 3. Normalized movement displacement relative to neutral baseline
        val deltaY = verticalCentroid - baseCentroidY
        smoothedDeltaY = (smoothedDeltaY * 0.6f) + (deltaY * 0.4f)

        // Exercise-specific displacement threshold and state machine
        val isPushUp = exerciseId.contains("push_up")
        val isSquat = exerciseId.contains("squat")
        val isLunge = exerciseId.contains("lunge")
        val isPress = exerciseId.contains("press")

        val (downThreshold, upThreshold) = when {
            isPushUp -> Pair(0.09f, 0.035f) // Torso drops in camera frame
            isSquat -> Pair(0.12f, 0.045f)  // Hips descend significantly
            isLunge -> Pair(0.10f, 0.04f)
            isPress -> Pair(-0.10f, -0.03f) // Hands/arms rise upward
            else -> Pair(0.08f, 0.03f)
        }

        val progress = if (isPress) {
            ((-smoothedDeltaY) / kotlin.math.abs(downThreshold)).coerceIn(0f, 1f)
        } else {
            (smoothedDeltaY / downThreshold).coerceIn(0f, 1f)
        }

        // State Machine: STARTING_POSITION -> INFLECTION_POSITION -> RETURNED_POSITION
        if (isPress) {
            // Movement goes upward (negative delta Y in image coordinates)
            when (repState) {
                RepState.STARTING_POSITION -> {
                    if (smoothedDeltaY < downThreshold) {
                        repState = RepState.INFLECTION_POSITION
                        currentStatus = PoseDetectionStatus(
                            isBodyDetected = true,
                            feedbackMessageEn = "Good! Lower with control",
                            feedbackMessageHi = "बढ़िया! धीरे-धीरे नीचे लाएं",
                            currentMovementProgress = progress
                        )
                    } else {
                        currentStatus = PoseDetectionStatus(
                            isBodyDetected = true,
                            feedbackMessageEn = "Press straight up",
                            feedbackMessageHi = "ऊपर उठाएं",
                            currentMovementProgress = progress
                        )
                    }
                }
                RepState.INFLECTION_POSITION -> {
                    if (smoothedDeltaY > upThreshold) {
                        // Returned to start! Check cooldown
                        if (now - lastRepTimestamp >= minRepDurationMs) {
                            totalReps++
                            lastRepTimestamp = now
                            onRepCounted(totalReps)
                            currentStatus = PoseDetectionStatus(
                                isBodyDetected = true,
                                feedbackMessageEn = "Rep $totalReps complete!",
                                feedbackMessageHi = "रैप $totalReps पूरा हुआ!",
                                currentMovementProgress = 0f
                            )
                        }
                        repState = RepState.STARTING_POSITION
                    }
                }
                else -> repState = RepState.STARTING_POSITION
            }
        } else {
            // Standard lowering movements (Push-ups, squats, lunges)
            when (repState) {
                RepState.STARTING_POSITION -> {
                    if (smoothedDeltaY > downThreshold) {
                        repState = RepState.INFLECTION_POSITION
                        currentStatus = PoseDetectionStatus(
                            isBodyDetected = true,
                            feedbackMessageEn = if (isPushUp) "Chest down • Now push up!" else "Deep squat • Rise up!",
                            feedbackMessageHi = if (isPushUp) "नीचे आ गए • अब ऊपर उठें!" else "गहरा स्क्वाट • अब खड़े हों!",
                            currentMovementProgress = 1f
                        )
                    } else {
                        val cueEn = if (isPushUp) "Keep back straight • Lower slowly" else "Keep chest up • Lower hips"
                        val cueHi = if (isPushUp) "पीठ सीधी रखें • धीरे नीचे जाएं" else "छाती सीधी रखें • नीचे बैठें"
                        currentStatus = PoseDetectionStatus(
                            isBodyDetected = true,
                            feedbackMessageEn = cueEn,
                            feedbackMessageHi = cueHi,
                            currentMovementProgress = progress
                        )
                    }
                }
                RepState.INFLECTION_POSITION -> {
                    if (smoothedDeltaY < upThreshold) {
                        // Full rep completed! Check cooldown
                        if (now - lastRepTimestamp >= minRepDurationMs) {
                            totalReps++
                            lastRepTimestamp = now
                            onRepCounted(totalReps)
                            currentStatus = PoseDetectionStatus(
                                isBodyDetected = true,
                                feedbackMessageEn = "Rep $totalReps counted!",
                                feedbackMessageHi = "रैप $totalReps पूरा हुआ!",
                                currentMovementProgress = 0f
                            )
                        }
                        repState = RepState.STARTING_POSITION
                    } else {
                        currentStatus = PoseDetectionStatus(
                            isBodyDetected = true,
                            feedbackMessageEn = "Push all the way up to finish rep",
                            feedbackMessageHi = "पूरा ऊपर आकर रैप समाप्त करें",
                            currentMovementProgress = 0.5f
                        )
                    }
                }
                else -> repState = RepState.STARTING_POSITION
            }
        }
    }
}
