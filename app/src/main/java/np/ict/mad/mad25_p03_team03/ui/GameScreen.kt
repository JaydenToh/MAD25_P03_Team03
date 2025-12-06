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
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.data.remote.dto.SongDto

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

    // ✅ 1. 将 playAudio 移到这里 (LaunchedEffect 之前)，以便它们可以调用它
    fun playAudio(url: String?) {
        val cleanUrl = url?.trim() ?: return
        if (cleanUrl.isEmpty()) return

        // 先清理旧的播放器
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }

        try {
            val mp = MediaPlayer().apply {
                setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                setDataSource(cleanUrl)
                // 准备好后自动播放
                setOnPreparedListener {
                    it.start()
                    println("🎵 Auto-playing: $cleanUrl")
                }
                setOnCompletionListener { release(); mediaPlayer = null }
                setOnErrorListener { _, _, _ ->
                    release(); mediaPlayer = null; true
                }
                prepareAsync() // 异步准备，不卡顿 UI
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            // message = "Audio error" // 可以选择不显示错误以免打扰用户
            mediaPlayer = null
        }
    }

    // 加载数据的 Effect
    LaunchedEffect(Unit) {
        isLoading = true
        val remoteSongs = songRepository.fetchSongsFromSupabase()
        if (remoteSongs.isNotEmpty()) {
            questions = remoteSongs.map { songDto ->
                val options = (listOf(songDto.title) + songDto.fakeOptions).shuffled().take(4)
                SongQuestion(songDto.title, options, songDto.audioUrl)
            }
        } else {
            // Fallback data
            questions = listOf(
                SongQuestion("Blinding Lights", listOf("Blinding Lights", "Save Your Tears", "Levitating", "Peaches"), "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
                SongQuestion("Bohemian Rhapsody", listOf("Bohemian Rhapsody", "Stairway to Heaven", "Hotel California", "Imagine"), "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
            )
        }
        isLoading = false
    }

    // ✅ 2. 核心修改：当 currentIndex 改变（换题）或 isLoading 结束时，自动播放
    LaunchedEffect(currentIndex, isLoading) {
        if (!isLoading && questions.isNotEmpty() && currentIndex < questions.size) {
            // 每次换题，重置时间
            timeLeft = 10

            // 自动播放当前歌曲
            val urlToPlay = questions[currentIndex].audioUrl
            playAudio(urlToPlay)

            // 启动倒计时
            object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = (millisUntilFinished / 1000).toInt()
                }
                override fun onFinish() {
                    // 只有在还是当前题目时才扣分（防止用户已经点下一题了倒计时才结束）
                    if (lives > 0 && currentIndex < questions.size) {
                        lives -= 1
                        message = "⏰ Time's up!"
                        if (lives > 0 && currentIndex < questions.size - 1) {
                            currentIndex += 1
                        }
                    }
                }
            }.start()
        }
    }

    // 清理资源的 Effect (当组件销毁时)
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // ✅ UI 部分
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

            // 这里的按钮不动，用户想重听时可以手动点
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

            // 游戏结束/通关逻辑
            if (lives <= 0 || currentIndex >= questions.size - 1 && lives > 0 && message.contains("Correct")) {
                // 注意：这里的逻辑可能需要根据你具体想要何时显示“结束画面”微调
                // 比如你是想答完最后一题马上结束，还是等最后一题判定完
            }

            // 为了简单演示，如果 lives 没了，显示 Reset
            if (lives <= 0) {
                Button(onClick = {
                    currentIndex = 0
                    score = 0
                    lives = 3
                    message = ""
                    // 重置会自动触发 LaunchedEffect 里的 playAudio
                }) {
                    Text("Game Over - Restart")
                }
            }
        }
    }
}

data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioUrl: String?
)