package np.ict.mad.mad25_p03_team03

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object IdentifiedSongHistory {

    data class Item(
        val title: String,
        val artist: String,
        val timestamp: String,
        val mood: String?
    )

    private val _items = mutableStateListOf<Item>()
    val items: List<Item> get() = _items

    private var isLoaded = false


    fun addFromSongText(songText: String, mood: String?) {
        val titlePrefix = "Song:"
        val artistPrefix = "Artist:"

        var title = "Unknown title"
        var artist = "Unknown artist"

        val lines = songText.split("\n")
        lines.forEach { line ->
            when {
                line.startsWith(titlePrefix) ->
                    title = line.removePrefix(titlePrefix).trim()

                line.startsWith(artistPrefix) ->
                    artist = line.removePrefix(artistPrefix).trim()
            }
        }

        val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date())

        _items.add(
            Item(
                title = title,
                artist = artist,
                timestamp = timestamp,
                mood = mood
            )
        )
    }

    // To Update mood of most recent item
    fun updateLastMood(mood: String) {
        if (_items.isNotEmpty()) {
            val lastIndex = _items.lastIndex
            val last = _items[lastIndex]
            _items[lastIndex] = last.copy(mood = mood)
        }
    }

    fun updateMoodAt(index: Int, mood: String) {
        if (index in _items.indices) {
            val current = _items[index]
            _items[index] = current.copy(mood = mood)
        }
    }

    // Delete a single entry (for the trash icon)
    fun deleteAt(index: Int) {
        if (index in _items.indices) {
            _items.removeAt(index)
        }
    }

    fun clear(context: Context) {
        _items.clear()
        saveToPreferences(context)
    }


    fun ensureLoaded(context: Context) {
        if (isLoaded) return
        loadFromPreferences(context)
        isLoaded = true
    }

    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences("identified_history", Context.MODE_PRIVATE)


        val serialized = _items.joinToString("§§") { item ->
            listOf(
                sanitize(item.title),
                sanitize(item.artist),
                sanitize(item.timestamp),
                item.mood ?: ""
            ).joinToString("||")
        }

        prefs.edit().putString("items", serialized).apply()
    }

    private fun loadFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences("identified_history", Context.MODE_PRIVATE)
        val raw = prefs.getString("items", null) ?: return

        _items.clear()

        raw.split("§§").forEach { entry ->
            if (entry.isBlank()) return@forEach

            val parts = entry.split("||")
            if (parts.size >= 4) {
                val title = parts[0]
                val artist = parts[1]
                val timestamp = parts[2]
                val moodStr = parts[3].ifEmpty { null }

                _items.add(
                    Item(
                        title = title,
                        artist = artist,
                        timestamp = timestamp,
                        mood = moodStr
                    )
                )
            }
        }
    }

    private fun sanitize(value: String): String {
        return value
            .replace("§§", " ")
            .replace("||", " ")
    }
}
