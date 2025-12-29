package np.ict.mad.mad25_p03_team03.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.utils.PitchDetector
import np.ict.mad.mad25_p03_team03.utils.SoundGenerator
import kotlin.math.abs

// 定义关卡数据 (音名, 频率)
data class MimicLevel(val name: String, val targetNote: String, val frequency: Double)

val levels = listOf(
    MimicLevel("Level 1", "C4 (Do)", 261.63),
    MimicLevel("Level 2", "E4 (Mi)", 329.63),
    MimicLevel("Level 3", "G4 (Sol)", 392.00),
    MimicLevel("Level 4", "A4 (La)", 440.00)
)

@Composable
fun MimicGameScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态
    var currentLevelIndex by remember { mutableStateOf(0) }
    val currentLevel = levels[currentLevelIndex]

    var isListening by remember { mutableStateOf(false) }
    var currentPitch by remember { mutableStateOf(0f) }      // 当前哼的 Hz
    var currentNoteName by remember { mutableStateOf("--") } // 当前哼的音名
    var matchProgress by remember { mutableStateOf(0f) }     // 匹配进度 (0..1)

    val pitchDetector = remember { PitchDetector() }

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 开始监听
            isListening = true
            pitchDetector.start { hz, note ->
                if (isListening) {
                    currentPitch = hz
                    currentNoteName = note
                }
            }
        } else {
            Toast.makeText(context, "Mic permission needed!", Toast.LENGTH_SHORT).show()
        }
    }

    // 播放示例声音
    fun playTargetSound() {
        scope.launch {
            isListening = false // 播放时暂停监听，防止自己听到自己
            pitchDetector.stop()

            Toast.makeText(context, "Listen...", Toast.LENGTH_SHORT).show()
            val targetFreq = levels[currentLevelIndex].frequency

            SoundGenerator.playTone(targetFreq, 1000)

            delay(500)
            if (!isListening) {
                // ... (保持原有的权限检查逻辑)
                // 如果你有权限检查逻辑，确保这里也能正确重启
                // 简单起见，这里可以直接 pitchDetector.start(...)
                // 或者调用 permissionLauncher (但这会导致弹窗)
                // 最好的方式是直接重启监听：
                try {
                    pitchDetector.start { hz, note ->
                        currentPitch = hz
                        currentNoteName = note
                    }
                    isListening = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 判定逻辑 (LaunchedEffect 监听 currentPitch)
    LaunchedEffect(currentPitch) {
        if (isListening && currentPitch > 0) {
            // 允许误差范围 +/- 15Hz (比较宽松)
            val diff = abs(currentPitch - currentLevel.frequency)

            if (diff < 20.0) {
                matchProgress += 0.05f
                if (matchProgress >= 1f) {
                    // 过关！
                    matchProgress = 0f
                    isListening = false // 立即停止接收新的判定，防止重复触发

                    // 🔥 核心修复：使用 scope.launch 启动一个独立的协程来处理跳转
                    // 这样即使 LaunchedEffect 被取消，这个跳转逻辑也会继续执行
                    scope.launch {
                        Toast.makeText(context, "Perfect! Next Level!", Toast.LENGTH_SHORT).show()
                        delay(1000) // 这里等待很安全，不会被打断

                        if (currentLevelIndex < levels.size - 1) {
                            currentLevelIndex++
                            // 自动播放下一关
                            playTargetSound()
                        } else {
                            Toast.makeText(context, "You Finished All Levels!", Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        }
                    }
                }
            } else {
                if (matchProgress > 0) matchProgress -= 0.02f
            }
        }
    }

    // 页面销毁时停止录音
    DisposableEffect(Unit) {
        onDispose {
            pitchDetector.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部信息
        Text("Humming Challenge", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(currentLevel.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(32.dp))

        // 目标显示
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Target Note", style = MaterialTheme.typography.labelLarge)
                Text(currentLevel.targetNote, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))

                Button(onClick = { playTargetSound() }) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play Tone")
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // 仪表盘 (Tuner UI)
        Text("You represent:", style = MaterialTheme.typography.labelMedium)
        Text("$currentNoteName (${currentPitch.toInt()} Hz)", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        // 简单的可视化条：左边低，右边高，中间准
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray, CircleShape)
        ) {
            // 中心标记
            Box(Modifier.align(Alignment.Center).width(4.dp).fillMaxHeight().background(Color.Black))

            // 计算偏差偏移量
            // 假设范围是 +/- 100Hz
            val diff = (currentPitch - currentLevel.frequency).coerceIn(-100.0, 100.0)
            val offsetX = (diff / 100.0) * 150 // 映射到像素偏移 (假设宽度300左右)

            // 指针
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = offsetX.dp)
                    .size(20.dp)
                    .background(
                        if (abs(diff) < 15) Color.Green else Color.Red,
                        CircleShape
                    )
            )
        }
        Text("Low  < --- >  High", modifier = Modifier.padding(top=8.dp), color = Color.Gray)

        Spacer(Modifier.weight(1f))

        // 匹配进度条
        Text("Holding Logic...", style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(
            progress = { matchProgress },
            modifier = Modifier.fillMaxWidth().height(20.dp),
            color = Color.Green,
            trackColor = Color.LightGray
        )

        Spacer(Modifier.height(24.dp))

        // 第一次启动按钮
        if (!isListening) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Mic, null)
                Spacer(Modifier.width(8.dp))
                Text("Start Microphone")
            }
        } else {
            OutlinedButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quit")
            }
        }
    }
}