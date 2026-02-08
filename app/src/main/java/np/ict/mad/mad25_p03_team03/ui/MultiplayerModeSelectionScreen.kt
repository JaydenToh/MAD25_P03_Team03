package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_p03_team03.data.GameMode

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White

// Class - Enum - Defines the specific type of mini-game
enum class GameType {
    TRIVIA, RHYTHM, MIMIC
}

// Function - Main Screen - Allows user to configure a new multiplayer room
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerModeSelectionScreen(
    onCreateRoom: (GameMode, GameType, Boolean) -> Unit, // Variable - Callback - Triggered when "Create" is clicked
    onBack: () -> Unit // Variable - Navigation - Go back to Lobby
) {

    // Flow 1.1: State Initialization
    // Default to English language and Trivia mode
    var selectedMode by remember { mutableStateOf(GameMode.ENGLISH) }
    var selectedType by remember { mutableStateOf(GameType.TRIVIA) }

    // Flow 2.0: UI Layout Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Room", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground1
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .background(DarkBackground1) // Ensure background covers scrolling area
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Flow 2.1: Language Selection Section
            Text(
                text = "1. Select Song Language",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // UI - Filter Chip for English
                FilterChip(
                    selected = selectedMode == GameMode.ENGLISH,
                    onClick = { selectedMode = GameMode.ENGLISH },
                    label = { Text("🇺🇸 English", color = TextWhite) },
                    modifier = Modifier.padding(end = 12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleAccent,
                        containerColor = CardColor1
                    )
                )
                // UI - Filter Chip for Mandarin
                FilterChip(
                    selected = selectedMode == GameMode.MANDARIN,
                    onClick = { selectedMode = GameMode.MANDARIN },
                    label = { Text("🇨🇳 Mandarin", color = TextWhite) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleAccent,
                        containerColor = CardColor1
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // Flow 2.2: Game Type Selection Section
            Text(
                text = "2. Select Game Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Flow 2.3: Game Type Cards
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
                // Mimic Card
                GameTypeCard(
                    title = "Mimic",
                    icon = Icons.Default.Mic,
                    isSelected = selectedType == GameType.MIMIC,
                    onClick = { selectedType = GameType.MIMIC }
                )
            }

            Spacer(Modifier.weight(1f))

            // Flow 3.0: Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button 1: Create Real PVP Room
                Button(
                    onClick = { onCreateRoom(selectedMode, selectedType, false) }, // Logic - isBot = false
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("CREATE PVP", color = TextWhite, fontWeight = FontWeight.Bold)
                }

                // Button 2: Create Bot Practice Room
                Button(
                    onClick = { onCreateRoom(selectedMode, selectedType, true) }, // Logic - isBot = true
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardColor1)
                ) {
                    Text("VS BOT 🤖", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Function - UI Component - Reusable card for selecting a game mode
// Flow 4.0: Selection Card Logic
@Composable
fun RowScope.GameTypeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp)
            .clickable { onClick() }, // Input - Click Listener
        colors = CardDefaults.cardColors(
            // Logic - Conditional Color - Highlights when selected
            containerColor = if (isSelected) PurpleAccent.copy(alpha = 0.2f) else CardColor1
        ),
        // Logic - Conditional Border - Adds purple border when selected
        border = if (isSelected) BorderStroke(2.dp, PurpleAccent) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PurpleAccent else TextWhite // Logic - Icon Tint
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}