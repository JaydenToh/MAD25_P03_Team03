package np.ict.mad.mad25_p03_team03

data class LyricLine(
    val timestamp: Long,
    val content: String
)

object LyricsHelper {
    fun parseLyrics(rawLyrics: String): List<LyricLine> {
        val parsedLines = mutableListOf<LyricLine>()

        // Split raw text into lines
        val lines = rawLyrics.trim().split("\n")
        // Pattern for [MM:SS.xx]
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\]")

        for (line in lines) {
            val match = regex.find(line)
            if (match != null) {
                // Extract time parts safely
                val (min, sec, ms) = match.destructured
                val timeMillis = (min.toLong() * 60 * 1000) +
                        (sec.toLong() * 1000) +
                        (ms.toLong() * 10)

                // Get the text part (remove the time)
                val text = line.replace(match.value, "").trim()

                if (text.isNotEmpty()) {
                    parsedLines.add(LyricLine(timeMillis, text))
                }
            }
        }
        return parsedLines
    }
}