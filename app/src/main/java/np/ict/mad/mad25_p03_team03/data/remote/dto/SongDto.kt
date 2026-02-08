package np.ict.mad.mad25_p03_team03.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Class - Data Transfer Object - Defines the structure for song data received from the remote API
// Flow 1.0: Data Class Definition
@Serializable
data class SongDto(
    // Variable - Property - Unique integer identifier for the song record
    // Flow 1.1: ID Field Initialization
    val id: Int,

    // Variable - Property - The actual title of the song
    // Flow 1.2: Title Field Initialization
    val title: String,

    // Variable - Property - The name of the artist, which may be null if unknown
    // Flow 1.3: Artist Field Initialization
    val artist: String? = null,

    // Variable - Property - The remote URL link to the audio file
    // Flow 1.4: Audio URL Field Initialization (Mapping from snake_case)
    @SerialName("audio_url")
    val audioUrl: String,

    // Variable - Property - A list of incorrect song titles used for game options
    // Flow 1.5: Fake Options Field Initialization
    @SerialName("fake_options")
    val fakeOptions: List<String> = emptyList()
)