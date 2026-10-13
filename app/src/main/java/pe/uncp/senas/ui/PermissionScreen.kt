package pe.uncp.senas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.uncp.senas.R
import pe.uncp.senas.ui.theme.Palette

@Composable
fun PermissionScreen(permanentlyDenied: Boolean, onRequest: () -> Unit, bottomInset: Dp) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(
                listOf(Palette.Indigo.copy(alpha = 0.35f), Palette.Background),
                radius = 1400f,
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(start = 28.dp, end = 28.dp, bottom = bottomInset + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(200.dp).clip(CircleShape).background(Palette.Violet.copy(alpha = 0.08f)))
                Box(Modifier.size(150.dp).clip(CircleShape).background(Palette.Violet.copy(alpha = 0.12f)))
                Box(
                    Modifier.size(104.dp).clip(CircleShape).background(Palette.Accent)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_sign_language), null, tint = Color.White, modifier = Modifier.size(52.dp))
                }
            }
            Spacer(Modifier.height(36.dp))
            Text(
                "Traduce señas\nal instante",
                style = MaterialTheme.typography.headlineLarge,
                color = Palette.Text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Muestra una letra del alfabeto dactilológico a la cámara y la verás escrita y escucharás su nombre.",
                style = MaterialTheme.typography.bodyLarge,
                color = Palette.Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Row(
                Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.06f)).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Palette.Teal))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Todo se procesa en tu teléfono, sin internet",
                    style = MaterialTheme.typography.labelMedium, color = Palette.Text,
                )
            }
            Spacer(Modifier.height(36.dp))
            GradientButton(
                text = if (permanentlyDenied) "Abrir ajustes" else "Activar cámara",
                onClick = onRequest,
                icon = R.drawable.ic_photo_camera,
                modifier = Modifier.fillMaxWidth(),
            )
            if (permanentlyDenied) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "El permiso de cámara está desactivado. Actívalo en Ajustes > Permisos.",
                    style = MaterialTheme.typography.bodyMedium, color = Palette.Muted, textAlign = TextAlign.Center,
                )
            }
        }
    }
}
