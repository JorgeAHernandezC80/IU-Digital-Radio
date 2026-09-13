package com.example.iudigitalradio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import com.example.iudigitalradio.audio.AudioSpectrumAnalyzer
import com.example.iudigitalradio.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// Modelo de una emisora del catálogo
data class Estacion(
    val nombre: String,
    val genero: String,
    val streamUrl: String,
    val region: String,
)

val catalogoEmisoras = listOf(
    Estacion("Radio Lounge", "Chill / Lounge", "https://ice1.somafm.com/groovesalad-128-mp3", "Estados Unidos"),
    Estacion("Radio Electrónica", "Deep House", "https://ice1.somafm.com/beatblender-128-mp3", "Estados Unidos"),
    Estacion("Radio Swiss Jazz", "Jazz", "http://stream.srg-ssr.ch/m/rsj/mp3_128", "Europa"),
    Estacion("Radio Swiss Pop", "Pop", "http://stream.srg-ssr.ch/m/rsp/mp3_128", "Europa"),
    Estacion("Radio UNAL Bogotá", "Radio universitaria / Cultura", "https://radio.unal.edu.co/streaming/bogota/;stream.mp3", "Colombia")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            IUDigitalRadioTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = RadioDeepBlue) {
                    PantallaConBienvenida()
                }
            }
        }
    }
}

fun vibrarDispositivo(context: Context) {
    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(45)
        }
    }
}

// ============================================================
//  BIENVENIDA (splash en Compose): logo nítido + nombre de la
//  emisora, con animación de entrada. Se muestra un instante
//  apenas abre la app y luego cede el paso al reproductor.
// ============================================================

@Composable
fun PantallaConBienvenida() {
    var mostrarBienvenida by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1900)
        mostrarBienvenida = false
    }

    Crossfade(targetState = mostrarBienvenida, animationSpec = tween(550), label = "bienvenida") { mostrando ->
        if (mostrando) SplashPersonalizado() else PantallaPrincipal()
    }
}

@Composable
fun SplashPersonalizado() {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), RadioDeepBlue, Color(0xFF06152A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Resplandor detrás del logo
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .graphicsLayer { alpha = progreso.value }
                        .background(
                            Brush.radialGradient(listOf(RadioCyan.copy(alpha = .30f), Color.Transparent))
                        )
                )
                Image(
                    painter = painterResource(R.drawable.splash_hero),
                    contentDescription = "Radio IU Digital",
                    modifier = Modifier
                        .size(172.dp)
                        .graphicsLayer {
                            val escala = 0.82f + 0.18f * progreso.value
                            scaleX = escala
                            scaleY = escala
                            alpha = progreso.value
                        }
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = progreso.value }
            ) {
                Text("RADIO", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Spacer(Modifier.width(8.dp))
                Text("IU DIGITAL", color = RadioCyan, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "TU MÚSICA, SIEMPRE",
                color = RadioTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer { alpha = progreso.value }
            )

            Spacer(Modifier.height(36.dp))

            // Barra de carga estilizada
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF14202E))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progreso.value.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.horizontalGradient(listOf(RadioBlue, RadioCyan)))
                )
            }
        }
    }
}

// ============================================================
//  PANTALLA PRINCIPAL
// ============================================================

