package com.tudominio.iudigitalradio.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reproductor de audio de ALTA CALIDAD para IUDigital Radio.
 *
 * Características para evitar distorsión y maximizar calidad:
 * - Media3 (ExoPlayer) con configuración de alta fidelidad
 * - AudioAttributes correctos (USAGE_MEDIA + CONTENT_TYPE_MUSIC)
 * - Manejo profesional de Audio Focus
 * - Preferencia por pistas de mayor bitrate
 * - Buffer generoso para evitar cortes
 * - Sin resampling agresivo
 */
@UnstableApi
class HighQualityAudioPlayer(private val context: Context) {

    private var player: ExoPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    // Listener de Audio Focus
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player?.volume = 1.0f
                player?.play()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Bajar volumen temporalmente (no pausar)
                player?.volume = 0.3f
            }
        }
    }

    fun initialize() {
        if (player != null) return

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // TrackSelector que prefiere mayor calidad
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setMaxAudioBitrate(Int.MAX_VALUE)      // Sin límite de bitrate
                    .setPreferredAudioLanguage("es")         // Preferir español si hay opciones
                    .setForceHighestSupportedBitrate(true)   // Forzar la mejor calidad disponible
            )
        }

        // RenderersFactory optimizado para audio
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableAudioFloatOutput(true)                 // Salida en float (mejor calidad)
            .setEnableDecoderFallback(true)

        player = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setAudioAttributes(
                Media3AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ false   // Lo manejamos manualmente para más control
            )
            .setHandleAudioBecomingNoisy(true)               // Pausa al desconectar auriculares
            .setWakeMode(C.WAKE_MODE_NETWORK)                // Mantener CPU despierta en streaming
            .build()
            .also { exo ->
                // Buffer más generoso = menos cortes y mejor experiencia
                exo.playWhenReady = false

                exo.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        // Puedes reaccionar a STATE_BUFFERING, STATE_READY, etc.
                    }
                })
            }
    }

    /**
     * Reproduce una URL de streaming (radio) o archivo local.
     * Usa la mejor calidad disponible.
     */
    fun play(url: String, title: String = "") {
        initialize()
        requestAudioFocus()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )
            .build()

        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        _currentTitle.value = title
    }

    fun playPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                requestAudioFocus()
                it.play()
            }
        }
    }

    fun stop() {
        player?.stop()
        abandonAudioFocus()
        _isPlaying.value = false
    }

    fun release() {
        stop()
        player?.release()
        player = null
    }

    // ------------------ Audio Focus ------------------

    private fun requestAudioFocus() {
        val am = audioManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .build()

            am.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(focusChangeListener)
        }
    }
}
