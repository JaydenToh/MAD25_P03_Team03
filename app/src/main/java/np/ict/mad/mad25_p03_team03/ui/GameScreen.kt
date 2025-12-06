// np/ict/mad/mad25_p03_team03/ui/GameScreen.kt

package np.ict.mad.mad25_p03_team03.ui

import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.data.SongRepository // 👈 你的 Repository
import np.ict.mad.mad25_p03_team03.data.remote.dto.SongDto // 👈 你的 DTO

@Composable
fun GameScreen(songRepository: SongRepository) {

    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var message by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(10) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val currentQuestion = questions.getOrNull(currentIndex)

    LaunchedEffect(Unit) {
        isLoading = true
        println("🔍 DEBUG: Launching Supabase fetch...")
        val remoteSongs = songRepository.fetchSongsFromSupabase()
        println("🔍 DEBUG: Fetched ${remoteSongs.size} songs")
        if (remoteSongs.isNotEmpty()) {
            questions = remoteSongs.map { songDto ->
                val options = (listOf(songDto.title) + songDto.fakeOptions).shuffled().take(4)
                SongQuestion(songDto.title, options, songDto.audioUrl)
            }
        } else {
            println("⚠️ DEBUG: Using fallback data")
            questions = listOf(
                SongQuestion(
                    correctTitle = "Blinding Lights",
                    options = listOf("Blinding Lights", "Save Your Tears", "Levitating", "Peaches"),
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                ),
                SongQuestion(
                    correctTitle = "Bohemian Rhapsody",
                    options = listOf("Bohemian Rhapsody", "Stairway to Heaven", "Hotel California", "Imagine"),
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                )
            )
        }
        isLoading = false
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= questions.size) return@LaunchedEffect
        timeLeft = 10
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                lives -= 1
                message = "⏰ Time's up!"
                if (lives > 0 && currentIndex < questions.size - 1) {
                    currentIndex += 1
                }
            }
        }.start()
    }

    LaunchedEffect(currentIndex) {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    // ✅ 修复版 playAudio
    fun playAudio(url: String?) {
        val cleanUrl = url?.trim() ?: return
        if (cleanUrl.isEmpty()) return

        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }

        try {
            val mp = MediaPlayer().apply {
                setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                setDataSource(cleanUrl)
                setOnPreparedListener { if (!isPlaying) start() }
                setOnCompletionListener { release(); mediaPlayer = null }
                setOnErrorListener { _, _, _ ->
                    release(); mediaPlayer = null; true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Audio error"
            mediaPlayer = null
        }
    }

    // ✅ UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎵 Song Guesser", style = MaterialTheme.typography.headlineMedium)

        if (isLoading) {
            CircularProgressIndicator()
            Text("Loading...")
        } else if (currentQuestion != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Score: $score")
                Text("Lives: $lives")
                Text("Time: $timeLeft")
            }

            Button(onClick = { playAudio(currentQuestion.audioUrl) }) {
                Text("▶️ Play Song Clip")
            }

            currentQuestion.options.forEach { option ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (option == currentQuestion.correctTitle) {
                            score += 10
                            message = "✅ Correct!"
                        } else {
                            lives -= 1
                            message = "❌ Wrong!"
                        }
                        // 仅当还有 lives 且没答完题，才进下一题
                        if (lives > 0 && currentIndex < questions.size - 1) {
                            currentIndex += 1
                        }
                    }
                ) {
                    Text(option)
                }
            }

            if (message.isNotEmpty()) {
                Text(message, color = if (message.contains("Correct")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }

            // ✅ 关键修复：用 .size 而非 .lastIndex
            if (lives <= 0 || currentIndex >= questions.size) {
                Spacer(Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🎉 Game Over!\nFinal Score: $score",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            currentIndex = 0
                            score = 0
                            lives = 3
                            message = ""
                        },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("↺ Play Again")
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioUrl: String?
)