package np.ict.mad.mad25_p03_team03.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.utils.PitchDetector
import np.ict.mad.mad25_p03_team03.utils.SoundGenerator
import kotlin.math.abs


val mimicLevels = listOf(
    MimicLevel("Do", "C4", 261.63),
    MimicLevel("Re", "D4", 293.66),
    MimicLevel("Mi", "E4", 329.63),
    MimicLevel("Fa", "F4", 349.23),
    MimicLevel("Sol", "G4", 392.00),
    MimicLevel("La", "A4", 440.00)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerMimicGameScreen(
    roomId: String,
    onNavigateBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val myId = currentUser?.uid ?: ""
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- 音高识别相关状态 ---
    var currentLevelIndex by remember { mutableStateOf(0) }
    // 循环关卡：如果超过了列表长度，就取模循环
    val currentLevel = mimicLevels[currentLevelIndex % mimicLevels.size]

    var isListening by remember { mutableStateOf(false) }
    var currentPitch by remember { mutableStateOf(0f) }
    var currentNoteName by remember { mutableStateOf("--") }
    var matchProgress by remember { mutableStateOf(0f) }
    val pitchDetector = remember { PitchDetector() }

    // --- PVP 房间状态 ---
    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val player1Id = roomData?.get("player1Id") as? String
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0
    val isPlayer1 = myId == player1Id
    val status = roomData?.get("status") as? String ?: "waiting"

    // 权限
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            try {
                pitchDetector.start { hz, note ->
                    if (isListening) {
                        currentPitch = hz
                        currentNoteName = note
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 清理
    DisposableEffect(Unit) {
        onDispose { pitchDetector.stop() }
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

    // 播放目标声音
    fun playTargetSound() {
        scope.launch {
            // 暂停监听防止干扰
            val wasListening = isListening
            isListening = false

            SoundGenerator.playTone(currentLevel.frequency, 800) // 播放0.8秒

            delay(200)
            if (wasListening) isListening = true // 恢复监听
        }
    }

    // 自动开始监听 (当状态变为 playing 时)
    LaunchedEffect(status) {
        if (status == "playing" && !isListening) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // 🔥 核心判定逻辑
    LaunchedEffect(currentPitch) {
        if (isListening && status == "playing" && currentPitch > 0) {
            val diff = abs(currentPitch - currentLevel.frequency)
            if (diff < 20.0) { // 误差 20Hz
                matchProgress += 0.1f
                if (matchProgress >= 1f) {
                    // 🎉 匹配成功！
                    matchProgress = 0f
                    // 1. 本地切下一关
                    currentLevelIndex++
                    // 2. 播放声音提示
                    playTargetSound()

                    // 3. 🔥 上传推球数据
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0
                        val direction = if (isPlayer1) 1 else -1
                        var newPos = currentPos + direction

                        // 限制
                        if (newPos > 2) newPos = 2
                        if (newPos < -2) newPos = -2

                        val updates = mutableMapOf<String, Any>("ballPosition" to newPos)
                        if (newPos >= 2) { updates["status"] = "finished"; updates["winnerId"] = player1Id ?: "" }
                        if (newPos <= -2) { updates["status"] = "finished"; updates["winnerId"] = "opponent" }

                        transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                    }
                }
            } else {
                if (matchProgress > 0) matchProgress -= 0.02f // 衰减
            }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Fast Mimic Battle") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "playing") {
                // 1. 顶部推球状态 (复用)
                Text("PUSH WITH YOUR VOICE!", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (ballPosition + 2) / 4f },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = if(isPlayer1) Color.Blue else Color.Red
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("YOU", fontWeight = FontWeight.Bold)
                    Text("SLOT 1", fontSize = 10.sp)
                    Text("MID", fontSize = 10.sp)
                    Text("SLOT 1", fontSize = 10.sp)
                    Text("ENEMY", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(30.dp))

                // 2. 当前关卡显示
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Sing this note:", style = MaterialTheme.typography.labelLarge)
                        Text(currentLevel.name, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                        Text("(${currentLevel.targetNote})", style = MaterialTheme.typography.titleMedium)

                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { playTargetSound() }) {
                            Icon(Icons.Default.PlayArrow, null)
                            Text("Play Tone")
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 3. 调音器 UI (Tuner)
                Text("You: $currentNoteName (${currentPitch.toInt()} Hz)", style = MaterialTheme.typography.headlineSmall)
                Box(
                    modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.LightGray, CircleShape)
                ) {
                    // 中心线
                    Box(Modifier.align(Alignment.Center).width(2.dp).fillMaxHeight().background(Color.Black))

                    // 偏差计算
                    val diff = (currentPitch - currentLevel.frequency).coerceIn(-100.0, 100.0)
                    val offsetX = (diff / 100.0) * 150

                    // 指针
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offsetX.dp)
                            .size(24.dp)
                            .background(if (abs(diff) < 20) Color.Green else Color.Red, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 进度条
                Text("Hold steady...", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { matchProgress },
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    color = Color.Green
                )

                Spacer(Modifier.weight(1f))

            } else if (status == "finished") {
                // 结果页面
                val iWon = (ballPosition >= 10 && isPlayer1) || (ballPosition <= -10 && !isPlayer1)
                Spacer(Modifier.height(40.dp))
                Text(if (iWon) "VICTORY! 🎤" else "DEFEAT...", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = if(iWon) Color.Green else Color.Red)
                Spacer(Modifier.height(20.dp))
                Button(onClick = handleExit) { Text("Back to Lobby") }
            } else {
                CircularProgressIndicator()
                Text("Waiting for opponent...")
            }
        }
    }
}