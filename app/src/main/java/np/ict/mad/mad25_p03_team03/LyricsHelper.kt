package np.ict.mad.mad25_p03_team03

data class LyricLine(
    val timestamp: Long,
    val content: String
)

object LyricsHelper {
    // Parse the stored firebase Lyrics here
    fun parseLyrics(rawLyrics: String): List<LyricLine> {
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2})] (.*)")
        return rawLyrics.lines().mapNotNull { line ->
            val match = regex.find(line)
            if (match != null) {
                val (min, sec, ms, text) = match.destructured
                val time = (min.toLong() * 60 * 1000) + (sec.toLong() * 1000) + (ms.toLong() * 10)
                LyricLine(time, text)
            } else null
        }
    }
}