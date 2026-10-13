package pe.uncp.senas.ml

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * Comprueba que el cálculo de características y el modelo en Kotlin dan los
 * mismos resultados que el código Python original con scikit-learn.
 * Los vectores se generan con tools/export_model.py.
 */
class ParityTest {
    private val data = JSONObject(javaClass.getResource("/vectors.json")!!.readText())
    private val model = File("src/main/assets/extra_trees.bin").inputStream().use { ExtraTreesModel.load(it) }

    @Test
    fun classesMatch() {
        val expected = data.getJSONArray("classes").let { a -> List(a.length()) { a.getString(it) } }
        assertEquals(expected, model.classes)
    }

    @Test
    fun featuresAndPredictionsMatchPython() {
        val cases = data.getJSONArray("cases")
        var predicted = 0
        for (i in 0 until cases.length()) {
            val case = cases.getJSONObject(i)
            val lm = case.getJSONArray("landmarks")
            val points = List(lm.length()) { j ->
                val p = lm.getJSONArray(j)
                doubleArrayOf(p.getDouble(0), p.getDouble(1), p.getDouble(2))
            }
            val feats = HandFeatures.compute(points)
            val expectedFeats = case.getJSONArray("features")
            if (expectedFeats.length() != 16) {
                assertNull("caso $i", feats)
                continue
            }
            for (k in 0 until 16) assertEquals("caso $i, rasgo $k", expectedFeats.getDouble(k), feats!![k], 1e-9)

            val proba = model.predictProba(feats!!)
            val expectedProba = case.getJSONArray("proba")
            for (c in proba.indices) assertEquals("caso $i, clase $c", expectedProba.getDouble(c), proba[c], 1e-6)
            val best = proba.indices.maxByOrNull { proba[it] }!!
            assertEquals("caso $i", case.getString("label"), model.classes[best])
            predicted++
        }
        println("Casos verificados: $predicted de ${cases.length()}")
    }
}
