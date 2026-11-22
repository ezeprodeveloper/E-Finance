package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val LOGO_PHASE_DURATION_MS = 10_000L
private const val WORD_PHASE_DURATION_MS = 5_000L
private const val TOTAL_INTRO_DURATION_MS = LOGO_PHASE_DURATION_MS + WORD_PHASE_DURATION_MS

@Composable
fun IntroSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val startNanos = System.nanoTime()
        while (true) {
            val currentElapsed = (System.nanoTime() - startNanos) / 1_000_000L
            if (currentElapsed >= TOTAL_INTRO_DURATION_MS) {
                elapsedMs = TOTAL_INTRO_DURATION_MS
                break
            }
            elapsedMs = currentElapsed
            delay(16L)
        }
        onFinished()
    }

    val palette = remember {
        listOf(
            RoyalBlue600,
            Cyan400,
            Emerald500,
            Gold400,
            Purple700,
            Crimson500,
            RoyalBlue700,
            Emerald400,
            Cyan500
        )
    }

    // Smoothly cycle through many colors across the 15s timeline
    val colorCyclePos = (elapsedMs / 950f)
    val idxA = colorCyclePos.toInt() % palette.size
    val idxB = (idxA + 1) % palette.size
    val idxC = (idxA + 3) % palette.size
    val frac = colorCyclePos - colorCyclePos.toInt()

    val primaryDynamicColor = lerp(palette[idxA], palette[idxB], frac)
    val secondaryDynamicColor = lerp(palette[idxB], palette[idxC], frac)
    val tertiaryDynamicColor = lerp(palette[idxC], palette[(idxA + 5) % palette.size], frac)

    val isLogoPhase = elapsedMs < LOGO_PHASE_DURATION_MS
    val overallProgress = (elapsedMs.toFloat() / TOTAL_INTRO_DURATION_MS.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        DeepNavy950,
                        primaryDynamicColor.copy(alpha = 0.72f),
                        secondaryDynamicColor.copy(alpha = 0.58f),
                        DeepNavy900
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 1800f)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("intro_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Multi-color animated ambient glow rings behind center
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension * 0.42f
            val angleRad = (elapsedMs / 1000f) * 1.4f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryDynamicColor.copy(alpha = 0.45f),
                        secondaryDynamicColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 1.35f
                ),
                center = center,
                radius = maxRadius * 1.35f
            )

            drawCircle(
                color = secondaryDynamicColor.copy(alpha = 0.4f),
                radius = maxRadius * 0.92f,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(primaryDynamicColor, secondaryDynamicColor, tertiaryDynamicColor, primaryDynamicColor)
                ),
                startAngle = (elapsedMs * 0.14f) % 360f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(center.x - maxRadius * 0.76f, center.y - maxRadius * 0.76f),
                size = androidx.compose.ui.geometry.Size(maxRadius * 1.52f, maxRadius * 1.52f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Orbiting color accent nodes
            val orbitR = maxRadius * 0.92f
            for (i in 0 until 3) {
                val theta = angleRad + (i * (2.0 * PI / 3.0)).toFloat()
                val orbCenter = Offset(
                    center.x + orbitR * cos(theta),
                    center.y + orbitR * sin(theta)
                )
                drawCircle(
                    color = palette[(idxA + i * 2) % palette.size],
                    radius = 7.dp.toPx(),
                    center = orbCenter
                )
            }
        }

        AnimatedContent(
            targetState = isLogoPhase,
            transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(450)) },
            label = "introPhaseTransition"
        ) { showLogo ->
            if (showLogo) {
                val logoSecondsRemaining = ((LOGO_PHASE_DURATION_MS - elapsedMs) / 1000L + 1L).coerceIn(1L, 10L)
                val pulseScale = 1f + 0.06f * sin((elapsedMs / 1000f) * 3.2f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.testTag("intro_logo_phase")
                ) {
                    Box(
                        modifier = Modifier
                            .size(168.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(primaryDynamicColor, secondaryDynamicColor, tertiaryDynamicColor)
                                )
                            )
                            .border(3.dp, PureWhite.copy(alpha = 0.85f), CircleShape)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(DeepNavy950.copy(alpha = 0.88f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "E-Finance App Logo",
                                modifier = Modifier.size(124.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = DeepNavy950.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = "${logoSecondsRemaining}s",
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                            color = primaryDynamicColor.let { lerp(it, PureWhite, 0.45f) },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                val wordElapsed = (elapsedMs - LOGO_PHASE_DURATION_MS).coerceIn(0L, WORD_PHASE_DURATION_MS)
                val wordProgress = wordElapsed.toFloat() / WORD_PHASE_DURATION_MS.toFloat()
                val rotationDegrees = wordProgress * 720f
                val wordSecondsRemaining = ((WORD_PHASE_DURATION_MS - wordElapsed) / 1000L + 1L).coerceIn(1L, 5L)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.testTag("intro_rotating_word_phase")
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = rotationDegrees
                            }
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        DeepNavy950.copy(alpha = 0.82f),
                                        primaryDynamicColor.copy(alpha = 0.78f),
                                        DeepNavy900.copy(alpha = 0.88f)
                                    )
                                )
                            )
                            .border(
                                width = 2.5.dp,
                                brush = Brush.linearGradient(listOf(Gold400, Cyan400, Emerald400)),
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .padding(horizontal = 32.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "E-Finance",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            ),
                            color = PureWhite,
                            modifier = Modifier.testTag("intro_rotating_word")
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = DeepNavy950.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = "${wordSecondsRemaining}s",
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                            color = Gold400,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom subtle launch progress bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 36.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Gold400,
                trackColor = PureWhite.copy(alpha = 0.2f)
            )
        }
    }
}
