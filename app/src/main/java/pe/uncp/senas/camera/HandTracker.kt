package pe.uncp.senas.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

/** Puntos de la mano detectada, normalizados (0–1) respecto a la imagen ya rotada. */
class HandLandmarks(
    val points: List<FloatArray>,
    val imageWidth: Int,
    val imageHeight: Int,
)

/** Envuelve el HandLandmarker de MediaPipe en modo de transmisión en vivo. */
class HandTracker(
    context: Context,
    private val onResult: (HandLandmarks?) -> Unit,
    onError: (String) -> Unit,
) {
    private val landmarker: HandLandmarker = HandLandmarker.createFromOptions(
        context,
        HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(
                BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task")
                    .setDelegate(Delegate.CPU)
                    .build()
            )
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, image -> deliver(result, image.width, image.height) }
            .setErrorListener { e -> onError(e.message ?: "Error de MediaPipe") }
            .build()
    )

    private var lastTimestamp = 0L

    /** Procesa un cuadro de CameraX. Cierra el ImageProxy. */
    fun analyze(proxy: ImageProxy) {
        val rotation = proxy.imageInfo.rotationDegrees
        val frame = proxy.use { it.toBitmap() }
        val upright = if (rotation == 0) frame else {
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(frame, 0, 0, frame.width, frame.height, m, true)
        }
        // MediaPipe exige marcas de tiempo estrictamente crecientes.
        val ts = maxOf(SystemClock.uptimeMillis(), lastTimestamp + 1)
        lastTimestamp = ts
        landmarker.detectAsync(BitmapImageBuilder(upright).build(), ts)
    }

    private fun deliver(result: HandLandmarkerResult, width: Int, height: Int) {
        val hand = result.landmarks().firstOrNull()
        if (hand == null || hand.size != 21) {
            onResult(null)
            return
        }
        onResult(HandLandmarks(hand.map { floatArrayOf(it.x(), it.y(), it.z()) }, width, height))
    }

    fun close() = landmarker.close()
}
