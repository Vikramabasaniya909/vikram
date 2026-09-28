package com.example.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExerciseAnimationType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExerciseAnimator(
    animationType: ExerciseAnimationType,
    modifier: Modifier = Modifier,
    isStaticTimed: Boolean = false,
    showControls: Boolean = true,
    repProgress: Float? = null // If provided, locks or drives phase directly
) {
    var isPlaying by remember { mutableStateOf(true) }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }
    var isFullScreen by remember { mutableStateOf(false) }
    var replayTrigger by remember { mutableIntStateOf(0) }

    // Natural 3.2 second cycle for 1 controlled rep (1.5s down, 0.4s pause, 1.3s up)
    val baseDurationMs = 3200
    val effectiveDuration = (baseDurationMs / speedMultiplier).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "exercise_anim_$replayTrigger")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(effectiveDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val currentPhase = when {
        !isPlaying -> 0.0f
        repProgress != null -> repProgress
        else -> animatedProgress
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF131B2E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animation Canvas Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ground line & alignment indicator
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawExerciseFrame(
                        type = animationType,
                        phase = currentPhase,
                        isStatic = isStaticTimed
                    )
                }

                // Phase label indicator (e.g. "Lowering", "Pushing Up", "Hold Form")
                val phaseText = getPhaseLabel(animationType, currentPhase, isStaticTimed)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC0B111E),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = phaseText,
                        color = Color(0xFFFFB74D),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Controls Bar
            if (showControls) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Play/Pause
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }

                    // Replay
                    IconButton(
                        onClick = {
                            replayTrigger++
                            isPlaying = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay",
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    // Speed controls: 0.5x, 0.75x, 1.0x
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf(0.5f, 0.75f, 1.0f).forEach { spd ->
                            val selected = speedMultiplier == spd
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFFFF5722) else Color(0xFF1E293B))
                                    .clickable { speedMultiplier = spd }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${spd}x",
                                    color = if (selected) Color.White else Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // Full Screen
                    IconButton(
                        onClick = { isFullScreen = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Full Screen",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    // Full Screen Modal Dialog
    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0B111E)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Demonstration Form",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        IconButton(onClick = { isFullScreen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawExerciseFrame(
                                type = animationType,
                                phase = currentPhase,
                                isStatic = isStaticTimed
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5722))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getPhaseLabel(type: ExerciseAnimationType, phase: Float, isStatic: Boolean): String {
    if (isStatic) return "Correct Hold Position (Breathe Steadily)"
    // Cycle goes 0 -> 0.5 (descent/eccentric), 0.5 -> 0.6 (inflection/squeeze), 0.6 -> 1.0 (ascent/concentric)
    return when {
        phase < 0.45f -> "1. Lower with Control ↓"
        phase < 0.55f -> "2. Peak Contraction •"
        else -> "3. Press / Rise Up ↑"
    }
}

