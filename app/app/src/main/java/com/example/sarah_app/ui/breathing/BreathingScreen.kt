package com.example.sarah_app.ui.breathing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sarah_app.ui.theme.AccentOrange
import com.example.sarah_app.ui.theme.BackgroundCream
import com.example.sarah_app.ui.theme.BrownText
import com.example.sarah_app.ui.theme.CardBrown
import com.example.sarah_app.ui.theme.PrimaryOrange
import com.example.sarah_app.ui.theme.SurfaceLight
import kotlinx.coroutines.delay

enum class BreathPhase(
    val label: String,
    val durationSec: Int,
    val color: Color,
    val hint: String,
) {
    INHALE("Inhale", 4, PrimaryOrange, "Breathe in slowly through your nose"),
    HOLD_IN("Hold your breath", 4, CardBrown, "Keep the air in, stay still"),
    EXHALE("Exhale", 4, AccentOrange, "Release slowly through your mouth"),
    HOLD_OUT("Hold your breath", 4, CardBrown, "Stay empty, stay calm");

    fun next(): BreathPhase = entries[(ordinal + 1) % entries.size]
}

@Composable
fun BreathingScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsLeft by remember { mutableIntStateOf(4) }
    val ringProgress = remember { Animatable(0f) }

    // Ring animation loop + phase sequencing
    LaunchedEffect(isRunning) {
        if (!isRunning) {
            ringProgress.snapTo(0f)
            phase = BreathPhase.INHALE
            secondsLeft = 4
        } else {
            while (true) {
                ringProgress.snapTo(0f)
                ringProgress.animateTo(1f, tween(4000, easing = LinearEasing))
                phase = phase.next()
            }
        }
    }

    // Per-phase countdown
    LaunchedEffect(isRunning, phase) {
        if (!isRunning) return@LaunchedEffect
        secondsLeft = phase.durationSec
        repeat(phase.durationSec) {
            delay(1000L)
            secondsLeft--
        }
    }

    val ringColor by animateColorAsState(
        targetValue = phase.color,
        animationSpec = tween(600),
        label = "ringColor",
    )

    // Scale derived directly from ring progress — perfectly in sync, no separate animator needed
    val pulseScale = when (phase) {
        BreathPhase.INHALE -> 0.85f + 0.30f * ringProgress.value
        BreathPhase.EXHALE -> 1.15f - 0.30f * ringProgress.value
        else -> 1.0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Title / phase label
        Text(
            text = if (isRunning) phase.label else "Box Breathing",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = BrownText,
            textAlign = TextAlign.Center,
        )

        // Progress ring + pulsing circle
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    // Background ring
                    drawArc(
                        color = ringColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke,
                    )
                    // Progress arc
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * ringProgress.value,
                        useCenter = false,
                        style = stroke,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (isRunning) pulseScale else 1f)
                    .drawBehind { drawCircle(color = ringColor.copy(alpha = 0.13f)) },
                contentAlignment = Alignment.Center,
            ) {
                if (isRunning) {
                    Text(
                        text = secondsLeft.coerceAtLeast(0).toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = ringColor,
                    )
                } else {
                    Text("🐼", fontSize = 72.sp)
                }
            }
        }

        // Hint text
        Text(
            text = if (isRunning) phase.hint
            else "Feeling stressed?\nLet's breathe together.",
            fontSize = 16.sp,
            color = BrownText.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            lineHeight = 22.sp,
        )

        // START / STOP button
        Button(
            onClick = { isRunning = !isRunning },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) SurfaceLight else PrimaryOrange,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(56.dp),
        ) {
            Text(
                text = if (isRunning) "STOP" else "START",
                color = if (isRunning) BrownText else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
            )
        }
    }
}
