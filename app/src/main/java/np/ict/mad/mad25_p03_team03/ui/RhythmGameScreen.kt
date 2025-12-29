package np.ict.mad.mad25_p03_team03.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import np.ict.mad.mad25_p03_team03.data.GameMode
import np.ict.mad.mad25_p03_team03.data.SongRepository
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhythmGameScreen(
    roomId: String,
    songRepository: SongRepository,
    onNavigateBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val myId = currentUser?.uid ?: ""
    val context = LocalContext.current

    // 状态
    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var feedbackText by remember { mutableStateOf("") } // 显示 "Perfect!", "Miss"
    var combo by remember { mutableStateOf(0) }

    // 节奏控制 (BPM 120 = 500ms)
    val bpm = 120
    val beatInterval = 60000 / bpm // 500ms
    var lastBeatTime by remember { mutableStateOf(0L) }

    // 动画状态 (让中间的大按钮根据节奏缩放)
    val infiniteTransition = rememberInfiniteTransition(label = "beat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f, // 稍微放大一点
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = beatInterval / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beat_scale"
    )

    // 数据解析
    val player1Id = roomData?.get("player1Id") as? String
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0
    val isPlayer1 = myId == player1Id
    val songUrl = roomData?.get("currentSongUrl") as? String
    val status = roomData?.get("status") as? String ?: "waiting"

    // 释放音频
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // 退出逻辑 (简易版)
    val handleExit = {
        if (player1Id == myId) db.collection("pvp_rooms").document(roomId).delete()
        else db.collection("pvp_rooms").document(roomId).update("player2Id", null)
        onNavigateBack()
    }
    BackHandler { handleExit() }

    // 1. 监听房间
    LaunchedEffect(roomId) {
        db.collection("pvp_rooms").document(roomId).addSnapshotListener { s, _ ->
            if (s != null && s.exists()) {
                roomData = s.data
            } else {
                onNavigateBack()
            }
        }
    }

    // 2. 房主初始化歌曲 (只要一首 BGM)
    LaunchedEffect(roomData) {
        if (isPlayer1 && songUrl == null) {
            // 随便抓一首歌当 BGM
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(1)
            if (songs.isNotEmpty()) {
                db.collection("pvp_rooms").document(roomId).update(
                    mapOf("currentSongUrl" to songs[0].audioUrl, "ballPosition" to 0)
                )
            }
        }
    }

    // 3. 播放音乐 & 节奏计时器
    LaunchedEffect(songUrl, status) {
        if (status == "playing" && songUrl != null) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(songUrl)
                isLooping = true // 循环播放直到分胜负
                prepareAsync()
                setOnPreparedListener {
                    start()
                    // 记录开始时间，用于对齐节拍
                    lastBeatTime = System.currentTimeMillis()
                }
            }

            // 启动一个循环来刷新“最近的节拍时间”，用于判定
            while (isActive) {
                lastBeatTime = System.currentTimeMillis()
                delay(beatInterval.toLong())
            }
        }
    }

    // 🔥 核心：点击判定逻辑
    fun onTap() {
        if (status != "playing") return

        val currentTime = System.currentTimeMillis()
        // 计算离刚才那个 Beat 过了多久，或者离下一个 Beat 还有多久
        // 简单算法：找最近的 500ms 倍数
        val timeSinceLastBeat = currentTime - lastBeatTime
        // 允许误差：+/- 150ms

        // 注意：因为上面协程更新 lastBeatTime 可能有误差，更精准的做法是:
        // diff = min(timeSinceLastBeat, beatInterval - timeSinceLastBeat)
        // 但为了作业简单，我们直接判断视觉：

        // 实际上，更简单的“体感”是：点下去的时候，scale 是大还是小？
        // 或者直接写死：如果在协程 delay 的前 100ms 或后 100ms 点算准。

        // 这里用简易时间差：
        // 假设 beatInterval = 500.
        // 0ms (Beat) --- 250ms (Offbeat) --- 500ms (Next Beat)
        val diff = if (timeSinceLastBeat < beatInterval / 2) timeSinceLastBeat else (beatInterval - timeSinceLastBeat)

        var pushAmount = 0
        if (diff < 150) { // 150ms 内算 Perfect
            feedbackText = "PERFECT!! 🔥"
            combo++
            pushAmount = 2 // 甚至可以加 Combo 加成
        } else if (diff < 250) {
            feedbackText = "Good!"
            combo = 0
            pushAmount = 1
        } else {
            feedbackText = "Miss..."
            combo = 0
            pushAmount = -1 // 惩罚：反向退一点，或者 0
        }

        // 更新数据库 (推球)
        if (pushAmount != 0) {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                // P1 往正推，P2 往负推 (这里需要和之前 Trivia 逻辑保持一致)
                // 假设之前是：P1 答对 +1 (向右), P2 答对 -1 (向左)
                val direction = if (isPlayer1) 1 else -1
                var newPos = currentPos + (pushAmount * direction)

                // 限制
                if (newPos > 10) newPos = 10 // 节奏模式轨道可以长一点，设为 10
                if (newPos < -10) newPos = -10

                // 判赢
                val updates = mutableMapOf<String, Any>("ballPosition" to newPos)
                if (newPos >= 10) { updates["status"] = "finished"; updates["winnerId"] = player1Id ?: "" }
                if (newPos <= -10) { updates["status"] = "finished"; updates["winnerId"] = "opponent" } // 简化

                transaction.update(db.collection("pvp_rooms").document(roomId), updates)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Rhythm Battle") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "playing") {
                // 1. 轨道 (重用你的 BallTrackUI，但记得把 range 改大一点，比如 -10 到 10)
                // 这里暂时用简单的 Text 代替轨道演示
                Text("Ball Position: $ballPosition", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { (ballPosition + 10) / 20f }, // Map -10..10 to 0..1
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("YOU (P1)")
                    Text("ENEMY (P2)")
                }

                Spacer(Modifier.weight(1f))

                // 2. 反馈文字
                Text(feedbackText, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if(feedbackText.contains("Miss")) Color.Gray else Color.Magenta)
                if (combo > 1) Text("Combo x$combo", fontSize = 20.sp, color = Color.Yellow)

                Spacer(Modifier.height(30.dp))

                // 3. 核心玩法：大按钮
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale) // 跟随节奏缩放
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // 去掉点击波纹，为了反应更快
                        ) { onTap() }
                ) {
                    Text("TAP!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.weight(1f))
            } else if (status == "finished") {
                Text("GAME OVER")
                Button(onClick = handleExit) { Text("Back") }
            } else {
                Text("Waiting for opponent...")
                CircularProgressIndicator()
            }
        }
    }
}