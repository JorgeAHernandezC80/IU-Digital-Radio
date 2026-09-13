# 📻 IU Digital Radio

App de radio online para Android, hecha con Kotlin y Jetpack Compose. Es un proyecto de aprendizaje para el curso **Programación de Dispositivos Móviles** (IU Digital de Antioquia), así que el código todavía está en proceso de mejora; cada entrega le agrego algo nuevo y aprendo un poco más de Compose en el camino.

La app reproduce streams de radio en vivo, muestra un ecualizador visual que reacciona al audio real (no animaciones falsas) y tiene perillas de volumen/sintonización que se controlan con gestos.

## Estructura del proyecto

```
IUDigitalRadio/
└── app/src/main/
    ├── java/com/example/iudigitalradio/
    │   ├── MainActivity.kt        # Pantalla principal, reproductor y lógica de audio
    │   ├── audio/
    │   │   └── AudioSpectrumAnalyzer.kt   # Análisis FFT del audio en tiempo real
    │   └── ui/theme/               # Colores, tipografía y tema de la app
    └── res/                        # Íconos, splash screen y recursos gráficos
```

> Nota de aprendizaje: dentro del proyecto quedaron algunos archivos sueltos (`ui/`, `audio/` en la raíz) de versiones anteriores que ya no se usan. Los dejé de momento como referencia mientras termino de limpiar el repo.

## Herramientas y tecnologías usadas

- **Kotlin** — lenguaje principal
- **Jetpack Compose** — toda la interfaz (sin XML de layouts)
- **Media3 ExoPlayer** — reproducción del stream de radio
- **Canvas + gestos de Compose** — para el ecualizador y las perillas funcionales
- **Splash Screen API (AndroidX)** — pantalla de bienvenida
- **Android Studio** — compilado con Gradle Kotlin DSL (`compileSdk 35`, `minSdk 24`)

## Cómo correrlo

1. Clonar el repositorio
2. Abrir la carpeta en Android Studio
3. Esperar a que sincronice Gradle
4. Ejecutar en un emulador o dispositivo (necesita conexión a internet para el streaming)

---

## Copyright

**Español:**
© 2026 Jorge A. Hernández. Este es un proyecto académico desarrollado para el curso Programación de Dispositivos Móviles de IU Digital de Antioquia, con fines exclusivamente educativos. Todos los derechos reservados por el autor; no está autorizado su uso comercial sin permiso.

**English:**
© 2026 Jorge A. Hernández. This is an academic project developed for the Mobile Device Programming course at IU Digital de Antioquia, for educational purposes only. All rights reserved by the author; commercial use is not authorized without permission.
