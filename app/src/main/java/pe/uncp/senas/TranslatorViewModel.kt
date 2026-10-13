package pe.uncp.senas

import android.app.Application
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.uncp.senas.camera.HandLandmarks
import pe.uncp.senas.camera.HandTracker
import pe.uncp.senas.data.Alphabet
import pe.uncp.senas.ml.ExtraTreesModel
import pe.uncp.senas.ml.HandFeatures
import pe.uncp.senas.ml.SignStabilizer
import pe.uncp.senas.speech.Speaker

data class Guess(val label: String, val confidence: Float)

data class TranslationState(
    val ready: Boolean = false,
    val hand: HandLandmarks? = null,
    val guess: Guess? = null,
    val stable: String? = null,
    val alternatives: List<Guess> = emptyList(),
    val error: String? = null,
)

class TranslatorViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(TranslationState())
    val state: StateFlow<TranslationState> = _state.asStateFlow()

    private val _voiceOn = MutableStateFlow(true)
    val voiceOn: StateFlow<Boolean> = _voiceOn.asStateFlow()

    private val speaker = Speaker(app)
    private val stabilizer = SignStabilizer()
    @Volatile private var model: ExtraTreesModel? = null
    @Volatile private var tracker: HandTracker? = null
    private var lastSpoken: String? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                model = app.assets.open("extra_trees.bin").use { ExtraTreesModel.load(it) }
                tracker = HandTracker(app, ::onHand) { msg -> _state.update { it.copy(error = msg) } }
                _state.update { it.copy(ready = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "No se pudo cargar el modelo") }
            }
        }
    }

    fun analyze(proxy: ImageProxy) {
        val t = tracker
        if (t == null) proxy.close() else t.analyze(proxy)
    }

    fun toggleVoice() = _voiceOn.update { !it }

    fun say(text: String) = speaker.speak(text)

    fun sayLabel(label: String) {
        if (label != " ") speaker.speak(Alphabet.spokenName(label) ?: label)
    }

    private fun onHand(hand: HandLandmarks?) {
        val m = model
        if (hand == null || m == null) {
            stabilizer.reset()
            lastSpoken = null
            _state.update { it.copy(hand = null, guess = null, stable = null, alternatives = emptyList()) }
            return
        }
        val features = HandFeatures.compute(HandFeatures.toReference(hand.points, hand.imageWidth, hand.imageHeight))
        if (features == null) {
            _state.update { it.copy(hand = hand) }
            return
        }
        val proba = m.predictProba(features)
        val ranked = proba.indices.sortedByDescending { proba[it] }
        val best = Guess(m.classes[ranked[0]], proba[ranked[0]].toFloat())
        val alternatives = ranked.drop(1).take(2)
            .map { Guess(m.classes[it], proba[it].toFloat()) }
            .filter { it.confidence >= 0.05f }
        val stable = stabilizer.push(best.label, proba[ranked[0]])

        if (stable != null && stable != lastSpoken) {
            lastSpoken = stable
            if (_voiceOn.value) sayLabel(stable)
        }
        _state.update { it.copy(hand = hand, guess = best, stable = stable, alternatives = alternatives) }
    }

    override fun onCleared() {
        tracker?.close()
        speaker.shutdown()
    }
}
