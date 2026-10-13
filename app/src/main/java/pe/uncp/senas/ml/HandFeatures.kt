package pe.uncp.senas.ml

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Calcula las 16 características que espera el modelo: 15 ángulos entre
 * articulaciones de los dedos y la orientación de la mano (muñeca → base del
 * dedo medio). Es una traducción fiel del cálculo de TraductorEstatico/app/app.py.
 */
object HandFeatures {
    // El modelo se entrenó con una webcam de 640×480; MediaPipe normaliza x por el
    // ancho e y por el alto, así que reescalamos para conservar la misma geometría.
    private const val REF_WIDTH = 640.0
    private const val REF_HEIGHT = 480.0

    /** Convierte puntos normalizados de MediaPipe (x, y, z) al sistema de referencia del entrenamiento. */
    fun toReference(normalized: List<FloatArray>, imageWidth: Int, imageHeight: Int): List<DoubleArray> =
        normalized.map { p ->
            doubleArrayOf(
                p[0] * imageWidth / REF_WIDTH,
                p[1] * imageHeight / REF_HEIGHT,
                p[2] * imageWidth / REF_WIDTH,
            )
        }

    /** Devuelve las 16 características, o null si algún ángulo no se pudo calcular. */
    fun compute(points: List<DoubleArray>): DoubleArray? {
        if (points.size != 21) return null
        val angles = ArrayList<Double>(16)
        for (i in 0 until 21) {
            if (i == 0 || i == 4 || i == 8 || i == 12 || i == 16 || i == 20) continue
            val prev = if (i == 5 || i == 9 || i == 13 || i == 17) points[0] else points[i - 1]
            val a = dist(prev, points[i + 1])
            val b = dist(prev, points[i])
            val c = dist(points[i + 1], points[i])
            if (a != 0.0 && b != 0.0 && c != 0.0) {
                val cos = (b * b + c * c - a * a) / (2 * b * c)
                // Python usa round(), que redondea al par más cercano; Math.rint hace lo mismo.
                angles += if (cos > -1 && cos < 1) Math.rint(Math.toDegrees(acos(cos))) else 0.0
            }
        }
        val dx = points[9][0] - points[0][0]
        val dy = (1 - points[9][1]) - (1 - points[0][1])
        var orientation = Math.toDegrees(atan2(dy, dx))
        if (orientation < 0) orientation += 360
        angles += orientation
        return if (angles.size == 16) angles.toDoubleArray() else null
    }

    private fun dist(p: DoubleArray, q: DoubleArray): Double {
        val dx = p[0] - q[0]
        val dy = p[1] - q[1]
        val dz = p[2] - q[2]
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
