package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_p03_team03.data.Difficulty
import np.ict.mad.mad25_p03_team03.data.GameMode

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val GrayText = Color.Gray

// Function - Main Screen - Select Language and Difficulty for Single Player
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    onStartGame: (GameMode, Difficulty) -> Unit, // Variable - Input - Callback to start the game
    onBack: () -> Unit                           // Variable - Input - Callback to go back
) {
    // Flow 1.1: State Initialization
    var selectedMode by remember { mutableStateOf(GameMode.ENGLISH) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    // Flow 2.0: UI Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game Setup", color = TextWhite, fontWeight = FontWeight.Bold) },
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
                .background(DarkBackground1), // Ensure background is dark
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Flow 2.1: Language Selection
            Text(
                text = "Select Language",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = selectedMode == GameMode.ENGLISH,
                    onClick = { selectedMode = GameMode.ENGLISH },
                    label = { Text("🇺🇸 English", color = TextWhite) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleAccent,
                        containerColor = CardColor1
                    )
                )
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

            Spacer(Modifier.height(32.dp))

            // Flow 2.2: Difficulty Selection
            Text(
                text = "Select Difficulty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(16.dp))

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

            // Flow 3.0: Start Game Action
            Button(
                onClick = { onStartGame(selectedMode, selectedDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccent, // Variable - Color - Accent
                    contentColor = TextWhite
                )
            ) {
                Text("START SINGLE PLAYER", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))


            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f))
                Text(" OR ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp))
                Divider(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))


            Button(
                onClick = onStartPvp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardColor2,
                    contentColor = Color.White
                )
            ) {
                Spacer(Modifier.width(8.dp))
                Text("PLAY PVP (1 VS 1)", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// Function - UI Component - Selectable Difficulty Card
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurpleAccent.copy(alpha = 0.1f) else CardColor1
        ),
        border = if (isSelected) BorderStroke(2.dp, PurpleAccent) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = PurpleAccent, unselectedColor = GrayText)
            )
            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = difficulty.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${difficulty.timeLimitSeconds} seconds per song",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
            }
        }
    }
}