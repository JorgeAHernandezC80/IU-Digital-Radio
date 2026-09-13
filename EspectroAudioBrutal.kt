package com.example.iudigitalradio

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iudigitalradio.audio.AudioSpectrumAnalyzer
import com.example.iudigitalradio.ui.theme.RadioCyan
import com.example.iudigitalradio.ui.theme.RadioTextMuted
import kotlin.math.*

/**
 * Spectrum BRUTAL con datos REALES de FFT.
 * Gradiente: Naranja → Amarillo → Verde → Cian → Azul → ROJO (sin fucsia/magenta).
 *
 * Reemplaza tu composable EspectroAudio actual por este.
 */
@Composable
fun EspectroAudioBrutal(playing: Boolean, spectrum: FloatArray) {
    val bars = if (spectrum.size == AudioSpectrumAnalyzer.BAR_COUNT) {
        spectrum
    } else {
        FloatArray(AudioSpectrumAnalyzer.BAR_COUNT)
    }

    val transition = rememberInfiniteTransition(label = "glow")
    val glow by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "glowPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF06070A))
            .border(1.dp, RadioCyan.copy(alpha = .45f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val n = bars.size.coerceAtLeast(1)
            val gap = 2.4f
            val totalGap = gap * (n - 1)
            val barW = ((size.width - totalGap) / n).coerceAtLeast(1f)
            val maxH = size.height * 0.82f
            val baseY = size.height * 0.88f
            val reflH = size.height * 0.12f

            // Fondo sutil
            drawRect(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0A16), Color(0xFF06070A), Color(0xFF040408))
                )
            )

            // Línea base
            drawLine(
                Color(0xFF1A2A40).copy(alpha = 0.6f),
                Offset(0f, baseY),
                Offset(size.width, baseY),
                strokeWidth = 1.2f
            )

            for (i in 0 until n) {
                val t = i / (n - 1).toFloat().coerceAtLeast(0.001f)
                val nivel = if (playing) {
                    bars[i].coerceIn(0f, 1f)
                } else {
                    (bars[i] * 0.12f).coerceIn(0f, 1f)
                }

                val barH = maxH * nivel
                val x = i * (barW + gap)
                val top = baseY - barH
                val color = spectrumColorRainbow(t)

                // Glow
                if (playing && nivel > 0.15f) {
                    drawRoundRect(
                        color.copy(alpha = 0.15f * glow * nivel),
                        Offset(x - 2f, top - 3f),
                        Size(barW + 4f, barH + 6f),
                        CornerRadius(2.5f)
                    )
                }

                // Barra
                drawRoundRect(
                    Brush.verticalGradient(
                        listOf(color, color.copy(alpha = 0.85f), color.copy(alpha = 0.45f)),
                        startY = top,
                        endY = baseY
                    ),
                    Offset(x, top),
                    Size(barW, barH.coerceAtLeast(1f)),
                    CornerRadius(2.2f)
                )

                // Highlight
                val hl = (barH * 0.18f).coerceAtMost(10f)
                if (hl > 1f) {
                    drawRoundRect(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(0.4f), Color.White.copy(0f))
                        ),
                        Offset(x, top),
                        Size(barW, hl),
                        CornerRadius(2.2f)
                    )
                }

                // Reflejo
                if (playing && nivel > 0.08f) {
                    val rh = (barH * 0.4f).coerceAtMost(reflH)
                    drawRoundRect(
                        Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.18f * nivel), color.copy(alpha = 0f))
                        ),
                        Offset(x, baseY + 2f),
                        Size(barW, rh),
                        CornerRadius(2f)
                    )
                }
            }

            // Viñeta
            drawRect(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Black.copy(0.35f),
                        0.06f to Color.Transparent,
                        0.94f to Color.Transparent,
                        1.0f to Color.Black.copy(0.35f)
                    )
                )
            )
        }

        Text(
            "AUDIO SPECTRUM",
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            color = Color.White.copy(alpha = .4f),
            fontSize = 8.sp,
            letterSpacing = 1.5.sp
        )
        Text(
            if (playing) "LIVE FFT" else "STANDBY",
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            color = if (playing) RadioCyan else RadioTextMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}

/**
 * Naranja → Amarillo → Verde → Cian → Azul → ROJO
 * (reemplaza magenta/fucsia por rojo)
 */
private fun spectrumColorRainbow(t: Float): Color {
    val hue = (15f + 345f * t.coerceIn(0f, 1f)) % 360f
    return hsvColor(hue, 0.94f, 0.55f + 0.45f * (0.35f + 0.65f * sin(t * PI.toFloat()).toFloat().coerceIn(0f, 1f)))
}

private fun hsvColor(h: Float, s: Float, v: Float): Color {
    val c = v * s
    val x = c * (1 - abs((h / 60f) % 2 - 1))
    val m = v - c
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
}