private fun DrawScope.drawExerciseFrame(
    type: ExerciseAnimationType,
    phase: Float,
    isStatic: Boolean
) {
    val w = size.width
    val h = size.height
    val centerX = w / 2f
    val groundY = h * 0.78f

    // Soft exercise mat / floor baseline
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(w * 0.08f, groundY),
        end = Offset(w * 0.92f, groundY),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Smooth sinusoidal motion curve: 0 at start, 1 at bottom, back to 0 at top
    val motion = if (isStatic) 0f else ((1f - cos(phase * 2f * PI.toFloat())) / 2f)

    val bodyColor = Color(0xFF38BDF8)       // Light electric cyan for torso & limbs
    val jointColor = Color(0xFFF97316)      // Warm orange for pivot joints
    val headColor = Color(0xFF38BDF8)
    val accentColor = Color(0xFF4ADE80)     // Green for alignment check
    val limbStroke = 7.dp.toPx()
    val headRadius = 12.dp.toPx()
    val jointRadius = 4.5.dp.toPx()

    when (type) {
        ExerciseAnimationType.PUSH_UP,
        ExerciseAnimationType.WIDE_PUSH_UP,
        ExerciseAnimationType.DIAMOND_PUSH_UP -> {
            // Horizontal push-up
            val feetX = centerX - w * 0.32f
            val feetY = groundY - limbStroke / 2f
            val handX = centerX + w * 0.22f
            val handY = groundY - limbStroke / 2f

            // Upper body descends with motion
            val dropY = motion * 40.dp.toPx()
            val shoulderX = centerX + w * 0.18f
            val shoulderY = groundY - 55.dp.toPx() + dropY
            val hipX = centerX - w * 0.08f
            val hipY = groundY - 45.dp.toPx() + (dropY * 0.75f)

            val headX = shoulderX + 16.dp.toPx()
            val headY = shoulderY - 8.dp.toPx()

            // Elbow bends backward
            val elbowX = (shoulderX + handX) / 2f - (motion * 16.dp.toPx())
            val elbowY = (shoulderY + handY) / 2f - (15.dp.toPx() * (1f - motion))

            // Body line: feet to hips to shoulders
            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)

            // Arm: shoulder to elbow to hand
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(elbowX, elbowY), Offset(handX, handY), limbStroke, StrokeCap.Round)

            // Head
            drawCircle(headColor, headRadius, Offset(headX, headY))
            // Joints
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(elbowX, elbowY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.INCLINE_PUSH_UP -> {
            // Hands on elevated bench/box
            val benchX = centerX + w * 0.25f
            val benchTopY = groundY - 45.dp.toPx()
            // Draw bench
            drawLine(Color(0xFF334155), Offset(benchX - 20.dp.toPx(), benchTopY), Offset(benchX + 30.dp.toPx(), benchTopY), 6.dp.toPx(), StrokeCap.Round)
            drawLine(Color(0xFF334155), Offset(benchX + 10.dp.toPx(), benchTopY), Offset(benchX + 10.dp.toPx(), groundY), 6.dp.toPx(), StrokeCap.Round)

            val feetX = centerX - w * 0.32f
            val feetY = groundY - limbStroke / 2f
            val handX = benchX
            val handY = benchTopY

            val drop = motion * 28.dp.toPx()
            val shoulderX = handX - 10.dp.toPx() - (drop * 0.5f)
            val shoulderY = handY - 45.dp.toPx() + drop
            val hipX = centerX - w * 0.08f
            val hipY = groundY - 35.dp.toPx() + (drop * 0.4f)
            val headX = shoulderX + 14.dp.toPx()
            val headY = shoulderY - 8.dp.toPx()

            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handX, handY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.KNEE_PUSH_UP -> {
            val kneeX = centerX - w * 0.22f
            val kneeY = groundY - limbStroke / 2f
            val handX = centerX + w * 0.20f
            val handY = groundY - limbStroke / 2f

            // Feet up in air
            val footX = kneeX - 25.dp.toPx()
            val footY = kneeY - 30.dp.toPx()
            drawLine(bodyColor, Offset(kneeX, kneeY), Offset(footX, footY), limbStroke, StrokeCap.Round)

            val dropY = motion * 35.dp.toPx()
            val shoulderX = centerX + w * 0.16f
            val shoulderY = groundY - 50.dp.toPx() + dropY
            val hipX = centerX - w * 0.04f
            val hipY = groundY - 40.dp.toPx() + (dropY * 0.6f)

            drawLine(bodyColor, Offset(kneeX, kneeY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handX, handY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(shoulderX + 14.dp.toPx(), shoulderY - 8.dp.toPx()))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(kneeX, kneeY))
        }

        ExerciseAnimationType.SQUAT,
        ExerciseAnimationType.SUMO_SQUAT -> {
            // Standing to squatting
            val footX = centerX
            val footY = groundY - limbStroke / 2f

            val squatDrop = motion * 55.dp.toPx()
            val kneeBend = motion * 30.dp.toPx()

            val hipX = centerX - (motion * 25.dp.toPx())
            val hipY = groundY - 85.dp.toPx() + squatDrop

            val kneeX = centerX + kneeBend
            val kneeY = groundY - 45.dp.toPx() + (squatDrop * 0.4f)

            val shoulderX = hipX + 6.dp.toPx()
            val shoulderY = hipY - 60.dp.toPx()
            val headX = shoulderX
            val headY = shoulderY - 18.dp.toPx()

            // Arms extended forward for balance
            val handX = shoulderX + 45.dp.toPx()
            val handY = shoulderY + 8.dp.toPx()

            // Legs: Foot to Knee to Hip
            drawLine(bodyColor, Offset(footX, footY), Offset(kneeX, kneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(kneeX, kneeY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            // Torso: Hip to Shoulder
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            // Arms
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handX, handY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(kneeX, kneeY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
        }

        ExerciseAnimationType.LUNGE,
        ExerciseAnimationType.WALKING_LUNGE,
        ExerciseAnimationType.BULGARIAN_SPLIT_SQUAT -> {
            // Lunge: front leg bent 90 deg, back knee drops
            val drop = motion * 40.dp.toPx()

            val frontFootX = centerX + 30.dp.toPx()
            val frontFootY = groundY - limbStroke / 2f
            val frontKneeX = frontFootX
            val frontKneeY = groundY - 45.dp.toPx() + (drop * 0.5f)

            val backFootX = centerX - 55.dp.toPx()
            val backFootY = groundY - limbStroke / 2f
            val backKneeX = centerX - 25.dp.toPx()
            val backKneeY = groundY - 20.dp.toPx() - (drop * 0.2f)

            val hipX = centerX - 10.dp.toPx()
            val hipY = groundY - 80.dp.toPx() + drop

            val shoulderX = hipX
            val shoulderY = hipY - 55.dp.toPx()
            val headX = shoulderX
            val headY = shoulderY - 18.dp.toPx()

            // Legs
            drawLine(bodyColor, Offset(frontFootX, frontFootY), Offset(frontKneeX, frontKneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(frontKneeX, frontKneeY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(backFootX, backFootY), Offset(backKneeX, backKneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(backKneeX, backKneeY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)

            // Torso & head
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(frontKneeX, frontKneeY))
            drawCircle(jointColor, jointRadius, Offset(backKneeX, backKneeY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.GLUTE_BRIDGE,
        ExerciseAnimationType.SINGLE_LEG_GLUTE_BRIDGE -> {
            // Lying flat on ground, hips thrust up
            val shoulderX = centerX - w * 0.22f
            val shoulderY = groundY - limbStroke / 2f
            val headX = shoulderX - 18.dp.toPx()
            val headY = groundY - 8.dp.toPx()

            val feetX = centerX + w * 0.22f
            val feetY = groundY - limbStroke / 2f
            val kneeX = centerX + w * 0.15f

            // Hip lifts up
            val hipLift = motion * 45.dp.toPx()
            val hipX = centerX - w * 0.02f
            val hipY = groundY - 10.dp.toPx() - hipLift
            val kneeY = groundY - 45.dp.toPx() - (hipLift * 0.3f)

            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(kneeX, kneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(kneeX, kneeY), Offset(feetX, feetY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
            drawCircle(jointColor, jointRadius, Offset(kneeX, kneeY))
        }

        ExerciseAnimationType.PLANK -> {
            // Static plank with subtle breathing glow & alignment line
            val feetX = centerX - w * 0.32f
            val feetY = groundY - limbStroke / 2f
            val elbowX = centerX + w * 0.22f
            val elbowY = groundY - limbStroke / 2f

            val shoulderX = elbowX
            val shoulderY = groundY - 40.dp.toPx()
            val hipX = centerX - w * 0.05f
            val hipY = groundY - 38.dp.toPx()
            val headX = shoulderX + 16.dp.toPx()
            val headY = shoulderY - 4.dp.toPx()

            // Alignment guide ray (green dash line demonstrating straight posture)
            drawLine(
                color = accentColor.copy(alpha = 0.5f),
                start = Offset(feetX - 15.dp.toPx(), feetY - 8.dp.toPx()),
                end = Offset(headX + 25.dp.toPx(), headY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )

            // Body
            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.SIDE_PLANK -> {
            val feetX = centerX - w * 0.28f
            val feetY = groundY - limbStroke / 2f
            val elbowX = centerX + w * 0.18f
            val elbowY = groundY - limbStroke / 2f

            val shoulderX = elbowX
            val shoulderY = groundY - 46.dp.toPx()
            val hipX = centerX - 10.dp.toPx()
            val hipY = groundY - 40.dp.toPx()
            val headX = shoulderX + 15.dp.toPx()
            val headY = shoulderY - 8.dp.toPx()

            // Upper arm reaching to sky
            val topHandX = shoulderX
            val topHandY = shoulderY - 45.dp.toPx()
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(topHandX, topHandY), limbStroke, StrokeCap.Round)

            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.SUPERMAN -> {
            val hipX = centerX
            val hipY = groundY - 12.dp.toPx()

            val lift = motion * 28.dp.toPx()
            val feetX = centerX - w * 0.32f
            val feetY = groundY - 12.dp.toPx() - lift
            val chestX = centerX + w * 0.15f
            val chestY = groundY - 12.dp.toPx() - (lift * 0.7f)
            val handX = chestX + 45.dp.toPx()
            val handY = chestY - lift
            val headX = chestX + 15.dp.toPx()
            val headY = chestY - 10.dp.toPx()

            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(chestX, chestY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(chestX, chestY), Offset(handX, handY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.BIRD_DOG -> {
            // Tabletop position on all 4s
            val kneeX = centerX - 25.dp.toPx()
            val kneeY = groundY - limbStroke / 2f
            val handOnFloorX = centerX + 30.dp.toPx()
            val handOnFloorY = groundY - limbStroke / 2f

            val hipX = kneeX
            val hipY = groundY - 50.dp.toPx()
            val shoulderX = handOnFloorX
            val shoulderY = groundY - 50.dp.toPx()
            val headX = shoulderX + 16.dp.toPx()
            val headY = shoulderY - 4.dp.toPx()

            // Moving opposite arm & leg
            val extend = motion * 50.dp.toPx()
            val extFootX = hipX - 20.dp.toPx() - extend
            val extFootY = hipY - (motion * 10.dp.toPx())
            val extHandX = shoulderX + 20.dp.toPx() + extend
            val extHandY = shoulderY - (motion * 10.dp.toPx())

            // Torso & fixed limbs
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(kneeX, kneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handOnFloorX, handOnFloorY), limbStroke, StrokeCap.Round)

            // Extended limbs
            drawLine(bodyColor, Offset(hipX, hipY), Offset(extFootX, extFootY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(extHandX, extHandY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
        }

        ExerciseAnimationType.DEAD_BUG -> {
            val backX = centerX
            val backY = groundY - 12.dp.toPx()
            val headX = backX - 45.dp.toPx()
            val headY = backY - 6.dp.toPx()

            val ext = motion * 40.dp.toPx()
            val reachHandX = headX - ext
            val reachHandY = backY - 10.dp.toPx()

            val reachLegX = backX + 35.dp.toPx() + ext
            val reachLegY = backY - 10.dp.toPx()

            drawLine(bodyColor, Offset(headX, headY), Offset(backX + 25.dp.toPx(), backY), limbStroke, StrokeCap.Round)
            // Limbs in air
            drawLine(bodyColor, Offset(backX, backY), Offset(reachHandX, reachHandY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(backX + 20.dp.toPx(), backY), Offset(reachLegX, reachLegY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(headX, headY))
        }

        ExerciseAnimationType.PIKE_PUSH_UP -> {
            // Inverted V
            val drop = motion * 25.dp.toPx()
            val feetX = centerX - w * 0.22f
            val feetY = groundY - limbStroke / 2f
            val handX = centerX + w * 0.18f
            val handY = groundY - limbStroke / 2f

            val hipX = centerX
            val hipY = groundY - 95.dp.toPx() + (drop * 0.3f)
            val headX = handX - 10.dp.toPx()
            val headY = groundY - 25.dp.toPx() + drop

            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(handX, handY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }

        ExerciseAnimationType.ROW -> {
            // Bent-over row
            val footX = centerX - 10.dp.toPx()
            val footY = groundY - limbStroke / 2f
            val kneeX = centerX
            val kneeY = groundY - 45.dp.toPx()
            val hipX = centerX - 25.dp.toPx()
            val hipY = groundY - 80.dp.toPx()
            val shoulderX = centerX + 15.dp.toPx()
            val shoulderY = groundY - 85.dp.toPx()
            val headX = shoulderX + 16.dp.toPx()
            val headY = shoulderY - 8.dp.toPx()

            // Arm pulling
            val pull = motion * 35.dp.toPx()
            val handX = shoulderX
            val handY = groundY - 35.dp.toPx() - pull
            val elbowX = shoulderX - 12.dp.toPx() - (pull * 0.5f)
            val elbowY = shoulderY + 15.dp.toPx() - pull

            drawLine(bodyColor, Offset(footX, footY), Offset(kneeX, kneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(kneeX, kneeY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(elbowX, elbowY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(elbowX, elbowY), Offset(handX, handY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(elbowX, elbowY))
        }

        ExerciseAnimationType.SHOULDER_PRESS -> {
            // Overhead press
            val footX = centerX
            val footY = groundY - limbStroke / 2f
            val hipX = centerX
            val hipY = groundY - 80.dp.toPx()
            val shoulderX = centerX
            val shoulderY = groundY - 130.dp.toPx()
            val headX = centerX
            val headY = shoulderY - 20.dp.toPx()

            val press = motion * 40.dp.toPx()
            val handLeftX = shoulderX - 25.dp.toPx()
            val handRightX = shoulderX + 25.dp.toPx()
            val handY = shoulderY - 10.dp.toPx() - press

            drawLine(bodyColor, Offset(footX, footY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handLeftX, handY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handRightX, handY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(headX, headY))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
        }

        ExerciseAnimationType.CALF_RAISE -> {
            val footX = centerX
            val rise = motion * 25.dp.toPx()
            val footY = groundY - limbStroke / 2f - rise
            val hipX = centerX
            val hipY = groundY - 80.dp.toPx() - rise
            val shoulderX = centerX
            val shoulderY = groundY - 135.dp.toPx() - rise

            drawLine(bodyColor, Offset(footX, footY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(centerX, shoulderY - 18.dp.toPx()))
        }

        ExerciseAnimationType.MOUNTAIN_CLIMBERS,
        ExerciseAnimationType.JUMPING_JACKS,
        ExerciseAnimationType.HIGH_KNEES,
        ExerciseAnimationType.BURPEES -> {
            // General rhythmic movement / plank climber
            val feetX = centerX - w * 0.32f
            val feetY = groundY - limbStroke / 2f
            val handX = centerX + w * 0.20f
            val handY = groundY - limbStroke / 2f

            val kneeCycle = motion * 35.dp.toPx()
            val runningKneeX = centerX - 10.dp.toPx() + kneeCycle
            val runningKneeY = groundY - 35.dp.toPx()

            val shoulderX = handX - 10.dp.toPx()
            val shoulderY = groundY - 50.dp.toPx()
            val hipX = centerX - w * 0.05f
            val hipY = groundY - 45.dp.toPx()

            drawLine(bodyColor, Offset(feetX, feetY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(runningKneeX, runningKneeY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(shoulderX, shoulderY), Offset(handX, handY), limbStroke, StrokeCap.Round)

            drawCircle(headColor, headRadius, Offset(shoulderX + 16.dp.toPx(), shoulderY - 8.dp.toPx()))
            drawCircle(jointColor, jointRadius, Offset(shoulderX, shoulderY))
            drawCircle(jointColor, jointRadius, Offset(hipX, hipY))
        }
        else -> {
            // Fallback for warmups, stretches, etc.
            val footX = centerX
            val footY = groundY - limbStroke / 2f
            val hipX = centerX
            val hipY = groundY - 80.dp.toPx()
            val shoulderX = centerX
            val shoulderY = groundY - 130.dp.toPx()
            val headX = centerX
            val headY = shoulderY - 20.dp.toPx()

            drawLine(bodyColor, Offset(footX, footY), Offset(hipX, hipY), limbStroke, StrokeCap.Round)
            drawLine(bodyColor, Offset(hipX, hipY), Offset(shoulderX, shoulderY), limbStroke, StrokeCap.Round)
            drawCircle(headColor, headRadius, Offset(headX, headY))
        }
    }
}
