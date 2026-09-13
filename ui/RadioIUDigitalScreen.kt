package com.tudominio.iudigitalradio.ui.equalizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla principal estilo "RADIO IU DIGITAL"
 * Diseño profesional inspirado en el mockup del usuario.
 * Spectrum con gradiente Amarillo → Rojo (sin fucsia).
 */
@Composable
fun RadioIUDigitalScreen(
    isPlaying: Boolean,
    stationName: String = "RADIO IU DIGITAL",
    slogan: String = "TU MÚSICA, SIEMPRE",
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBand by remember { mutableStateOf("FM") }
    var volume by remember { mutableFloatStateOf(0.75f) }

    val bgColor = Color(0xFF0A0E17)
    val panelColor = Color(0xFF121826)
    val neonBlue = Color(0xFF00B4FF)
    val neonCyan = Color(0xFF00E5FF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1520),
                        Color(0xFF080C14),
                        Color(0xFF050810)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ========== HEADER ==========
            HeaderBar(
                title = "RADIO IU DIGITAL",
                subtitle = "TU MÚSICA, SIEMPRE",
                isLive = isPlaying
            )

            // ========== SPECTRUM VISUALIZER (el protagonista) ==========
            SpectrumVisualizerPro(
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = neonBlue.copy(0.3f))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(neonBlue.copy(0.6f), neonCyan.copy(0.3f), neonBlue.copy(0.6f))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            // ========== CONTROLES CENTRALES + LEVEL METERS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level meter izquierdo
                LevelMeter(isPlaying = isPlaying, modifier = Modifier.width(36.dp).fillMaxHeight())

                // Transport controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransportIconButton(
                        icon = Icons.Rounded.SkipPrevious,
                        onClick = onPrevious,
                        size = 46.dp
                    )
                    // Play/Pause grande con glow
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(10.dp, CircleShape, ambientColor = neonCyan.copy(0.5f))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                                ),
                                CircleShape
                            )
                            .clickable { onPlayPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    TransportIconButton(
                        icon = Icons.Rounded.SkipNext,
                        onClick = onNext,
                        size = 46.dp
                    )
                }

                // Level meter derecho
                LevelMeter(isPlaying = isPlaying, modifier = Modifier.width(36.dp).fillMaxHeight())
            }

            // ========== DISPLAY DE ESTACIÓN + KNOBS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Knob Volumen
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RotaryKnob(
                        value = volume,
                        onValueChange = { volume = it },
                        size = 72.dp,
                        label = "VOLUMEN",
                        showColorArc = true
                    )
                }

                // Display central de estación
                StationDisplay(
                    name = stationName,
                    slogan = slogan,
                    isLive = isPlaying,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                )

                // Knob Sintonizar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RotaryKnob(
                        value = 0.55f,
                        onValueChange = {},
                        size = 72.dp,
                        label = "SINTONIZAR",
                        showColorArc = true
                    )
                }
            }

            // ========== BOTONES DE BANDA (FM / AM / WEB / FAVORITOS) ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "FM" to Icons.Rounded.SettingsInputAntenna,
                    "AM" to Icons.Rounded.SettingsInputAntenna,
                    "WEB" to Icons.Rounded.Language,
                    "FAVORITOS" to Icons.Rounded.Star
                ).forEach { (label, icon) ->
                    BandButton(
                        label = label,
                        icon = icon,
                        selected = selectedBand == label,
                        onClick = { selectedBand = label }
                    )
                }
            }
        }
    }
}

// ============================================================
//  COMPONENTES
// ============================================================

@Composable
private fun HeaderBar(title: String, subtitle: String, isLive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0A1525), Color(0xFF0D1A30), Color(0xFF0A1525))
                ),
                RoundedCornerShape(10.dp)
            )
            .border(1.dp, Color(0xFF1A3A5C), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono de señal
            Icon(
                imageVector = Icons.Rounded.Sensors,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF8AABB8),
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Indicador EN VIVO
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF1A0A0A), RoundedCornerShape(20.dp))
                .border(1.dp, if (isLive) Color(0xFFFF1744) else Color(0xFF444444), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isLive) Color(0xFFFF1744) else Color(0xFF666666),
                        CircleShape
                    )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "EN VIVO",
                color = if (isLive) Color(0xFFFF1744) else Color(0xFF888888),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun LevelMeter(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "level")
    val anim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "levelAnim"
    )

    val segments = 8
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(segments) { i ->
            val threshold = 1f - (i / segments.toFloat())
            val active = isPlaying && anim > threshold * 0.7f
            val color = when {
                i < 2 -> Color(0xFFFF1744)      // Rojo (pico)
                i < 4 -> Color(0xFFFFEA00)      // Amarillo
                else -> Color(0xFF00E676)       // Verde
            }
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) color else color.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
private fun TransportIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .background(Color(0xFF1A2435), CircleShape)
            .border(1.dp, Color(0xFF2A3A50), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFB0C4DE),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun StationDisplay(
    name: String,
    slogan: String,
    isLive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A1220), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1A3050), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Sensors,
                contentDescription = null,
                tint = if (isLive) Color(0xFF00E5FF) else Color(0xFF556677),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isLive) "EN VIVO" else "DETENIDO",
                color = if (isLive) Color(0xFF00E5FF) else Color(0xFF667788),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = slogan,
            color = Color(0xFF7A9AAB),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BandButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF003D5C) else Color(0xFF121A28)
    val border = if (selected) Color(0xFF00E5FF) else Color(0xFF2A3A50)
    val content = if (selected) Color(0xFF00E5FF) else Color(0xFF8A9AAA)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = content,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
