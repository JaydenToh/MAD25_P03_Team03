package np.ict.mad.mad25_p03_team03.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import np.ict.mad.mad25_p03_team03.data.GameMode
import np.ict.mad.mad25_p03_team03.data.SongRepository

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
    var feedbackText by remember { mutableStateOf("") } // Perfect / Miss
    var combo by remember { mutableStateOf(0) }

    // 解析数据
    val player1Id = roomData?.get("player1Id") as? String
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0
    val isPlayer1 = myId == player1Id
    val songUrl = roomData?.get("currentSongUrl") as? String
    val status = roomData?.get("status") as? String ?: "waiting"

    // 🔥 动画核心：0f 到 1f 的循环动画
    // 假设跑完全程需要 2000ms (2秒)
    val infiniteTransition = rememberInfiniteTransition(label = "rhythm")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // 线性移动
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // 退出逻辑
    val handleExit = {
        if (player1Id == myId) db.collection("pvp_rooms").document(roomId).delete()
        else db.collection("pvp_rooms").document(roomId).update("player2Id", null)
        onNavigateBack()
    }
    BackHandler { handleExit() }

    // 监听房间
    LaunchedEffect(roomId) {
        db.collection("pvp_rooms").document(roomId).addSnapshotListener { s, _ ->
            if (s != null && s.exists()) roomData = s.data else onNavigateBack()
        }
    }

    // 房主初始化 BGM (只做一次)
    LaunchedEffect(roomData) {
        if (isPlayer1 && songUrl == null) {
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(1)
            if (songs.isNotEmpty()) {
                db.collection("pvp_rooms").document(roomId).update(
                    mapOf("currentSongUrl" to songs[0].audioUrl, "ballPosition" to 0)
                )
            }
        }
    }

    // 播放音乐 (循环)
    LaunchedEffect(songUrl, status) {
        if (status == "playing" && songUrl != null) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(songUrl)
                isLooping = true // 🔥 循环播放
                prepareAsync()
                setOnPreparedListener { start() }
            }
        }else if (status == "finished") {
            // 🔥 2. 修复：游戏结束时停止播放
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // 🔥 核心判定逻辑
    // 轨道定义：Slot1 - Gap - Slot2 - Gap - Slot3 - Gap - Slot4
    // 我们把 0f-1f 分成 7 份 (4 slots + 3 gaps) 稍微有些复杂，我们用百分比定义
    // Slot 1: 0.05 - 0.20
    // Gap 1: 0.20 - 0.30
    // Slot 2: 0.30 - 0.45
    // Gap 2: 0.45 - 0.55
    // Slot 3: 0.55 - 0.70
    // Gap 3: 0.70 - 0.80
    // Slot 4: 0.80 - 0.95
    fun checkHit(): Boolean {
        val p = progress
        return (p in 0.05..0.20) || (p in 0.30..0.45) || (p in 0.55..0.70) || (p in 0.80..0.95)
    }

    fun onTap() {
        if (status != "playing") return

        var moveAmount = 0

        if (checkHit()) {
            feedbackText = "PERFECT! ⭐"
            combo++
            moveAmount = 1 // 成功，正常推进
        } else {
            feedbackText = "MISS... ❌"
            combo = 0
            moveAmount = -1 // 🔥 1. 修复：失败，反向惩罚 (扣分)
        }

        db.runTransaction { transaction ->
            val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
            val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

            // 计算方向：
            // P1 想要往正方向推 (+1)
            // P2 想要往负方向推 (-1)
            val playerDirection = if (isPlayer1) 1 else -1

            // 最终移动值 = 玩家方向 * 判定结果 (1 或 -1)
            // 例子 P1: Perfect -> 1 * 1 = +1 (前进); Miss -> 1 * -1 = -1 (后退)
            // 例子 P2: Perfect -> -1 * 1 = -1 (前进); Miss -> -1 * -1 = +1 (后退)
            val actualMove = playerDirection * moveAmount

            var newPos = currentPos + actualMove

            // 限制范围
            if (newPos > 10) newPos = 10
            if (newPos < -10) newPos = -10

            val updates = mutableMapOf<String, Any>("ballPosition" to newPos)

            // 判赢
            if (newPos >= 10) { updates["status"] = "finished"; updates["winnerId"] = player1Id ?: "" }
            if (newPos <= -10) { updates["status"] = "finished"; updates["winnerId"] = "opponent" }

            transaction.update(db.collection("pvp_rooms").document(roomId), updates)

        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Rhythm Master") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "playing") {
                // 1. 顶部推球状态
                Text("PUSH THE BALL!", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (ballPosition + 10) / 20f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).networkDropShadow(),
                    color = if(isPlayer1) Color.Blue else Color.Red
                )

                Spacer(Modifier.height(40.dp))

                // 2. 🔥 视觉轨道区域
                Text("Tap when note hits the slot!", color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    // 绘制 4 个 Slots (背景框)
                    // 位置必须和 checkHit 里的百分比对应
                    // Slot 1: 0.05 - 0.20 (Center ~ 0.125, Width ~ 0.15)
                    // 我们用 Row + Weights 来布局会更容易对齐

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.weight(0.05f)) // Start Padding

                        // Slot 1
                        SlotBox(Modifier.weight(0.15f))
                        Spacer(Modifier.weight(0.10f)) // Gap

                        // Slot 2
                        SlotBox(Modifier.weight(0.15f))
                        Spacer(Modifier.weight(0.10f)) // Gap

                        // Slot 3
                        SlotBox(Modifier.weight(0.15f))
                        Spacer(Modifier.weight(0.10f)) // Gap

                        // Slot 4
                        SlotBox(Modifier.weight(0.15f))

                        Spacer(Modifier.weight(0.05f)) // End Padding
                    }

                    // 🔥 移动的音符 (Note)
                    // 使用 BiasAlignment 或者 Offset
                    // progress 0f -> 1f maps to horizontalBias -1f -> 1f
                    val bias = (progress * 2) - 1

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth() // 需要填满宽度才能用 Bias 定位
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Note",
                            tint = Color.Magenta,
                            modifier = Modifier
                                .align(BiasAlignment(bias, 0f)) // 垂直居中，水平动态
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color.Magenta, CircleShape)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // 3. 点击按钮区域
                Text(feedbackText, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if(feedbackText.contains("MISS")) Color.Gray else Color.Green)
                if (combo > 1) Text("$combo Combo!", fontSize = 24.sp, color = Color.Yellow)

                Spacer(Modifier.weight(1f))

                // 大按钮
                Button(
                    onClick = { onTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("TAP HERE!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(20.dp))

            } else if (status == "finished") {
                // 结果页面
                Spacer(Modifier.height(40.dp))

                // 判断胜负
                val iWon = (ballPosition >= 10 && isPlayer1) || (ballPosition <= -10 && !isPlayer1)

                Text(if (iWon) "YOU WON! 🏆" else "YOU LOST 💀", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = if(iWon) Color.Green else Color.Red)

                Spacer(Modifier.height(20.dp))
                Button(onClick = handleExit) { Text("Back to Lobby") }
            } else {
                CircularProgressIndicator()
                Text("Waiting for opponent...")
            }
        }
    }
}

@Composable
fun SlotBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight(0.8f) // 稍微小一点
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 可以加个小圆点或者 "Hit" 文字
        Box(Modifier.size(8.dp).background(Color.Gray.copy(alpha = 0.5f), CircleShape))
    }
}

// 辅助扩展函数
fun Modifier.networkDropShadow() = this // 占位符