@Composable
fun PantallaPrincipal() {
    val context = LocalContext.current
    var fotoPerfil by remember { mutableStateOf<Bitmap?>(null) }
    var estacionSeleccionada by rememberSaveable { mutableIntStateOf(0) }
    var estaReproduciendo by rememberSaveable { mutableStateOf(false) }
    var silenciado by rememberSaveable { mutableStateOf(false) }
    var estaCargando by remember { mutableStateOf(false) }
    var volumen by rememberSaveable { mutableFloatStateOf(0.78f) }
    var preset by rememberSaveable { mutableStateOf("FM") }

    // El espectro recibe PCM real del mismo pipeline de Media3 que reproduce la radio.
    // No se generan barras aleatorias ni una animación independiente del audio.
    val spectrumAnalyzer = remember { AudioSpectrumAnalyzer() }
    val exoPlayer = remember {
        @OptIn(UnstableApi::class)
        fun createPlayer(): ExoPlayer {
            val renderersFactory = object : DefaultRenderersFactory(context) {
                override fun buildAudioSink(
                    context: Context,
                    enableFloatOutput: Boolean,
                    enableAudioTrackPlaybackParams: Boolean
                ): AudioSink {
                    return DefaultAudioSink.Builder(context)
                        .setAudioProcessors(arrayOf(TeeAudioProcessor(spectrumAnalyzer)))
                        .setEnableFloatOutput(enableFloatOutput)
                        .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                        .build()
                }
            }
            renderersFactory.setEnableAudioFloatOutput(true)
            renderersFactory.setEnableDecoderFallback(true)

            return ExoPlayer.Builder(context, renderersFactory).build()
        }
        createPlayer()
    }
    val spectrumValues by spectrumAnalyzer.spectrum.collectAsState()
    val listener = remember {
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                estaCargando = playbackState == Player.STATE_BUFFERING
            }
        }
    }

    DisposableEffect(Unit) {
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            spectrumAnalyzer.close()
        }
    }

    LaunchedEffect(estacionSeleccionada) {
        val estacion = catalogoEmisoras[estacionSeleccionada]
        exoPlayer.setMediaItem(MediaItem.fromUri(estacion.streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = estaReproduciendo
    }

    LaunchedEffect(estaReproduciendo) { exoPlayer.playWhenReady = estaReproduciendo }
    LaunchedEffect(silenciado, volumen) { exoPlayer.volume = if (silenciado) 0f else volumen }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) fotoPerfil = bitmap
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzadorCamara.launch(null)
        else Toast.makeText(context, context.getString(R.string.permiso_camara_denegado), Toast.LENGTH_SHORT).show()
    }
    fun abrirCamara() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzadorCamara.launch(null)
        } else lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    val estacion = catalogoEmisoras[estacionSeleccionada]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), RadioDeepBlue, Color(0xFF06152A))))
            // El fondo llena toda la pantalla (incluso detrás de la barra de estado),
            // pero el contenido interactivo respeta los márgenes del sistema para no
            // quedar oculto debajo del reloj/batería en edge-to-edge (targetSdk 35).
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        // ========== CHASIS METÁLICO: el reproductor "hardware" ==========
        ChasisMetalico {
            HeaderRadio(
                playing = estaReproduciendo,
                loading = estaCargando,
                foto = fotoPerfil,
                onCamera = ::abrirCamara
            )
            Spacer(Modifier.height(14.dp))

            EspectroAudio(estaReproduciendo, spectrumValues)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MedidorNiveles(estaReproduciendo, modifier = Modifier.width(34.dp).fillMaxHeight())

                ControlesReproduccion(
                    estaReproduciendo = estaReproduciendo,
                    estaCargando = estaCargando,
                    onPrevious = {
                        estacionSeleccionada = if (estacionSeleccionada == 0) catalogoEmisoras.lastIndex else estacionSeleccionada - 1
                        estaReproduciendo = true
                        vibrarDispositivo(context)
                    },
                    onPlayPause = { estaReproduciendo = !estaReproduciendo; vibrarDispositivo(context) },
                    onNext = {
                        estacionSeleccionada = (estacionSeleccionada + 1) % catalogoEmisoras.size
                        estaReproduciendo = true
                        vibrarDispositivo(context)
                    }
                )

                MedidorNiveles(estaReproduciendo, modifier = Modifier.width(34.dp).fillMaxHeight())
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Perilla de VOLUMEN: arrastra hacia arriba para subir, hacia abajo para bajar.
                RotaryKnob(
                    value = volumen,
                    onValueChange = { nuevo ->
                        volumen = nuevo
                        silenciado = false
                    },
                    label = "VOLUMEN",
                    diameter = 78.dp
                )

                PanelEstacion(
                    estacion = estacion,
                    playing = estaReproduciendo,
                    loading = estaCargando,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                )

                // Perilla de SINTONIZAR: arrastra para recorrer el catálogo de emisoras.
                RotaryKnob(
                    value = (estacionSeleccionada + 1f) / catalogoEmisoras.size,
                    onValueChange = { nuevo ->
                        estacionSeleccionada = (nuevo * catalogoEmisoras.size).roundToInt()
                            .coerceIn(1, catalogoEmisoras.size) - 1
                        estaReproduciendo = true
                    },
                    label = "SINTONIZAR",
                    diameter = 78.dp
                )
            }
            Spacer(Modifier.height(16.dp))

            BandasSelector(preset) { preset = it; vibrarDispositivo(context) }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallActionButton(
                if (silenciado) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                if (silenciado) RadioGold else RadioCyan
            ) {
                silenciado = !silenciado
                vibrarDispositivo(context)
            }
            Text(
                "${(volumen * 100).roundToInt()}%",
                color = RadioTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            SmallActionButton(Icons.Default.FavoriteBorder, RadioCyan) { vibrarDispositivo(context) }
        }
        Spacer(Modifier.height(18.dp))

        CatalogoCompacto(estacionSeleccionada) { index ->
            estacionSeleccionada = index
            estaReproduciendo = true
            vibrarDispositivo(context)
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ============================================================
//  CHASIS Y ENCABEZADO
// ============================================================

@Composable
fun ChasisMetalico(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = RadioCyan.copy(alpha = .35f),
                spotColor = RadioCyan.copy(alpha = .3f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF3A4C5F), Color(0xFF1C2530), Color(0xFF32424F),
                        Color(0xFF171F28), Color(0xFF44576B)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(RadioCyan.copy(alpha = .55f), Color(0xFF0E2A3D), RadioCyan.copy(alpha = .35f))
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun RadioWaveIcon(modifier: Modifier = Modifier, tint: Color) {
    Canvas(modifier) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = tint, radius = size.minDimension * 0.14f, center = centro)
        for (i in 1..2) {
            val factor = size.minDimension * 0.22f * (i + 1)
            val lado = androidx.compose.ui.geometry.Size(factor * 2f, factor * 2f)
            val alpha = 1f - i * 0.28f
            drawArc(
                color = tint.copy(alpha = alpha),
                startAngle = -55f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(centro.x - factor, centro.y - factor),
                size = lado,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
            )
            drawArc(
                color = tint.copy(alpha = alpha),
                startAngle = 125f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(centro.x - factor, centro.y - factor),
                size = lado,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun HeaderRadio(playing: Boolean, loading: Boolean, foto: Bitmap?, onCamera: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        RadioWaveIcon(modifier = Modifier.size(28.dp), tint = RadioCyan)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("RADIO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
                Spacer(Modifier.width(6.dp))
                Text("IU DIGITAL", color = RadioCyan, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
            }
            Text("TU MÚSICA, SIEMPRE", color = RadioTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.4.sp)
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B1A2D))
                .border(1.dp, RadioCyan.copy(alpha = .4f), CircleShape)
                .clickable(onClick = onCamera),
            contentAlignment = Alignment.Center
        ) {
            if (foto != null) Image(foto.asImageBitmap(), "Foto", Modifier.fillMaxSize().clip(CircleShape))
            else Icon(Icons.Default.CameraAlt, "Perfil", tint = RadioCyan, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(8.dp))
        EstadoPill(playing, loading)
    }
}

@Composable
fun EstadoPill(playing: Boolean, loading: Boolean) {
    val (label, color) = when {
        loading -> "CONECTANDO" to RadioGold
        playing -> "EN VIVO" to Color(0xFFFF3B4E)
        else -> "EN PAUSA" to RadioTextMuted
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF160B0D), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = .8f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
    }
}

// ============================================================
//  ESPECTRO DE AUDIO: ecualizador de LEDs (rojo/amarillo/verde)
//  con ondas suaves superpuestas, con datos reales de FFT.
// ============================================================

/** Color LED según la fila (de arriba hacia abajo): rojo -> naranja/amarillo -> verde. */
private fun colorFilaLed(filaDesdeArriba: Int, totalFilas: Int): Color = when {
    filaDesdeArriba < (totalFilas * 0.22f) -> Color(0xFFFF3B30)
    filaDesdeArriba < (totalFilas * 0.45f) -> Color(0xFFFF9500)
    filaDesdeArriba < (totalFilas * 0.68f) -> Color(0xFFFFD500)
    else -> Color(0xFF34D058)
}

/** Curva suave (spline por cuadráticas) a través de una lista de puntos. */
private fun trazoSuave(puntos: List<Offset>): androidx.compose.ui.graphics.Path {
    val path = androidx.compose.ui.graphics.Path()
    if (puntos.isEmpty()) return path
    path.moveTo(puntos[0].x, puntos[0].y)
    for (i in 0 until puntos.size - 1) {
        val p0 = puntos[i]
        val p1 = puntos[i + 1]
        val mid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
        path.quadraticBezierTo(p0.x, p0.y, mid.x, mid.y)
    }
    path.lineTo(puntos.last().x, puntos.last().y)
    return path
}

@Composable
fun EspectroAudio(playing: Boolean, spectrum: FloatArray) {
    val bars = if (spectrum.size == AudioSpectrumAnalyzer.BAR_COUNT) {
        spectrum
    } else {
        FloatArray(AudioSpectrumAnalyzer.BAR_COUNT)
    }

    // Fase animada para que la onda "roja" viaje y cruce a la onda "verde",
    // igual que en el visualizador de referencia.
    val transition = rememberInfiniteTransition(label = "ondaFase")
    val fase by transition.animateFloat(
        initialValue = 0f,
        targetValue = bars.size.toFloat(),
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "fase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF06070A))
            .border(1.dp, RadioCyan.copy(alpha = .45f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val filas = 9
            val gapCol = 3.dp.toPx()
            val gapFila = 3.dp.toPx()
            val colWidth = ((size.width - gapCol * (bars.size - 1)) / bars.size).coerceAtLeast(1f)
            val rowHeight = ((size.height - gapFila * (filas - 1)) / filas).coerceAtLeast(1f)

            // --- Rejilla de LEDs (encendidos desde abajo según el nivel de cada banda) ---
            bars.forEachIndexed { index, valor ->
                val nivel = if (playing) valor.coerceIn(0f, 1f) else (valor * .12f).coerceIn(0f, 1f)
                val filasEncendidas = (nivel * filas).roundToInt().coerceIn(0, filas)
                val x = index * (colWidth + gapCol)

                for (fila in 0 until filas) {
                    val filaDesdeAbajo = fila
                    val filaDesdeArriba = filas - 1 - fila
                    val encendido = filaDesdeAbajo < filasEncendidas
                    val color = colorFilaLed(filaDesdeArriba, filas)
                    val y = size.height - (fila + 1) * rowHeight - fila * gapFila

                    drawRoundRect(
                        color = if (encendido) color else color.copy(alpha = .12f),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(colWidth, rowHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // --- Ondas suaves superpuestas (verde: nivel real, roja: nivel desfasado) ---
            val centerBase = size.height * 0.52f
            val amplitud = size.height * 0.34f

            val puntosVerdes = bars.indices.map { i ->
                val x = i * (colWidth + gapCol) + colWidth / 2f
                val v = if (playing) bars[i].coerceIn(0f, 1f) else 0.08f
                Offset(x, centerBase - v * amplitud)
            }
            val puntosRojos = bars.indices.map { i ->
                val iDesfasado = ((i + fase.toInt()) % bars.size + bars.size) % bars.size
                val x = i * (colWidth + gapCol) + colWidth / 2f
                val v = if (playing) bars[iDesfasado].coerceIn(0f, 1f) else 0.05f
                Offset(x, centerBase - v * amplitud * 0.85f)
            }

            val caminoVerde = trazoSuave(puntosVerdes)
            val caminoRojo = trazoSuave(puntosRojos)

            // Glow: varias pasadas con más grosor y menos opacidad detrás de la línea nítida.
            listOf(10.dp.toPx() to 0.08f, 6.dp.toPx() to 0.16f).forEach { (grosor, alpha) ->
                drawPath(caminoVerde, color = Color(0xFF34D058).copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(grosor, cap = StrokeCap.Round))
                drawPath(caminoRojo, color = Color(0xFFFF4D3D).copy(alpha = alpha), style = androidx.compose.ui.graphics.drawscope.Stroke(grosor, cap = StrokeCap.Round))
            }
            drawPath(caminoVerde, color = Color(0xFF7CFF9E), style = androidx.compose.ui.graphics.drawscope.Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
            drawPath(caminoRojo, color = Color(0xFFFF8A75), style = androidx.compose.ui.graphics.drawscope.Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
        }

        Text(
            "AUDIO SPECTRUM",
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            color = Color.White.copy(alpha = .45f),
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

// ============================================================
//  VU-METERS, TRANSPORTE Y PERILLAS
// ============================================================

@Composable
fun MedidorNiveles(playing: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "nivel")
    val nivel by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (playing) 1f else 0.3f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "nivelAnim"
    )

    val segmentos = 9
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF060B12))
            .border(1.dp, Color(0xFF1B2A3A), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(segmentos) { i ->
            val umbral = 1f - (i / segmentos.toFloat())
            val activo = playing && nivel > umbral * 0.65f
            val color = when {
                i < 2 -> Color(0xFFFF3B30)
                i < 4 -> Color(0xFFFFC400)
                else -> Color(0xFF33E27A)
            }
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(6.dp)
                    .padding(vertical = 1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (activo) color else color.copy(alpha = .16f))
            )
        }
    }
}

@Composable
fun ControlesReproduccion(
    estaReproduciendo: Boolean,
    estaCargando: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonTransporte(Icons.Default.FastRewind, onPrevious)

        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = RadioCyan.copy(alpha = .8f),
                    spotColor = RadioCyan.copy(alpha = .8f)
                )
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF1B2733), Color(0xFF0A1119))))
                .border(2.dp, Brush.sweepGradient(listOf(RadioCyan, RadioBlue, RadioCyan, RadioCyanSoft)), CircleShape)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            if (estaCargando) {
                CircularProgressIndicator(Modifier.size(30.dp), color = RadioCyan, strokeWidth = 3.dp)
            } else {
                Icon(
                    imageVector = if (estaReproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        BotonTransporte(Icons.Default.FastForward, onNext)
    }
}

@Composable
fun BotonTransporte(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1C2733), Color(0xFF0C131C))))
            .border(1.dp, Color(0xFF2A3A4C), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = .92f), modifier = Modifier.size(24.dp))
    }
}

