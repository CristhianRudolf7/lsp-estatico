package pe.uncp.senas.ml

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementación en Kotlin del ExtraTreesClassifier entrenado con scikit-learn.
 * Lee el binario generado por tools/export_model.py y reproduce predict_proba:
 * cada árbol aporta la distribución de su hoja y se promedian todas.
 */
class ExtraTreesModel private constructor(
    val classes: List<String>,
    private val nFeatures: Int,
    private val trees: Array<Tree>,
) {
    private class Tree(
        val left: IntArray,
        val right: IntArray,
        val feature: ShortArray,
        val threshold: DoubleArray,
        val leafProba: FloatArray,
    )

    fun predictProba(x: DoubleArray): DoubleArray {
        require(x.size == nFeatures) { "Se esperaban $nFeatures valores y llegaron ${x.size}" }
        // scikit-learn convierte las entradas a float32 antes de recorrer los árboles.
        val xf = DoubleArray(nFeatures) { x[it].toFloat().toDouble() }
        val nClasses = classes.size
        val out = DoubleArray(nClasses)
        for (t in trees) {
            var node = 0
            while (t.feature[node] >= 0) {
                node = if (xf[t.feature[node].toInt()] <= t.threshold[node]) t.left[node] else t.right[node]
            }
            val offset = (-t.left[node] - 1) * nClasses
            for (c in 0 until nClasses) out[c] += t.leafProba[offset + c].toDouble()
        }
        for (c in 0 until nClasses) out[c] /= trees.size
        return out
    }

    companion object {
        fun load(input: InputStream): ExtraTreesModel {
            val buf = ByteBuffer.wrap(input.readBytes()).order(ByteOrder.LITTLE_ENDIAN)
            val magic = ByteArray(4).also { buf.get(it) }
            check(String(magic) == "ETRF") { "Archivo de modelo no válido" }
            check(buf.int == 1) { "Versión de modelo no soportada" }
            val nFeatures = buf.int
            val nClasses = buf.int
            val nTrees = buf.int
            val classes = List(nClasses) {
                val bytes = ByteArray(buf.get().toInt() and 0xFF).also { b -> buf.get(b) }
                String(bytes, Charsets.UTF_8)
            }
            val trees = Array(nTrees) {
                val nNodes = buf.int
                val nLeaves = buf.int
                val left = IntArray(nNodes).also { buf.asIntBuffer().get(it); buf.position(buf.position() + nNodes * 4) }
                val right = IntArray(nNodes).also { buf.asIntBuffer().get(it); buf.position(buf.position() + nNodes * 4) }
                val feature = ShortArray(nNodes).also { buf.asShortBuffer().get(it); buf.position(buf.position() + nNodes * 2) }
                val threshold = DoubleArray(nNodes).also { buf.asDoubleBuffer().get(it); buf.position(buf.position() + nNodes * 8) }
                val proba = FloatArray(nLeaves * nClasses).also { buf.asFloatBuffer().get(it); buf.position(buf.position() + it.size * 4) }
                Tree(left, right, feature, threshold, proba)
            }
            return ExtraTreesModel(classes, nFeatures, trees)
        }
    }
}
