package np.ict.mad.mad25_p03_team03

import com.google.firebase.firestore.Exclude

data class SongItem(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val audioUrl: String = "",
    val lyrics: String = "",
    @get:Exclude // Tells Firebase to ignore this field
    var drawableId: Int = R.drawable.arcanepic
)