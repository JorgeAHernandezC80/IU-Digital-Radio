package com.tudominio.iudigitalradio.ui.equalizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random

/**
 * Pantalla principal del Equalizer / Visualizer estilo DJ Mixer
 * Se muestra mientras se reproduce la música.
 */
@Composable
fun EqualizerVisualizerScreen(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados de los sliders del equalizer (5 bandas)
    val eqBands = remember { mutableStateListOf(0.6f, 0.75f, 0.5f, 0.8f, 0.65f) }

    // Estados de los knobs rotativos
    var leftKnob by remember { mutableFloatStateOf(0.4f) }
    var rightKnob by remember { mutableFloatStateOf(0.7f) }

    // Preset actual
    var currentPreset by remember { mutableStateOf("NORMAL") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF0D0D0D),
                        Color(0xFF050505)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ========== VISUALIZADOR DE ESPECTRO (arriba) ==========
            SpectrumVisualizer(
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            )

            // ========== EQUALIZER BARS + BOTONES DE COLOR ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botones de color izquierdos
                ColorPadColumn(colors = listOf(
                    Color(0xFFE53935), Color(0xFFFF7043),
                    Color(0xFFFFCA28), Color(0xFF66BB6A),
                    Color(0xFF26A69A)
                ))

                // 5 Bandas del equalizer
                eqBands.forEachIndexed { index, value ->
                    EqualizerBand(
                        value = value,
                        onValueChange = { eqBands[index] = it },
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight()
                    )
                }

                // Botones de color derechos
                ColorPadColumn(colors = listOf(
                    Color(0xFFE53935), Color(0xFFFFCA28),
                    Color(0xFF66BB6A), Color(0xFF26C6DA),
                    Color(0xFF42A5F5)
                ))
            }

            // ========== CONTROLES DE TRANSPORTE ==========
            TransportControls(
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                onStop = onStop,
                onNext = onNext,
                onPrevious = onPrevious,
                modifier = Modifier.fillMaxWidth()
            )

            // ========== KNOBS + PRESETS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Knob izquierdo (grande con arco de color)
                RotaryKnob(
                    value = leftKnob,
                    onValueChange = { leftKnob = it },
                    size = 90.dp,
                    label = "BASS",
                    showColorArc = true
                )

                // 4 knobs pequeños
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallKnob(value = 0.5f, color = Color(0xFF42A5F5))
                        SmallKnob(value = 0.7f, color = Color(0xFFAB47BC))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallKnob(value = 0.3f, color = Color(0xFFFFCA28))
                        SmallKnob(value = 0.6f, color = Color(0xFFEC407A))
                    }
                }

                // Presets
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    listOf("NORMAL", "HIP-POP", "JAZZ").forEach { preset ->
                        PresetButton(
                            text = preset,
                            selected = currentPreset == preset,
                            onClick = {
                                currentPreset = preset
                                // Aplicar valores de preset
                                when (preset) {
                                    "NORMAL" -> eqBands.apply {
                                        clear()
                                        addAll(listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f))
                                    }
                                    "HIP-POP" -> eqBands.apply {
                                        clear()
                                        addAll(listOf(0.85f, 0.7f, 0.4f, 0.55f, 0.75f))
                                    }
                                    "JAZZ" -> eqBands.apply {
                                        clear()
                                        addAll(listOf(0.4f, 0.6f, 0.75f, 0.65f, 0.5f))
                                    }
                                }
                            }
                        )
                    }
                }

                // Knob derecho
                RotaryKnob(
                    value = rightKnob,
                    onValueChange = { rightKnob = it },
                    size = 90.dp,
                    label = "TREBLE",
                    showColorArc = true
                )
            }
        }
    }
}

