package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_p03_team03.data.Difficulty
import np.ict.mad.mad25_p03_team03.data.GameMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    onStartGame: (GameMode, Difficulty) -> Unit, // 修改回调，回传两个参数
    onBack: () -> Unit
) {
    // 保存用户的选择状态，默认 英文 + 简单
    var selectedMode by remember { mutableStateOf(GameMode.ENGLISH) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. 语言选择区域 ---
            Text(
                text = "Select Language",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            // 使用 Segmented Button 或简单的 RadioButton 列表
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = selectedMode == GameMode.ENGLISH,
                    onClick = { selectedMode = GameMode.ENGLISH },
                    label = { Text("🇺🇸 English") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = selectedMode == GameMode.MANDARIN,
                    onClick = { selectedMode = GameMode.MANDARIN },
                    label = { Text("🇨🇳 Mandarin") }
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- 2. 难度选择区域 (在下面) ---
            Text(
                text = "Select Difficulty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(16.dp))

            // 难度选项卡片
            Column(modifier = Modifier.selectableGroup()) {
                DifficultyOption(
                    difficulty = Difficulty.EASY,
                    isSelected = selectedDifficulty == Difficulty.EASY,
                    onSelect = { selectedDifficulty = Difficulty.EASY }
                )
                DifficultyOption(
                    difficulty = Difficulty.MEDIUM,
                    isSelected = selectedDifficulty == Difficulty.MEDIUM,
                    onSelect = { selectedDifficulty = Difficulty.MEDIUM }
                )
                DifficultyOption(
                    difficulty = Difficulty.HARD,
                    isSelected = selectedDifficulty == Difficulty.HARD,
                    onSelect = { selectedDifficulty = Difficulty.HARD }
                )
            }

            Spacer(Modifier.weight(1f))

            // --- 3. 开始游戏按钮 ---
            Button(
                onClick = { onStartGame(selectedMode, selectedDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("🚀 START GAME", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// 辅助组件：显示单个难度选项
@Composable
fun DifficultyOption(
    difficulty: Difficulty,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = null)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = difficulty.name, // 显示 EASY, MEDIUM, HARD
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${difficulty.timeLimitSeconds} seconds per song",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}