package pe.uncp.senas.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.uncp.senas.R
import pe.uncp.senas.ui.theme.Palette

/** Superficie translúcida con borde sutil, pensada para flotar sobre la cámara. */
@Composable
fun Glass(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    alpha: Float = 0.72f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .clip(shape)
            .background(Palette.Background.copy(alpha = alpha))
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape),
        content = content,
    )
}

@Composable
fun GlassIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val bg by animateColorAsState(
        if (active) Palette.Violet.copy(alpha = 0.9f) else Palette.Background.copy(alpha = 0.6f),
        label = "bg",
    )
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.AccentHorizontal)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

/** Píldora con un punto de color, para estados breves. */
@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(7.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

enum class Tab(val title: String, @param:DrawableRes val icon: Int) {
    Translate("Traducir", R.drawable.ic_sign_language),
    Alphabet("Alfabeto", R.drawable.ic_menu_book),
}

/** Barra de navegación flotante en forma de píldora. */
@Composable
fun FloatingNav(current: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Glass(modifier, shape = CircleShape, alpha = 0.85f) {
        Row(Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Tab.entries.forEach { tab ->
                NavItem(tab, selected = tab == current, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun NavItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1f else 0.96f, label = "scale")
    val background: Brush = if (selected) Palette.AccentHorizontal else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    Row(
        Modifier
            .scale(scale)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(tab.icon), null,
            tint = if (selected) Color.White else Palette.Muted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            tab.title,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else Palette.Muted,
        )
    }
}

/** Barra de progreso con degradado. */
@Composable
fun GradientBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "progress")
    Box(
        modifier
            .height(height)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(CircleShape)
                .background(Palette.AccentHorizontal)
        )
    }
}
