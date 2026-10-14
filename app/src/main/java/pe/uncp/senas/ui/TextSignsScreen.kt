package pe.uncp.senas.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.uncp.senas.data.SignToken
import pe.uncp.senas.data.TextSigns
import pe.uncp.senas.ui.theme.Palette

@Composable
fun TextSignsScreen(bottomInset: Dp) {
    var text by rememberSaveable { mutableStateOf("") }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var milliseconds by rememberSaveable { mutableLongStateOf(1500L) }
    val tokens = remember(text) { TextSigns.sequence(text) }
    val current = tokens.getOrNull(index)
    val unsupported = remember(tokens) { tokens.filter { it.sign == null && it.letter != " " }.map { it.letter }.distinct() }
    LaunchedEffect(playing, index, tokens.size, milliseconds) {
        if (playing) {
            delay(milliseconds)
            if (index >= tokens.lastIndex) playing = false else index++
        }
    }
    fun step(next: Int) { playing = false; index = next }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(96.dp),
        modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = bottomInset + 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Texto a señas", style = MaterialTheme.typography.headlineLarge, color = Palette.Text)
                Text("Escribe una frase y recorre sus letras en el alfabeto dactilológico peruano.", color = Palette.Muted)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(280); index = 0; playing = false },
                    label = { Text("Tu frase") }, placeholder = { Text("Por ejemplo: Hola, Perú") },
                    supportingText = { Text("${text.length}/280 caracteres") },
                    minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth(),
                )
                Text("Esta guía muestra el deletreo letra por letra. La LSP tiene su propia gramática y esta función no traduce frases completas a esa gramática. Las vocales con tilde usan su letra base y la Ñ conserva su seña.", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
                if (unsupported.isNotEmpty()) Text("Sin seña disponible: ${unsupported.joinToString(" ")}. Aparecen como pausas de referencia.", color = Palette.Amber, style = MaterialTheme.typography.bodyMedium)
                Text("Tiempo por seña", color = Palette.Text)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1000L to "1 s", 1500L to "1,5 s", 2500L to "2,5 s").forEach { (time, label) ->
                        FilterChip(selected = milliseconds == time, onClick = { milliseconds = time }, label = { Text(label) })
                    }
                    TextButton(onClick = { text = ""; index = 0; playing = false }, enabled = text.isNotEmpty()) { Text("Limpiar") }
                }
                Text(if (current == null) "Escribe para empezar" else "Paso ${index + 1} de ${tokens.size}", color = Palette.Muted)
                TokenImage(current, Modifier.fillMaxWidth().height(220.dp))
                Text(current?.let { if (it.sign != null) "Letra ${it.letter}" else if (it.letter == " ") "Espacio entre palabras" else "Carácter ${it.letter}: sin seña disponible" } ?: "Tu frase, en señas", color = Palette.Text, style = MaterialTheme.typography.titleLarge)
                if (current?.sign?.recognized == false) Text("${current.letter} requiere movimiento. Sigue la trayectoria de la ilustración.", color = Palette.Amber)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(enabled = index > 0, onClick = { step(index - 1) }) { Text("Anterior") }
                    Button(enabled = tokens.isNotEmpty(), onClick = {
                        if (!playing && index == tokens.lastIndex) index = 0
                        playing = !playing
                    }) { Text(if (playing) "Pausar" else "Reproducir") }
                    TextButton(enabled = current != null && index < tokens.lastIndex, onClick = { step(index + 1) }) { Text("Siguiente") }
                }
                TextButton(enabled = tokens.isNotEmpty(), onClick = { step(0) }) { Text("Volver al inicio") }
                if (tokens.isNotEmpty()) Text("Tu frase, paso a paso", style = MaterialTheme.typography.titleLarge, color = Palette.Text)
            }
        }
        itemsIndexed(tokens) { i, token ->
            Column(
                Modifier.fillMaxWidth().border(2.dp, if (i == index) Palette.Violet else Palette.Outline, RoundedCornerShape(16.dp))
                    .background(Palette.Surface, RoundedCornerShape(16.dp)).clickable(role = Role.Button, onClickLabel = "Ver paso ${i + 1}") { step(i) }.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TokenImage(token, Modifier.fillMaxWidth().height(95.dp))
                Text(if (token.letter == " ") "Espacio" else token.letter, color = Palette.Text)
                if (token.sign?.recognized == false) Text("Movimiento", style = MaterialTheme.typography.labelSmall, color = Palette.Amber)
            }
        }
    }
}

@Composable
private fun TokenImage(token: SignToken?, modifier: Modifier) {
    val sign = token?.sign
    Box(modifier.background(Palette.SurfaceHigh, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        if (sign != null) {
            val image = rememberSignImage(sign)
            if (image != null) Image(image, contentDescription = "Seña de la letra ${sign.letter}", colorFilter = ColorFilter.tint(Palette.Text), modifier = Modifier.fillMaxSize().padding(12.dp))
            else Text("Ilustración no disponible", color = Palette.Muted)
        } else Text(if (token == null) "Escribe una frase" else if (token.letter == " ") "Pausa" else token.letter, color = Palette.Muted)
    }
}
