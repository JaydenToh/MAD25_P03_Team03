package np.ict.mad.mad25_p03_team03.data

// Class - Data Model - Represents a simplified structure for song information
// Flow 1.0: Object Definition
data class SimpleSong(
    // Variable - Property - Unique identifier for the song record
    // Flow 1.1: ID initialization
    val id: String? = "",

    // Variable - Property - The name/title of the song
    // Flow 1.2: Title initialization
    val title: String? = "",

    // Variable - Property - The name of the artist or performer
    // Flow 1.3: Artist initialization
    val artist: String? = ""
)