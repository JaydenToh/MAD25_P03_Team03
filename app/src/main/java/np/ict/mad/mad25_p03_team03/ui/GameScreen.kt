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
fun GameScreen(songRepository: SongRepository) { // ✅ 接收 Repository

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ 可变 questions：初始为空，加载后更新
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 保留你原有的游戏状态
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var message by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(10) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val currentQuestion = questions.getOrNull(currentIndex)

    // ✅ 加载 Supabase 数据（首次进入时）
    LaunchedEffect(Unit) {
        isLoading = true
        println("🔍 DEBUG: Launching Supabase fetch...") // Debug log

        val remoteSongs = songRepository.fetchSongsFromSupabase()
        println("🔍 DEBUG: Fetched ${remoteSongs.size} songs") // Debug log
        if (remoteSongs.isNotEmpty()) {
            questions = remoteSongs.map { songDto ->
                val options = listOf(songDto.title) + songDto.fakeOptions
                SongQuestion(
                    correctTitle = songDto.title,
                    options = options.shuffled().take(4),
                    audioUrl = songDto.audioUrl
                )
            }
        } else {
            println("⚠️ DEBUG: Supabase returned empty — using fallback")
            // ✅ fallback：Supabase 无数据时用本地测试（避免白屏）
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

    // ✅ 计时器 + 下一题逻辑（你原有的，完全保留）
    LaunchedEffect(currentIndex) {
        if (currentIndex >= questions.size) return@LaunchedEffect
        timeLeft = 10
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                lives -= 1
                message = "Time's up!"
                if (lives > 0 && currentIndex < questions.lastIndex) {
                    currentIndex += 1
                }
            }
        }.start()
    }

    // ✅ 释放 MediaPlayer（你原有的）
    LaunchedEffect(currentIndex) {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    // ✅ 播放网络音频
    fun playAudio(url: String?) {
        if (url == null) return

        mediaPlayer?.apply {
            stop()
            release()
        }

        try {
            val mp = MediaPlayer().apply {
                setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                setDataSource(url)
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
                prepareAsync() // 异步准备，避免 ANR
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Audio load failed"
        }
    }

    // ✅ UI 主体
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
            Text("Loading songs from Supabase...")
        } else if (currentQuestion != null) {
            // 状态栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Score: $score", style = MaterialTheme.typography.bodyLarge)
                Text("Lives: $lives", style = MaterialTheme.typography.bodyLarge)
                Text("Time: $timeLeft", style = MaterialTheme.typography.bodyLarge)
            }

            // 播放按钮
            Button(
                onClick = { playAudio(currentQuestion.audioUrl) },
                enabled = currentQuestion.audioUrl != null
            ) {
                Text("▶️ Play Song Clip")
            }

            // 选项按钮
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

                        if (lives > 0 && currentIndex < questions.lastIndex) {
                            currentIndex += 1
                        }
                    }
                ) {
                    Text(option)
                }
            }

            // 提示消息
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (message.contains("Correct")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 游戏结束
            if (lives <= 0 || currentIndex >= questions.lastIndex) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "🎉 Game Over!\nFinal Score: $score",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // 重置游戏（可选）
                    currentIndex = 0
                    score = 0
                    lives = 3
                    message = ""
                }) {
                    Text("↺ Play Again")
                }
            }
        } else {
            Text("No songs available. Check your Supabase table.")
        }
    }
}

// ✅ SongQuestion data class（支持网络 URL）
data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioUrl: String? = null
)