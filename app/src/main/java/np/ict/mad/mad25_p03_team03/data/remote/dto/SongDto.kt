package np.ict.mad.mad25_p03_team03.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable // 👈 必须加这个
data class SongDto(
    val id: Int,
    val title: String,
    val artist: String? = null, // 处理可能为 null 的情况

    @SerialName("audio_url") // 数据库是 snake_case，代码用 camelCase
    val audioUrl: String,

    @SerialName("fake_options")
    val fakeOptions: List<String> = emptyList() // 👈 直接转为 List，不要用 String
)