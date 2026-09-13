package com.tudominio.iudigitalradio.ui.equalizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

/**
 * Visualizador de espectro de audio de nivel profesional.
 * Estilo "RADIO IU DIGITAL" – neón, glow, reflejo y gradiente
 * Amarillo → Naranja → Verde → Cian → Azul → ROJO (sin fucsia).
 */
@Composable
fun SpectrumVisualizerPro(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrumPro")

    // Fase principal del movimiento
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Segunda fase (más rápida) para dar vida
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    // Intensidad según estado de reproducción
    val intensity by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.12f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "intensity"
    )

    // Pulso sutil del glow
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF050510))
    ) {
        val barCount = 72
        val gap = 2.5f
        val totalGap = gap * (barCount - 1)
        val barWidth = (size.width - totalGap) / barCount
        val maxBarHeight = size.height * 0.78f
        val reflectionHeight = size.height * 0.18f
        val baseY = size.height * 0.80f

        // Fondo con sutil degradado vertical
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A1A),
                    Color(0xFF050510),
                    Color(0xFF030308)
                )
            )
        )

        // Línea de base sutil (como en equipos profesionales)
        drawLine(
            color = Color(0xFF1A2A4A).copy(alpha = 0.6f),
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 1.5f
        )

        for (i in 0 until barCount) {
            val t = i / (barCount - 1).toFloat()

            // --- Forma de onda realista (múltiples armónicos) ---
            val waveA = sin(t * 7.5f + phase) * 0.32f
            val waveB = sin(t * 14f - phase2 * 1.1f) * 0.22f
            val waveC = cos(t * 4.2f + phase * 0.6f) * 0.18f
            val waveD = sin(t * 22f + phase2 * 0.8f) * 0.10f

            // Ruido controlado para que no se vea artificial
            val seed = (i * 17 + (phase * 8).toInt())
            val noise = (Random(seed).nextFloat() - 0.5f) * 0.12f

            // Envelope tipo "spectrum analyzer" (más alto en medios-agudos)
            val envelope = 0.55f + 0.35f * sin(t * PI.toFloat()).coerceAtLeast(0f)

            var heightFactor = (0.28f + waveA + waveB + waveC + waveD + noise) * envelope
            heightFactor = heightFactor.coerceIn(0.06f, 1f) * intensity

            val barHeight = maxBarHeight * heightFactor
            val x = i * (barWidth + gap)
            val topY = baseY - barHeight

            // ========== COLOR GRADIENT (sin fucsia → ROJO) ==========
            val barColor = spectrumColor(t)

            // --- Glow exterior (neón) ---
            if (intensity > 0.3f) {
                drawRoundRect(
                    color = barColor.copy(alpha = 0.18f * glowPulse * intensity),
                    topLeft = Offset(x - 3f, topY - 4f),
                    size = Size(barWidth + 6f, barHeight + 8f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }

            // --- Barra principal con gradiente vertical interno ---
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        barColor.copy(alpha = 1f),
                        barColor.copy(alpha = 0.85f),
                        barColor.copy(alpha = 0.55f)
                    ),
                    startY = topY,
                    endY = baseY
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )

            // --- Highlight superior (brillo profesional) ---
            val highlightHeight = (barHeight * 0.22f).coerceAtMost(14f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.0f)
                    )
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, highlightHeight),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )

            // --- Reflejo inferior (espejo) ---
            val reflectionAlpha = 0.22f * intensity
            if (reflectionAlpha > 0.02f) {
                val reflHeight = (barHeight * 0.55f).coerceAtMost(reflectionHeight)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            barColor.copy(alpha = reflectionAlpha),
                            barColor.copy(alpha = 0.0f)
                        )
                    ),
                    topLeft = Offset(x, baseY + 2f),
                    size = Size(barWidth, reflHeight),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }

        // Viñeta sutil en los bordes (más profesional)
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to Color.Black.copy(alpha = 0.35f),
                    0.08f to Color.Transparent,
                    0.92f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.35f)
                )
            )
        )
    }
}

/**
 * Gradiente de color profesional:
 * Amarillo → Naranja → Verde → Cian → Azul → ROJO
 * (reemplaza el fucsia/magenta original por rojo intenso)
 */
private fun spectrumColor(t: Float): Color {
    // t de 0.0 a 1.0
    return when {
        t < 0.18f -> lerpColor(
            Color(0xFFFFD600), // Amarillo dorado
            Color(0xFFFF9100), // Naranja
            t / 0.18f
        )
        t < 0.36f -> lerpColor(
            Color(0xFFFF9100), // Naranja
            Color(0xFF00E676), // Verde neón
            (t - 0.18f) / 0.18f
        )
        t < 0.52f -> lerpColor(
            Color(0xFF00E676), // Verde
            Color(0xFF00E5FF), // Cian
            (t - 0.36f) / 0.16f
        )
        t < 0.70f -> lerpColor(
            Color(0xFF00E5FF), // Cian
            Color(0xFF2979FF), // Azul
            (t - 0.52f) / 0.18f
        )
        t < 0.85f -> lerpColor(
            Color(0xFF2979FF), // Azul
            Color(0xFFD50000), // Rojo intenso (antes fucsia)
            (t - 0.70f) / 0.15f
        )
        else -> lerpColor(
            Color(0xFFD50000), // Rojo
            Color(0xFFFF1744), // Rojo brillante final
            (t - 0.85f) / 0.15f
        )
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = 1f
    )
}
