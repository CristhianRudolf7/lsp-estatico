package pe.uncp.senas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pe.uncp.senas.R

object Palette {
    val Background = Color(0xFF0B0B14)
    val Surface = Color(0xFF15151F)
    val SurfaceHigh = Color(0xFF1E1E2B)
    val Outline = Color(0xFF2B2A3C)
    val Violet = Color(0xFF8B6CFF)
    val Indigo = Color(0xFF5B4BDB)
    val Teal = Color(0xFF22C7B8)
    val Amber = Color(0xFFFFB547)
    val Text = Color(0xFFF4F3FF)
    val Muted = Color(0xFFA19FBA)

    val Accent = Brush.linearGradient(listOf(Violet, Indigo, Teal))
    val AccentHorizontal = Brush.horizontalGradient(listOf(Violet, Teal))
}

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun jakarta(weight: Int) = Font(
    R.font.jakarta,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Jakarta = FontFamily(jakarta(400), jakarta(500), jakarta(600), jakarta(700), jakarta(800))

private val typography = Typography(
    displayLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 96.sp, lineHeight = 96.sp),
    headlineLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
)

private val colors = darkColorScheme(
    primary = Palette.Violet,
    onPrimary = Color.White,
    secondary = Palette.Teal,
    background = Palette.Background,
    onBackground = Palette.Text,
    surface = Palette.Surface,
    onSurface = Palette.Text,
    surfaceVariant = Palette.SurfaceHigh,
    onSurfaceVariant = Palette.Muted,
    surfaceContainerLow = Palette.Surface,
    surfaceContainer = Palette.Surface,
    surfaceContainerHigh = Palette.SurfaceHigh,
    outline = Palette.Outline,
)

@Composable
fun SenasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
