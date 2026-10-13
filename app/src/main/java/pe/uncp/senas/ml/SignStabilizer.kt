package pe.uncp.senas.ml

/**
 * Suaviza las predicciones cuadro a cuadro: una letra se considera "fijada"
 * cuando domina la ventana reciente con suficiente confianza media.
 */
class SignStabilizer(
    private val window: Int = 8,
    private val minVotes: Int = 6,
    private val minConfidence: Double = 0.45,
) {
    private val history = ArrayDeque<Pair<String, Double>>()

    fun reset() = history.clear()

    /** Añade una predicción y devuelve la letra estable, si la hay. */
    fun push(label: String, confidence: Double): String? {
        history.addLast(label to confidence)
        if (history.size > window) history.removeFirst()
        val votes = history.filter { it.first == label }
        if (votes.size < minVotes) return null
        return if (votes.sumOf { it.second } / votes.size >= minConfidence) label else null
    }
}
