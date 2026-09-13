package com.tudominio.iudigitalradio.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudominio.iudigitalradio.audio.HighQualityAudioPlayer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel simple para conectar el EqualizerVisualizer
 * con el reproductor de alta calidad.
 */
class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val player = HighQualityAudioPlayer(application)

    val isPlaying: StateFlow<Boolean> = player.isPlaying
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentTitle: StateFlow<String> = player.currentTitle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        player.initialize()
    }

    fun playStation(url: String, title: String) {
        player.play(url, title)
    }

    fun playPause() {
        player.playPause()
    }

    fun stop() {
        player.stop()
    }

    fun next() {
        // Aquí puedes implementar la lógica de siguiente estación
    }

    fun previous() {
        // Aquí puedes implementar la lógica de estación anterior
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