/**
 * Perilla giratoria funcional: arrastra verticalmente (arriba = sube, abajo = baja).
 * Se usa tanto para VOLUMEN (controla exoPlayer.volume en tiempo real) como para
 * SINTONIZAR (recorre el catálogo de emisoras).
 */
@Composable
fun RotaryKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 84.dp
) {
    val valorActual = rememberUpdatedState(value)
    val onChangeActual = rememberUpdatedState(onValueChange)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(diameter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        // Arrastrar hacia arriba (dragAmount negativo) sube el valor.
                        val nuevo = (valorActual.value - dragAmount / 260f).coerceIn(0f, 1f)
                        onChangeActual.value(nuevo)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(10.dp, CircleShape, ambientColor = RadioCyan.copy(alpha = .5f), spotColor = RadioCyan.copy(alpha = .5f))
            ) {
                val strokeWidth = size.minDimension * 0.1f
                val startAngle = 135f
                val sweep = 270f
                val v = value.coerceIn(0f, 1f)

                // Riel de fondo
                drawArc(
                    color = Color(0xFF0E1B26),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
                )
                // Arco activo (neón)
                drawArc(
                    brush = Brush.sweepGradient(listOf(RadioBlue, RadioCyan, RadioCyanSoft, RadioCyan)),
                    startAngle = startAngle,
                    sweepAngle = sweep * v,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Cuerpo cromado de la perilla
                val faceRadius = size.minDimension * 0.32f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEDF1F5), Color(0xFF98A4B0), Color(0xFF3B4552), Color(0xFF14181D)),
                        center = Offset(center.x - faceRadius * 0.4f, center.y - faceRadius * 0.45f),
                        radius = faceRadius * 2.1f
                    ),
                    radius = faceRadius,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = .1f),
                    radius = faceRadius * 0.92f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )

                // Indicador de posición
                val angleRad = Math.toRadians((startAngle + sweep * v).toDouble())
                val pStart = Offset(
                    center.x + kotlin.math.cos(angleRad).toFloat() * faceRadius * 0.2f,
                    center.y + kotlin.math.sin(angleRad).toFloat() * faceRadius * 0.2f
                )
                val pEnd = Offset(
                    center.x + kotlin.math.cos(angleRad).toFloat() * faceRadius * 0.88f,
                    center.y + kotlin.math.sin(angleRad).toFloat() * faceRadius * 0.88f
                )
                drawLine(Color.White, pStart, pEnd, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color(0xFFB9D5EF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
    }
}

