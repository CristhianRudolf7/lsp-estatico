package pe.uncp.senas.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.uncp.senas.R
import pe.uncp.senas.data.Alphabet
import pe.uncp.senas.data.SignLetter
import pe.uncp.senas.ui.theme.Palette

@Composable
private fun rememberSignImage(letter: SignLetter): ImageBitmap? {
    val context = LocalContext.current
    return remember(letter.asset) {
        runCatching {
            context.assets.open(Alphabet.assetPath(letter)).use { BitmapFactory.decodeStream(it).asImageBitmap() }
        }.getOrNull()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetScreen(onSpeak: (String) -> Unit, bottomInset: Dp) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(0f to Color(0xFF17142B), 0.35f to Palette.Background)
        )
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomInset + 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Header() }
            items(Alphabet.letters, key = { it.letter }) { letter ->
                LetterCard(letter, onClick = { selected = letter.letter })
            }
        }
    }

    val current = Alphabet.letters.firstOrNull { it.letter == selected }
    if (current != null) {
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Palette.Surface,
            dragHandle = {
                Box(Modifier.padding(top = 12.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Palette.Outline))
            },
        ) {
            LetterDetail(current, onSpeak = { onSpeak(current.spokenName) })
        }
    }
}

@Composable
private fun Header() {
    Column(Modifier.padding(top = 20.dp, bottom = 8.dp)) {
        Text("Alfabeto", style = MaterialTheme.typography.headlineLarge, color = Palette.Text)
        Spacer(Modifier.height(6.dp))
        Text(
            "Guía de referencia del alfabeto dactilológico. Toca una letra para verla de cerca y escuchar su nombre.",
            style = MaterialTheme.typography.bodyMedium, color = Palette.Muted,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill("24 se reconocen", Palette.Teal)
            StatusPill("J, Ñ, Z con movimiento", Palette.Amber)
        }
    }
}

@Composable
private fun LetterCard(letter: SignLetter, onClick: () -> Unit) {
    val image = rememberSignImage(letter)
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(shape)
            .background(Palette.Surface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
            .clickable(role = Role.Button, onClickLabel = "Ver letra ${letter.letter}", onClick = onClick),
    ) {
        if (image != null) {
            Image(
                image, contentDescription = "Seña de la letra ${letter.letter}",
                colorFilter = ColorFilter.tint(Palette.Text),
                modifier = Modifier.align(Alignment.Center).padding(top = 18.dp, start = 14.dp, end = 14.dp, bottom = 10.dp)
                    .fillMaxSize().alpha(if (letter.recognized) 0.92f else 0.5f),
            )
        }
        Text(
            letter.letter,
            style = MaterialTheme.typography.titleLarge.copy(
                brush = if (letter.recognized) Palette.AccentHorizontal else Brush.linearGradient(listOf(Palette.Amber, Palette.Amber)),
            ),
            modifier = Modifier.padding(start = 12.dp, top = 8.dp),
        )
        if (!letter.recognized) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(10.dp).size(8.dp).clip(CircleShape).background(Palette.Amber)
            )
        }
    }
}

@Composable
private fun LetterDetail(letter: SignLetter, onSpeak: () -> Unit) {
    val image = rememberSignImage(letter)
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = Palette.Muted, fontSize = 16.sp)) { append("Letra  ") }
                withStyle(SpanStyle(brush = Palette.AccentHorizontal)) { append(letter.letter) }
            },
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp, lineHeight = 48.sp),
        )
        Text("Se pronuncia “${letter.spokenName}”", style = MaterialTheme.typography.bodyMedium, color = Palette.Muted)
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(28.dp))
                .background(Brush.radialGradient(listOf(Palette.Violet.copy(alpha = 0.22f), Palette.SurfaceHigh))),
            contentAlignment = Alignment.Center,
        ) {
            if (image != null) {
                Image(
                    image, contentDescription = "Seña de la letra ${letter.letter}",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        if (!letter.recognized) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Palette.Amber.copy(alpha = 0.12f)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_info), null, tint = Palette.Amber, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Esta letra se hace con movimiento, así que el traductor de señas estáticas no la reconoce.",
                    style = MaterialTheme.typography.bodyMedium, color = Palette.Text,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        GradientButton("Escuchar", onClick = onSpeak, icon = R.drawable.ic_volume_up, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
    }
}
