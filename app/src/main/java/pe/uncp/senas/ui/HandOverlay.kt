package pe.uncp.senas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import pe.uncp.senas.camera.HandLandmarks
import pe.uncp.senas.ui.theme.Palette

private val CONNECTIONS = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4,
    0 to 5, 5 to 6, 6 to 7, 7 to 8,
    5 to 9, 9 to 10, 10 to 11, 11 to 12,
    9 to 13, 13 to 14, 14 to 15, 15 to 16,
    13 to 17, 0 to 17, 17 to 18, 18 to 19, 19 to 20,
)
private val TIPS = setOf(4, 8, 12, 16, 20)

/**
 * Dibuja el esqueleto de la mano sobre la vista previa. La vista previa usa
 * FILL_CENTER, así que se replica el mismo escalado y recorte.
 */
@Composable
fun HandOverlay(hand: HandLandmarks?, mirrored: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (hand == null) return@Canvas
        val scale = maxOf(size.width / hand.imageWidth, size.height / hand.imageHeight)
        val dx = (size.width - hand.imageWidth * scale) / 2f
        val dy = (size.height - hand.imageHeight * scale) / 2f
        val pts = hand.points.map { p ->
            val x = p[0] * hand.imageWidth * scale + dx
            Offset(if (mirrored) size.width - x else x, p[1] * hand.imageHeight * scale + dy)
        }
        val unit = size.minDimension / 100f
        val brush = Brush.linearGradient(listOf(Palette.Violet, Palette.Teal), pts[0], pts[12])
        CONNECTIONS.forEach { (a, b) ->
            drawLine(Color.Black.copy(alpha = 0.25f), pts[a], pts[b], strokeWidth = unit * 1.6f, cap = StrokeCap.Round)
            drawLine(brush, pts[a], pts[b], strokeWidth = unit * 0.9f, cap = StrokeCap.Round)
        }
        pts.forEachIndexed { i, o ->
            val r = if (i in TIPS) unit * 1.5f else unit * 1.05f
            drawCircle(Palette.Teal.copy(alpha = 0.25f), r * 2.2f, o)
            drawCircle(Color.White, r, o)
        }
    }
}
