package pe.uncp.senas.ml

import org.junit.Assert.*
import org.junit.Test
import pe.uncp.senas.data.TextSigns

class TextSignsTest {
    @Test fun preservesLettersAndSpaces() {
        val tokens = TextSigns.sequence("¡Hola, Perú! Niño\nü 2026 👍")
        assertEquals("¡HOLA, PERU! NIÑO U 2026 👍", tokens.joinToString("") { it.letter })
        assertEquals("N_TILDE", tokens.first { it.letter == "Ñ" }.sign?.asset)
        assertNull(tokens.last().sign)
    }
    @Test fun handlesUnicodeAndRepeatedDynamicLetters() {
        assertEquals("ÑA", TextSigns.sequence("n\u0303a\u0301").joinToString("") { it.letter })
        assertEquals(2, TextSigns.sequence("jj").size)
        assertFalse(TextSigns.sequence("jj").first().sign!!.recognized)
        assertTrue(TextSigns.sequence("").isEmpty())
    }
}
