// np/ict.mad.mad25_p03_team03/ui/GameScreen.kt

package np.ict.mad.mad25_p03_team03.ui

import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.ict.mad.mad25_p03_team03.data.SongRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    songRepository: SongRepository,
    onNavigateBack: () -> Unit  // 👈 新增回调：返回上一页
) {
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

    // ✅ 播放函数（同前）
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
                setOnPreparedListener { it.start() }
                setOnCompletionListener { release(); mediaPlayer = null }
                setOnErrorListener { _, _, _ -> release(); mediaPlayer = null; true }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer = null
        }
    }

    // 加载数据
    LaunchedEffect(Unit) {
        isLoading = true
        val remoteSongs = songRepository.fetchSongsFromSupabase()
        questions = if (remoteSongs.isNotEmpty()) {
            remoteSongs.map { songDto ->
                val options = (listOf(songDto.title) + songDto.fakeOptions).shuffled().take(4)
                SongQuestion(songDto.title, options, songDto.audioUrl)
            }
        } else {
            listOf(
                SongQuestion("Blinding Lights", listOf("Blinding Lights", "Save Your Tears", "Levitating", "Peaches"), "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
                SongQuestion("Bohemian Rhapsody", listOf("Bohemian Rhapsody", "Stairway to Heaven", "Hotel California", "Imagine"), "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
            )
        }
        isLoading = false
    }

    // 换题时自动播放 + 计时
    LaunchedEffect(currentIndex, isLoading) {
        if (!isLoading && currentIndex < questions.size) {
            timeLeft = 10
            playAudio(questions[currentIndex].audioUrl)

            object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (lives > 0 && currentIndex < questions.size) {
                        timeLeft = (millisUntilFinished / 1000).toInt()
                    }
                }
                override fun onFinish() {
                    if (lives > 0 && currentIndex < questions.size) {
                        lives -= 1
                        message = "⏰ Time's up!"
                        if (lives > 0 && currentIndex < questions.lastIndex) {
                            currentIndex += 1
                        }
                    }
                }
            }.start()
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // 👇 关键：用 Scaffold 包裹，自动避开状态栏
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("🎵 Song Guesser", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Rules"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                    Text("Loading songs...")
                } else if (currentQuestion != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: $score", style = MaterialTheme.typography.bodyLarge)
                        Text("Lives: $lives", style = MaterialTheme.typography.bodyLarge)
                        Text("Time: $timeLeft", style = MaterialTheme.typography.bodyLarge)
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
                                if (lives > 0 && currentIndex < questions.lastIndex) {
                                    currentIndex += 1
                                }
                            }
                        ) {
                            Text(option)
                        }
                    }

                    if (message.isNotEmpty()) {
                        Text(
                            message,
                            color = if (message.contains("Correct")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }

                    // ✅ 游戏结局页面（成功 or 失败）
                    val isGameOver = lives <= 0
                    val isSuccess = !isGameOver && currentIndex >= questions.lastIndex && message.isNotBlank()

                    if (isGameOver || isSuccess) {
                        Spacer(Modifier.weight(1f))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isSuccess) "🎉 Success!" else "😢 Game Over",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            Text("Final Score: $score", style = MaterialTheme.typography.titleLarge)

                            if (isGameOver) {
                                Spacer(Modifier.height(24.dp))
                                Button(onClick = {
                                    currentIndex = 0
                                    score = 0
                                    lives = 3
                                    message = ""
                                }) {
                                    Text("↺ Restart")
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    )
}

data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioUrl: String?
)