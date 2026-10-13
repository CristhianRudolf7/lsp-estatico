package pe.uncp.senas.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Texto a voz en español usando el motor del teléfono. */
class Speaker(context: Context) {
    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) ready = configureLanguage()
    }

    private fun configureLanguage(): Boolean {
        val candidates = listOf("es-PE", "es-US", "es-ES", "es").map(Locale::forLanguageTag)
        return candidates.any { tts.setLanguage(it) >= TextToSpeech.LANG_AVAILABLE }
    }

    fun speak(text: String) {
        if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
