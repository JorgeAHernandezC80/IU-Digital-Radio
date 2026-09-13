package com.example.iudigitalradio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iudigitalradio.ui.theme.*

/**
 * Pantallas extra para dar más funcionalidad sin romper tu MainActivity.
 *
 * Uso en PantallaPrincipal:
 *   var pantalla by rememberSaveable { mutableStateOf("player") } // player | favoritos | web | ajustes
 *   when (pantalla) {
 *     "player" -> { ... tu UI actual ... }
 *     "favoritos" -> PantallaFavoritos(...)
 *     "web" -> PantallaWeb(...)
 *   }
 */

@Composable
fun PantallaFavoritos(
    favoritos: Set<Int>,
    seleccionada: Int,
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), RadioDeepBlue)))
            .systemBarsPadding()
    ) {
        TopBarExtra("Favoritos", onBack)

        val indices = favoritos.sorted()
        if (indices.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.StarBorder, null, tint = RadioTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Sin favoritos", color = RadioTextSecondary, fontSize = 15.sp)
                    Text("Toca ★ en una emisora para guardarla", color = RadioTextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(indices) { _, index ->
                    val est = catalogoEmisoras.getOrNull(index) ?: return@itemsIndexed
                    EstacionRow(
                        estacion = est,
                        selected = index == seleccionada,
                        isFavorite = true,
                        onClick = { onSelect(index) },
                        onToggleFavorite = { onToggleFavorite(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaWeb(
    seleccionada: Int,
    favoritos: Set<Int>,
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onBack: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtradas = remember(query) {
        catalogoEmisoras.mapIndexed { i, e -> i to e }
            .filter { (_, e) ->
                query.isBlank() ||
                    e.nombre.contains(query, true) ||
                    e.genero.contains(query, true) ||
                    e.region.contains(query, true)
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), RadioDeepBlue)))
            .systemBarsPadding()
    ) {
        TopBarExtra("Estaciones WEB", onBack)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar emisora, género o región…", color = RadioTextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RadioCyan,
                unfocusedBorderColor = Color(0xFF1E3548),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = RadioCyan,
                focusedContainerColor = Color(0xFF0A1420),
                unfocusedContainerColor = Color(0xFF0A1420)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(filtradas) { _, (index, est) ->
                EstacionRow(
                    estacion = est,
                    selected = index == seleccionada,
                    isFavorite = index in favoritos,
                    onClick = { onSelect(index) },
                    onToggleFavorite = { onToggleFavorite(index) }
                )
            }
        }
    }
}

@Composable
private fun TopBarExtra(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1420))
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, null, tint = RadioCyan)
        }
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun EstacionRow(
    estacion: Estacion,
    selected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF0D2742) else Color(0xFF071321))
            .border(
                1.dp,
                if (selected) RadioCyan.copy(alpha = .5f) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Radio, null,
            tint = if (selected) RadioCyan else RadioTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                estacion.nombre,
                color = if (selected) Color.White else RadioTextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${estacion.genero} · ${estacion.region}",
                color = RadioTextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                null,
                tint = if (isFavorite) RadioCyan else RadioTextMuted
            )
        }
    }
}
