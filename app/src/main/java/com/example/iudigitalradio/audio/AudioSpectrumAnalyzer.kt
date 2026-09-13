package com.example.iudigitalradio.audio

import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Analizador de espectro REAL para el audio que Media3 está reproduciendo.
 *
 * Recibe el PCM decodificado directamente desde el pipeline de Media3 mediante
 * TeeAudioProcessor. No usa valores aleatorios ni una animación prefabricada.
 *
 * Las barras representan energía por bandas de frecuencia, de graves a agudos.
 */
class AudioSpectrumAnalyzer : TeeAudioProcessor.AudioBufferSink {

    companion object {
        const val BAR_COUNT = 36
        private const val FFT_SIZE = 2048
        private const val HOP_SAMPLES = 1024
        private const val MIN_FREQUENCY_HZ = 35.0
        private const val MAX_DB = -12.0
        private const val MIN_DB = -78.0
    }

    private val _spectrum = MutableStateFlow(FloatArray(BAR_COUNT))
    val spectrum: StateFlow<FloatArray> = _spectrum.asStateFlow()

    private var sampleRateHz = 44100
    private var channelCount = 2
    private var encoding = C.ENCODING_PCM_16BIT

    private val monoBuffer = FloatArray(FFT_SIZE)
    private var monoCount = 0

    private val real = DoubleArray(FFT_SIZE)
    private val imag = DoubleArray(FFT_SIZE)
    private val window = DoubleArray(FFT_SIZE) { index ->
        // Hann window para reducir leakage espectral.
        0.5 - 0.5 * cos(2.0 * PI * index / (FFT_SIZE - 1))
    }

    private val smoothed = FloatArray(BAR_COUNT)
    private var lastPublishMs = 0L

