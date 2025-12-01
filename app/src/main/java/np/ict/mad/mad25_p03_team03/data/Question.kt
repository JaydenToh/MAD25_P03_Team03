package np.ict.mad.mad25_p03_team03.data.SongChoice

import SongChoice

data class Question(
    val songUrl: String,
    val options: List<SongChoice>,
    val correctIndex: Int
)