// ============================================================
//  PANEL DE ESTACIÓN Y SELECTOR DE BANDAS
// ============================================================

@Composable
fun PanelEstacion(estacion: Estacion, playing: Boolean, loading: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1420), Color(0xFF060D16))))
            .border(1.dp, Color(0xFF1E3548), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            RadioWaveIcon(modifier = Modifier.size(15.dp), tint = if (playing) RadioCyan else RadioTextMuted)
            Spacer(Modifier.width(6.dp))
            Text(
                text = when {
                    loading -> "CONECTANDO"
                    playing -> "EN VIVO"
                    else -> "DETENIDO"
                },
                color = if (playing) RadioCyan else RadioTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = .8.sp,
                modifier = Modifier.weight(1f)
            )
            MiniBars(playing)
        }
        Spacer(Modifier.height(6.dp))
        Text(estacion.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(estacion.genero, color = RadioTextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MiniBars(playing: Boolean) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(4) { i ->
            val transition = rememberInfiniteTransition(label = "miniBars$i")
            val h by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = if (playing) 1f else 0.35f,
                animationSpec = infiniteRepeatable(tween(480 + i * 90), RepeatMode.Reverse),
                label = "mini$i"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((6 + h * 12).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (playing) RadioCyan else RadioTextMuted.copy(alpha = .4f))
            )
        }
    }
}

