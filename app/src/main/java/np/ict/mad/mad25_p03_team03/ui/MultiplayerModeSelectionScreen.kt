package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_p03_team03.data.GameMode

enum class GameType {
    TRIVIA, RHYTHM, MIMIC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerModeSelectionScreen(
    onCreateRoom: (GameMode, GameType, Boolean) -> Unit,
    onBack: () -> Unit
) {

    var selectedMode by remember { mutableStateOf(GameMode.ENGLISH) }
    var selectedType by remember { mutableStateOf(GameType.TRIVIA)}

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Room", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Song Language",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))


            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = selectedMode == GameMode.ENGLISH,
                    onClick = { selectedMode = GameMode.ENGLISH },
                    label = { Text("🇺🇸 English") },
                    modifier = Modifier.padding(end = 12.dp)
                )
                FilterChip(
                    selected = selectedMode == GameMode.MANDARIN,
                    onClick = { selectedMode = GameMode.MANDARIN },
                    label = { Text("🇨🇳 Mandarin") }
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("2. Select Game Type", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Trivia Card
                GameTypeCard(
                    title = "Trivia Quiz",
                    icon = Icons.Default.QuestionMark,
                    isSelected = selectedType == GameType.TRIVIA,
                    onClick = { selectedType = GameType.TRIVIA }
                )
                // Rhythm Card
                GameTypeCard(
                    title = "Rhythm Tap",
                    icon = Icons.Default.MusicNote,
                    isSelected = selectedType == GameType.RHYTHM,
                    onClick = { selectedType = GameType.RHYTHM }
                )
                GameTypeCard(
                    title = "Mimic",
                    icon = Icons.Default.Mic,
                    isSelected = selectedType == GameType.MIMIC,
                    onClick = { selectedType = GameType.MIMIC }
                )
            }

            Spacer(Modifier.weight(1f))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Create Room (PVP)
                Button(
                    onClick = { onCreateRoom(selectedMode, selectedType, false) }, // isBot = false
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("CREATE PVP")
                }

                // Play vs Bot
                Button(
                    onClick = { onCreateRoom(selectedMode, selectedType, true) }, // isBot = true
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("VS BOT 🤖")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RowScope.GameTypeCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.weight(1f).height(100.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null)
            Text(title, fontWeight = FontWeight.Bold)
        }
    }
}