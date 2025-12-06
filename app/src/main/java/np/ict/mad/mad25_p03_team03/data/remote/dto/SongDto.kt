package np.ict.mad.mad25_p03_team03.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val id: Int,
    val title: String,
    val artist: String? = null, // for some songs artist can be null

    @SerialName("audio_url") // snakecase to camelcase
    val audioUrl: String,

    @SerialName("fake_options")
    val fakeOptions: List<String> = emptyList() // list of fake options
)