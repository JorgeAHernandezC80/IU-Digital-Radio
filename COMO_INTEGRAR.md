# Cómo integrar en tu MainActivity.kt

Tu código actual ya tiene:
- FFT real (`AudioSpectrumAnalyzer`)
- Volumen que **sube y baja** (`detectVerticalDragGestures`)
- Chasis metálico profesional
- ExoPlayer de alta calidad

## 1. Spectrum más brutal (con ROJO, sin fucsia)

1. Copia `EspectroAudioBrutal.kt` a tu package `com.example.iudigitalradio`
2. En `PantallaPrincipal`, cambia:

```kotlin
// ANTES
EspectroAudio(estaReproduciendo, spectrumValues)

// DESPUÉS
EspectroAudioBrutal(estaReproduciendo, spectrumValues)
```

Sigue usando **datos reales de FFT**. Solo cambia el estilo visual a barras arcoíris que terminan en **rojo**.

---

## 2. Más funcionalidades (Favoritos + WEB)

1. Copia `PantallasExtra.kt` a tu package
2. En `PantallaPrincipal` agrega estado:

```kotlin
var pantalla by rememberSaveable { mutableStateOf("player") } // player | favoritos | web
var favoritos by rememberSaveable { mutableStateOf(setOf<Int>()) }
```

3. Envuelve el contenido actual así:

```kotlin
when (pantalla) {
    "favoritos" -> PantallaFavoritos(
        favoritos = favoritos,
        seleccionada = estacionSeleccionada,
        onSelect = {
            estacionSeleccionada = it
            estaReproduciendo = true
            pantalla = "player"
            vibrarDispositivo(context)
        },
        onToggleFavorite = { idx ->
            favoritos = if (idx in favoritos) favoritos - idx else favoritos + idx
        },
        onBack = { pantalla = "player" }
    )
    "web" -> PantallaWeb(
        seleccionada = estacionSeleccionada,
        favoritos = favoritos,
        onSelect = {
            estacionSeleccionada = it
            estaReproduciendo = true
            pantalla = "player"
            vibrarDispositivo(context)
        },
        onToggleFavorite = { idx ->
            favoritos = if (idx in favoritos) favoritos - idx else favoritos + idx
        },
        onBack = { pantalla = "player" }
    )
    else -> {
        // ===== TODO TU CONTENIDO ACTUAL DEL PLAYER =====
        Column(modifier = Modifier.fillMaxSize()...) {
            ChasisMetalico { ... }
            // ...
            CatalogoCompacto(...)
        }
    }
}
```

4. Conecta el selector de bandas:

```kotlin
BandasSelector(preset) { nueva ->
    preset = nueva
    vibrarDispositivo(context)
    when (nueva) {
        "FAVORITOS" -> pantalla = "favoritos"
        "WEB" -> pantalla = "web"
        // FM / AM pueden filtrar catálogo más adelante
    }
}
```

5. Conecta el botón de favorito (el de ★ abajo):

```kotlin
SmallActionButton(Icons.Default.FavoriteBorder, RadioCyan) {
    val idx = estacionSeleccionada
    favoritos = if (idx in favoritos) favoritos - idx else favoritos + idx
    vibrarDispositivo(context)
}
```

(Opcional: cambia el icono a `Star` cuando ya es favorito.)

---

## 3. Volumen (ya lo tienes bien)

Tu `RotaryKnob` ya permite subir y bajar:

```kotlin
val nuevo = (valorActual.value - dragAmount / 260f).coerceIn(0f, 1f)
```

Y se aplica así:

```kotlin
LaunchedEffect(silenciado, volumen) {
    exoPlayer.volume = if (silenciado) 0f else volumen
}
```

No hace falta cambiarlo.

---

## 4. Próximos pasos recomendados

| Prioridad | Funcionalidad |
|-----------|----------------|
| Alta | Foreground Service + notificación MediaStyle (radio en segundo plano) |
| Alta | Persistencia de favoritos (DataStore / SharedPreferences) |
| Media | Filtro FM/AM por región o género |
| Media | Sleep timer |
| Baja | Más emisoras en el catálogo |

Si quieres, en el siguiente mensaje te preparo el **Foreground Service** completo para que la radio no se corte al salir de la app.
