package np.ict.mad.mad25_p03_team03.data

// --- GAME DATA STRUCTURES ---

/**
 * Data class to represent a song choice (Option)
 */
data class SongChoice(val title: String, val artist: String)

/**
 * Data class to represent a single question
 */
data class Question(
    val options: List<SongChoice>,
    val correctIndex: Int // Index of the correct answer in the options list
)

// --- MOCK GAME DATA ---

val mockQuestions = listOf(
    Question(
        options = listOf(
            SongChoice("As It Was", "Harry Styles"),
            SongChoice("Levitating", "Dua Lipa"),
            SongChoice("Good 4 U", "Olivia Rodrigo"),
            SongChoice("Heat Waves", "Glass Animals")
        ),
        correctIndex = 1
    ),
    Question(
        options = listOf(
            SongChoice("Shape of You", "Ed Sheeran"),
            SongChoice("Blinding Lights", "The Weeknd"),
            SongChoice("Dynamite", "BTS"),
            SongChoice("Save Your Tears", "The Weeknd")
        ),
        correctIndex = 2
    ),
    Question(
        options = listOf(
            SongChoice("Bad Guy", "Billie Eilish"),
            SongChoice("Havana", "Camila Cabello"),
            SongChoice("Shallow", "Lady Gaga & Bradley Cooper"),
            SongChoice("Despacito", "Luis Fonsi") // Correct
        ),
        correctIndex = 3
    )
)