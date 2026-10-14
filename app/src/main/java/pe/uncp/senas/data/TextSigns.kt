package pe.uncp.senas.data

import java.text.Normalizer
import java.util.Locale

data class SignToken(val letter: String, val sign: SignLetter?)

object TextSigns {
    private val byLetter = Alphabet.letters.associateBy { it.letter }
    private val accents = mapOf('Á' to 'A', 'É' to 'E', 'Í' to 'I', 'Ó' to 'O', 'Ú' to 'U', 'Ü' to 'U')
    fun sequence(text: String): List<SignToken> =
        Normalizer.normalize(text, Normalizer.Form.NFC).uppercase(Locale.ROOT).codePoints().toArray().map { point ->
            val original = String(Character.toChars(point))
            val letter = if (Character.isWhitespace(point) || Character.isSpaceChar(point)) " "
                else (if (original.length == 1) accents[original[0]] else null)?.toString() ?: original
            SignToken(letter, byLetter[letter])
        }
}
