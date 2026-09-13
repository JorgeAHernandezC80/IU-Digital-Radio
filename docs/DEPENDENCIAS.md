# Dependencias necesarias (app/build.gradle.kts)

```kotlin
dependencies {
    // ===== Media3 (ExoPlayer) - Reproducción de alta calidad =====
    val media3Version = "1.4.1"   // o la más reciente estable

    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // ===== Compose (ya las tienes) =====
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")

    // ===== Splash Screen (del paquete anterior) =====
    implementation("androidx.core:core-splashscreen:1.0.1")
}
```

## Permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<!-- Opcional pero recomendado para foreground service de radio -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```