    override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
        this.sampleRateHz = sampleRateHz.coerceAtLeast(8000)
        this.channelCount = channelCount.coerceAtLeast(1)
        this.encoding = encoding
        monoCount = 0
        smoothed.fill(0f)
        _spectrum.value = FloatArray(BAR_COUNT)
    }

    override fun handleBuffer(buffer: ByteBuffer) {
        if (!buffer.hasRemaining()) return

        // El buffer recibido pertenece al pipeline de Media3. Lo consumimos sin
        // modificar su posición mediante una vista independiente.
        val view = buffer.slice().order(ByteOrder.nativeOrder())

        when (encoding) {
            C.ENCODING_PCM_16BIT -> readPcm16(view)
            C.ENCODING_PCM_FLOAT -> readPcmFloat(view)
            C.ENCODING_PCM_8BIT -> readPcm8(view)
            else -> return
        }
    }

    private fun readPcm16(buffer: ByteBuffer) {
        val bytesPerFrame = channelCount * 2
        while (buffer.remaining() >= bytesPerFrame) {
            var sum = 0.0
            repeat(channelCount) {
                sum += buffer.short.toInt() / 32768.0
            }
            appendSample((sum / channelCount).toFloat())
        }
    }

    private fun readPcmFloat(buffer: ByteBuffer) {
        val bytesPerFrame = channelCount * 4
        while (buffer.remaining() >= bytesPerFrame) {
            var sum = 0.0
            repeat(channelCount) {
                sum += buffer.float.toDouble()
            }
            appendSample((sum / channelCount).toFloat().coerceIn(-1f, 1f))
        }
    }

    private fun readPcm8(buffer: ByteBuffer) {
        val bytesPerFrame = channelCount
        while (buffer.remaining() >= bytesPerFrame) {
            var sum = 0.0
            repeat(channelCount) {
                // PCM 8-bit en Android es unsigned.
                sum += ((buffer.get().toInt() and 0xFF) - 128) / 128.0
            }
            appendSample((sum / channelCount).toFloat())
        }
    }

    private fun appendSample(sample: Float) {
        monoBuffer[monoCount++] = sample
        if (monoCount == FFT_SIZE) {
            analyzeFrame()
            // 50% overlap: conservamos la segunda mitad.
            System.arraycopy(monoBuffer, HOP_SAMPLES, monoBuffer, 0, FFT_SIZE - HOP_SAMPLES)
            monoCount = FFT_SIZE - HOP_SAMPLES
        }
    }

    private fun analyzeFrame() {
        for (i in 0 until FFT_SIZE) {
            real[i] = monoBuffer[i] * window[i]
            imag[i] = 0.0
        }

        fft(real, imag)

        val bars = FloatArray(BAR_COUNT)
        val nyquist = sampleRateHz / 2.0
        val maxFrequency = minOf(nyquist, 18000.0)
        val ratio = maxFrequency / MIN_FREQUENCY_HZ

        for (bar in 0 until BAR_COUNT) {
            // Bandas logarítmicas: más resolución donde el oído es más sensible
            // y separación natural de graves, medios y agudos.
            val lowHz = MIN_FREQUENCY_HZ * Math.pow(ratio, bar.toDouble() / BAR_COUNT)
            val highHz = MIN_FREQUENCY_HZ * Math.pow(ratio, (bar + 1).toDouble() / BAR_COUNT)

            val lowBin = ((lowHz * FFT_SIZE) / sampleRateHz)
                .toInt().coerceIn(1, FFT_SIZE / 2 - 1)
            val highBin = ((highHz * FFT_SIZE) / sampleRateHz)
                .toInt().coerceIn(lowBin + 1, FFT_SIZE / 2)

            var energy = 0.0
            var count = 0
            for (bin in lowBin until highBin) {
                val magnitude = sqrt(real[bin] * real[bin] + imag[bin] * imag[bin])
                energy += magnitude * magnitude
                count++
            }

            val rms = sqrt(energy / max(1, count))
            // Normalización aproximada del FFT + conversión a dB.
            val db = (20.0 * log10(rms / FFT_SIZE + 1e-9))
                .coerceIn(MIN_DB, MAX_DB)
            val normalized = ((db - MIN_DB) / (MAX_DB - MIN_DB)).toFloat()

            // Compresión suave para que las barras sean legibles incluso con
            // emisoras que entregan un nivel de señal relativamente bajo.
            bars[bar] = sqrt(normalized.coerceIn(0f, 1f))
        }

        // Suavizado ataque/caída: rápido para golpes, lento para evitar jitter.
        for (i in 0 until BAR_COUNT) {
            val target = bars[i]
            val attack = 0.62f
            val release = 0.20f
            smoothed[i] += (target - smoothed[i]) * if (target > smoothed[i]) attack else release
        }

        // No publicamos más rápido que ~30 FPS para no sobrecargar Compose.
        val now = SystemClock.elapsedRealtime()
        if (now - lastPublishMs >= 33L) {
            lastPublishMs = now
            _spectrum.value = smoothed.copyOf()
        }
    }

    fun close() {
        monoCount = 0
        smoothed.fill(0f)
        _spectrum.value = FloatArray(BAR_COUNT)
    }

    /**
     * FFT Cooley-Tukey iterativa, sin dependencias externas.
     */
    private fun fft(real: DoubleArray, imag: DoubleArray) {
        var j = 0
        for (i in 1 until FFT_SIZE) {
            var bit = FFT_SIZE shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit

            if (i < j) {
                val tr = real[i]
                real[i] = real[j]
                real[j] = tr

                val ti = imag[i]
                imag[i] = imag[j]
                imag[j] = ti
            }
        }

        var length = 2
        while (length <= FFT_SIZE) {
            val angle = -2.0 * PI / length
            val wLenReal = cos(angle)
            val wLenImag = sin(angle)

            var i = 0
            while (i < FFT_SIZE) {
                var wReal = 1.0
                var wImag = 0.0

                for (k in 0 until length / 2) {
                    val even = i + k
                    val odd = i + k + length / 2

                    val oddReal = real[odd] * wReal - imag[odd] * wImag
                    val oddImag = real[odd] * wImag + imag[odd] * wReal

                    real[odd] = real[even] - oddReal
                    imag[odd] = imag[even] - oddImag
                    real[even] += oddReal
                    imag[even] += oddImag

                    val nextWReal = wReal * wLenReal - wImag * wLenImag
                    wImag = wReal * wLenImag + wImag * wLenReal
                    wReal = nextWReal
                }
                i += length
            }
            length = length shl 1
        }
    }
}
