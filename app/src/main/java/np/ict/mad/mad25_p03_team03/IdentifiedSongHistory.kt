package np.ict.mad.mad25_p03_team03

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.content.edit


object IdentifiedSongHistory {

    data class Item(
        val title: String,
        val artist: String,
        var mood: String?,
        var timestamp: String
    )

    val items: SnapshotStateList<Item> = mutableStateListOf()

    private const val PREF_NAME = "identified_song_history"
    private const val KEY_HISTORY = "history_json"

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")


    fun ensureLoaded(context: Context) {
        if (items.isNotEmpty()) return

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return

        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                items.add(
                    Item(
                        title = obj.optString("title"),
                        artist = obj.optString("artist"),
                        mood = if (obj.isNull("mood")) null else obj.optString("mood"),
                        timestamp = obj.optString("timestamp")
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    fun saveToPreferences(context: Context) {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("title", item.title)
                put("artist", item.artist)
                put("mood", item.mood)
                put("timestamp", item.timestamp)
            }
            arr.put(obj)
        }

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_HISTORY, arr.toString())
        }
    }


    fun addFromSongText(songText: String, selectedMood: String?) {
        if (songText.isBlank()) return

        val lines = songText.split('\n')

        var title: String = songText
        var artist = "Unknown Artist"

        lines.forEach { line ->
            when {
                line.startsWith("Song:", ignoreCase = true) ->
                    title = line.removePrefix("Song:").trim()

                line.startsWith("Title:", ignoreCase = true) ->
                    title = line.removePrefix("Title:").trim()

                line.startsWith("Artist:", ignoreCase = true) ->
                    artist = line.removePrefix("Artist:").trim()
            }
        }

        val now = LocalDateTime.now().format(formatter)

        val existingIndex = items.indexOfFirst {
            it.title.equals(title, ignoreCase = true) &&
                    it.artist.equals(artist, ignoreCase = true)
        }

        if (existingIndex >= 0) {
            // Update existing entry this keep old mood if new one is null
            val existing = items[existingIndex]
            items.removeAt(existingIndex)
            items.add(
                existing.copy(
                    mood = existing.mood,
                    timestamp = now
                )
            )
        } else {
            // New song will append to history
            items.add(
                Item(
                    title = title,
                    artist = artist,
                    mood = selectedMood,
                    timestamp = now
                )
            )
        }
    }


    fun updateLastMood(mood: String) {
        if (items.isEmpty()) return
        items[items.lastIndex].mood = mood
    }

    fun updateMoodAt(index: Int, mood: String) {
        if (index < 0 || index >= items.size) return
        items[index].mood = mood
    }

    fun deleteAt(index: Int) {
        if (index < 0 || index >= items.size) return
        items.removeAt(index)
    }
}
