package np.ict.mad.mad25_p03_team03

import androidx.compose.runtime.mutableStateListOf

data class IdentifiedSongItem(
    val id: Long,
    val title: String,
    val artist: String,
    val mood: String?
)

object IdentifiedSongHistory {
    val items = mutableStateListOf<IdentifiedSongItem>()
    private var nextId = 1L

    fun addFromSongText(songText: String, mood: String?) {
        // Expects "Song: X\nArtist: Y"
        val parts = songText.split('\n')
        val title = parts.getOrNull(0)?.removePrefix("Song: ")?.trim().orEmpty()
        val artist = parts.getOrNull(1)?.removePrefix("Artist: ")?.trim().orEmpty()

        if (title.isBlank() && artist.isBlank()) return

        items.add(
            IdentifiedSongItem(
                id = nextId++,
                title = title,
                artist = artist,
                mood = mood
            )
        )
    }

    fun updateMood(id: Long, newMood: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = items[index]
            items[index] = old.copy(mood = newMood)
        }
    }

    fun updateLastMood(newMood: String) {
        if (items.isEmpty()) return
        val lastIndex = items.lastIndex
        val last = items[lastIndex]
        items[lastIndex] = last.copy(mood = newMood)
    }
}
