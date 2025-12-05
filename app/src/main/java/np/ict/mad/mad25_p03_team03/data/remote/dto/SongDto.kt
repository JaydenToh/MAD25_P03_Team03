package np.ict.mad.mad25_p03_team03.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val id: Int,
    val title: String,
    val artist: String? = null, // Handle nullable explicitly
    @SerialName("audio_url") val audioUrl: String, // Maps snake_case JSON to camelCase Kotlin
    @SerialName("fake_options") val fakeOptions: List<String> = emptyList()
)