@Composable
fun BandasSelector(selected: String, onSelect: (String) -> Unit) {
    val opciones = listOf(
        "FM" to Icons.Default.SettingsInputAntenna,
        "AM" to Icons.Default.SettingsInputAntenna,
        "WEB" to Icons.Default.Language,
        "FAVORITOS" to Icons.Default.Star
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        opciones.forEach { (label, icon) ->
            val activo = label == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activo) Color(0xFF06405C) else Color(0xFF0E1620))
                    .border(1.5.dp, if (activo) RadioCyan else Color(0xFF223140), RoundedCornerShape(12.dp))
                    .clickable { onSelect(label) }
                    .padding(vertical = 10.dp)
            ) {
                Icon(icon, null, tint = if (activo) RadioCyan else RadioTextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text(label, color = if (activo) RadioCyan else RadioTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp, maxLines = 1)
            }
        }
    }
}

// ============================================================
//  CONTROLES SECUNDARIOS Y CATÁLOGO
// ============================================================

@Composable
fun SmallActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A182A))
            .border(1.dp, tint.copy(alpha = .28f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun CatalogoCompacto(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        Text("EMISORAS", color = RadioCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        catalogoEmisoras.forEachIndexed { index, station ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (index == selected) Color(0xFF0D2742) else Color(0xFF071321))
                    .border(1.dp, if (index == selected) RadioCyan.copy(alpha = .5f) else Color.Transparent, RoundedCornerShape(14.dp))
                    .clickable { onSelect(index) }
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (index == selected) Brush.linearGradient(listOf(RadioBlue, RadioCyan))
                            else Brush.linearGradient(listOf(Color(0xFF15263C), Color(0xFF0A1524)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Radio, null, tint = if (index == selected) Color.White else RadioTextMuted, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(station.nombre, color = if (index == selected) Color.White else RadioTextSecondary, fontSize = 13.sp, fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(station.genero, color = RadioTextMuted, fontSize = 10.sp)
                }
                if (index == selected) Box(Modifier.size(8.dp).clip(CircleShape).background(RadioCyan))
            }
        }
    }
}
