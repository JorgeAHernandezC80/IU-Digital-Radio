# IUDigital Radio — Visualizador + UI Profesional

## Lo que se ve ahora

El **spectrum analyzer** tiene este estilo:

- Gradiente: **Amarillo → Naranja → Verde → Cian → Azul → ROJO**
- (Se eliminó el fucsia/magenta y se reemplazó por rojo intenso)
- Barras con **glow neón**, highlight superior y **reflejo** inferior
- Animación fluida y profesional
- Se intensifica solo cuando está reproduciendo

Además incluye la pantalla completa estilo radio:
`RadioIUDigitalScreen.kt` (header, spectrum, controles, knobs, botones FM/AM/WEB/FAVORITOS)

---

## Archivos principales

| Archivo | Descripción |
|---------|-------------|
| `ui/SpectrumVisualizerPro.kt` | Visualizador de espectro brutal y profesional |
| `ui/RadioIUDigitalScreen.kt` | Pantalla completa estilo "RADIO IU DIGITAL" |
| `ui/EqualizerVisualizer.kt` | Versión anterior (equalizer clásico) |
| `audio/HighQualityAudioPlayer.kt` | Reproductor de alta calidad sin distorsión |
| `ui/RadioViewModel.kt` | ViewModel de conexión |

---

## Uso rápido

```kotlin
@Composable
fun MainRadioScreen(viewModel: RadioViewModel = viewModel()) {
    val isPlaying by viewModel.isPlaying.collectAsState()

    RadioIUDigitalScreen(
        isPlaying = isPlaying,
        stationName = "RADIO IU DIGITAL",
        slogan = "TU MÚSICA, SIEMPRE",
        onPlayPause = { viewModel.playPause() },
        onNext = { viewModel.next() },
        onPrevious = { viewModel.previous() }
    )
}
```

Solo el spectrum (si quieres integrarlo en otra pantalla):

```kotlin
SpectrumVisualizerPro(
    isPlaying = isPlaying,
    modifier = Modifier
        .fillMaxWidth()
        .height(170.dp)
)
```

---

## Color del spectrum (confirmado)

Ya no hay fucsia. El final del gradiente es **rojo** (`#D50000` → `#FF1744`).
