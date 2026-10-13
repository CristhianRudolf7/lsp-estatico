package pe.uncp.senas.data

/** Letra del alfabeto dactilológico con su nombre hablado y su lámina de referencia. */
data class SignLetter(
    val letter: String,
    val spokenName: String,
    val asset: String,
    /** J, Ñ y Z se hacen con movimiento y el modelo estático no las reconoce. */
    val recognized: Boolean = true,
)

object Alphabet {
    val letters = listOf(
        SignLetter("A", "a", "A"),
        SignLetter("B", "be", "B"),
        SignLetter("C", "ce", "C"),
        SignLetter("D", "de", "D"),
        SignLetter("E", "e", "E"),
        SignLetter("F", "efe", "F"),
        SignLetter("G", "ge", "G"),
        SignLetter("H", "hache", "H"),
        SignLetter("I", "i", "I"),
        SignLetter("J", "jota", "J", recognized = false),
        SignLetter("K", "ka", "K"),
        SignLetter("L", "ele", "L"),
        SignLetter("M", "eme", "M"),
        SignLetter("N", "ene", "N"),
        SignLetter("Ñ", "eñe", "N_TILDE", recognized = false),
        SignLetter("O", "o", "O"),
        SignLetter("P", "pe", "P"),
        SignLetter("Q", "cu", "Q"),
        SignLetter("R", "erre", "R"),
        SignLetter("S", "ese", "S"),
        SignLetter("T", "te", "T"),
        SignLetter("U", "u", "U"),
        SignLetter("V", "uve", "V"),
        SignLetter("W", "uve doble", "W"),
        SignLetter("X", "equis", "X"),
        SignLetter("Y", "ye", "Y"),
        SignLetter("Z", "zeta", "Z", recognized = false),
    )

    private val byLetter = letters.associateBy { it.letter }

    fun spokenName(label: String): String? = byLetter[label]?.spokenName

    fun assetPath(letter: SignLetter) = "signos/${letter.asset}.png"
}