// ============================================================
//  VISUALIZADOR DE ESPECTRO (ondas de colores)
// ============================================================
@Composable
fun SpectrumVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")

    // Animación de fase para el movimiento
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Intensidad (más alta cuando está reproduciendo)
    val intensity by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.15f,
        animationSpec = tween(400),
        label = "intensity"
    )

    Canvas(modifier = modifier.background(Color(0xFF0A0A12))) {
        val barCount = 64
        val barWidth = size.width / barCount
        val maxHeight = size.height * 0.92f

        for (i in 0 until barCount) {
            val normalized = i / barCount.toFloat()

            // Forma de onda tipo spectrum realista
            val wave1 = sin(normalized * 8f + phase) * 0.35f
            val wave2 = sin(normalized * 15f - phase * 1.3f) * 0.25f
            val wave3 = cos(normalized * 4f + phase * 0.7f) * 0.2f
            val noise = (Random(i + (phase * 10).toInt()).nextFloat() - 0.5f) * 0.15f

            val heightFactor = (0.35f + wave1 + wave2 + wave3 + noise)
                .coerceIn(0.08f, 1f) * intensity

            val barHeight = maxHeight * heightFactor
            val x = i * barWidth
            val y = size.height - barHeight

            // Gradiente arcoíris vertical
            val color = when {
                normalized < 0.2f -> lerp(Color(0xFFFFEB3B), Color(0xFFFF9800), normalized / 0.2f)
                normalized < 0.4f -> lerp(Color(0xFFFF9800), Color(0xFF4CAF50), (normalized - 0.2f) / 0.2f)
                normalized < 0.6f -> lerp(Color(0xFF4CAF50), Color(0xFF00BCD4), (normalized - 0.4f) / 0.2f)
                normalized < 0.8f -> lerp(Color(0xFF00BCD4), Color(0xFF2196F3), (normalized - 0.6f) / 0.2f)
                else -> lerp(Color(0xFF2196F3), Color(0xFF9C27B0), (normalized - 0.8f) / 0.2f)
            }

            // Barra principal
            drawRoundRect(
                color = color.copy(alpha = 0.95f),
                topLeft = Offset(x + 1f, y),
                size = Size(barWidth - 2f, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Brillo superior
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(x + 1f, y),
                size = Size(barWidth - 2f, barHeight * 0.18f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

// ============================================================
//  BANDA DEL EQUALIZER (slider vertical)
// ============================================================
@Composable
fun EqualizerBand(
    value: Float,
    onValueChange: (Float) -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "eqBand"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newValue = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = size.width * 0.35f
            val trackX = (size.width - trackWidth) / 2

            // Track de fondo
            drawRoundRect(
                color = Color(0xFF2A2A2A),
                topLeft = Offset(trackX, 0f),
                size = Size(trackWidth, size.height),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Track activo (cyan)
            val fillHeight = size.height * animatedValue
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                ),
                topLeft = Offset(trackX, size.height - fillHeight),
                size = Size(trackWidth, fillHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Thumb (control deslizante)
            val thumbY = size.height * (1f - animatedValue)
            val thumbHeight = 14.dp.toPx()
            val thumbWidth = size.width * 0.85f

            drawRoundRect(
                color = Color(0xFFE0E0E0),
                topLeft = Offset((size.width - thumbWidth) / 2, thumbY - thumbHeight / 2),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(3f, 3f)
            )

            // Líneas del thumb
            drawLine(
                color = Color(0xFF424242),
                start = Offset((size.width - thumbWidth) / 2 + 4f, thumbY),
                end = Offset((size.width + thumbWidth) / 2 - 4f, thumbY),
                strokeWidth = 1.5f
            )
        }
    }
}

// ============================================================
//  CONTROLES DE TRANSPORTE
// ============================================================
@Composable
fun TransportControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1C1C1C), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransportButton(icon = Icons.Default.SkipPrevious, onClick = onPrevious)
        TransportButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            onClick = onPlayPause,
            isPrimary = true
        )
        TransportButton(icon = Icons.Default.Stop, onClick = onStop)
        TransportButton(icon = Icons.Default.SkipNext, onClick = onNext)
    }
}

@Composable
fun TransportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(if (isPrimary) 52.dp else 42.dp)
            .background(
                if (isPrimary) Color(0xFF00BCD4) else Color(0xFF2A2A2A),
                CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPrimary) Color.Black else Color.White,
            modifier = Modifier.size(if (isPrimary) 28.dp else 22.dp)
        )
    }
}

// ============================================================
//  KNOB ROTATIVO
// ============================================================
@Composable
fun RotaryKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    size: androidx.compose.ui.unit.Dp,
    label: String,
    showColorArc: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        // Drag vertical para cambiar valor
                        val delta = -change.position.y / size.toPx()
                        onValueChange((value + delta * 0.4f).coerceIn(0f, 1f))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = this.size.minDimension / 2
                val center = Offset(this.size.width / 2, this.size.height / 2)

                // Arco de color (si se solicita)
                if (showColorArc) {
                    val sweep = 270f
                    val startAngle = 135f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFFF1744), Color(0xFFFFEA00),
                                Color(0xFF00E676), Color(0xFF00B0FF),
                                Color(0xFFD500F9)
                            )
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx()),
                        size = Size(radius * 1.7f, radius * 1.7f),
                        topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.85f)
                    )
                }

                // Cuerpo del knob
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4A4A4A), Color(0xFF1A1A1A)),
                        center = center
                    ),
                    radius = radius * 0.72f,
                    center = center
                )

                // Anillo metálico
                drawCircle(
                    color = Color(0xFF757575),
                    radius = radius * 0.72f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )

                // Indicador de posición
                val angle = Math.toRadians((135.0 + value * 270.0))
                val indicatorLength = radius * 0.5f
                val endX = center.x + cos(angle).toFloat() * indicatorLength
                val endY = center.y + sin(angle).toFloat() * indicatorLength

                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Centro
                drawCircle(
                    color = Color(0xFF212121),
                    radius = radius * 0.18f,
                    center = center
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFFAAAAAA),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SmallKnob(value: Float, color: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                Brush.radialGradient(listOf(Color(0xFF555555), Color(0xFF1A1A1A))),
                CircleShape
            )
            .border(2.dp, color.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun ColorPadColumn(colors: List<Color>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(28.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
                    .clickable { /* feedback visual opcional */ }
            )
        }
    }
}

@Composable
fun PresetButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(0xFF00BCD4) else Color(0xFF2A2A2A))
            .border(1.dp, if (selected) Color(0xFF00E5FF) else Color(0xFF444444), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper para interpolar colores
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
