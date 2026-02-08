package np.ict.mad.mad25_p03_team03.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFCF6679)

// Class - Data Model - Represents a specific singing challenge
data class MimicLevel(val name: String, val targetNote: String, val frequency: Double)

// Variable - Game Config - Hardcoded levels
val levels = listOf(
    MimicLevel("Level 1", "C4 (Do)", 261.63),
    MimicLevel("Level 2", "E4 (Mi)", 329.63),
    MimicLevel("Level 3", "G4 (Sol)", 392.00),
    MimicLevel("Level 4", "A4 (La)", 440.00)
)

// Function - Main Screen - Single Player Pitch Mimic Game
// Flow 1.0: Screen Entry Point
@Composable
fun MimicGameScreen(onNavigateBack: () -> Unit) { // Variable - Input - Callback to exit
    // Flow 1.1: Dependency Setup
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Variable - State - Game Progress
    var currentLevelIndex by remember { mutableStateOf(0) }
    val currentLevel = levels[currentLevelIndex]

    // Variable - State - Audio Processing
    var isListening by remember { mutableStateOf(false) }
    var currentPitch by remember { mutableStateOf(0f) }
    var currentNoteName by remember { mutableStateOf("--") }
    var matchProgress by remember { mutableStateOf(0f) }

    // Variable - Helper - Detects frequency from mic
    val pitchDetector = remember { PitchDetector() }

    // Flow 2.0: Permission Handling
    // Launcher to request Microphone access
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Logic - Start Listening
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

    // Function - Audio Logic - Plays the reference tone
    // Flow 3.0: Sound Generation
    fun playTargetSound() {
        scope.launch {
            isListening = false
            pitchDetector.stop() // Stop detecting while playing to avoid feedback

            Toast.makeText(context, "Listen...", Toast.LENGTH_SHORT).show()
            val targetFreq = levels[currentLevelIndex].frequency

            SoundGenerator.playTone(targetFreq, 1000)

            delay(500)
            if (!isListening) {
                // Logic - Resume Listening
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

    // Flow 4.0: Pitch Matching Logic
    // Runs whenever the detected pitch changes
    LaunchedEffect(currentPitch) {
        if (isListening && currentPitch > 0) {

            val diff = abs(currentPitch - currentLevel.frequency)

            // Logic - Threshold Check (Within 20Hz)
            if (diff < 20.0) {
                matchProgress += 0.05f // Increment progress

                // Logic - Win Condition (Bar Full)
                if (matchProgress >= 1f) {
                    matchProgress = 0f
                    isListening = false

                    scope.launch {
                        Toast.makeText(context, "Perfect! Next Level!", Toast.LENGTH_SHORT).show()
                        delay(1000)

                        if (currentLevelIndex < levels.size - 1) {
                            currentLevelIndex++
                            playTargetSound()
                        } else {
                            Toast.makeText(context, "You Finished All Levels!", Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        }
                    }
                }
            } else {
                // Logic - Decay progress if pitch is wrong
                if (matchProgress > 0) matchProgress -= 0.02f
            }
        }
    }

    // Flow 1.2: Cleanup
    DisposableEffect(Unit) {
        onDispose {
            pitchDetector.stop()
        }
    }

    // Flow 5.0: UI Construction
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground1) // Variable - Color - Dark Background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // UI - Header
        Text(
            "Humming Challenge",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Spacer(Modifier.height(8.dp))
        Text(
            currentLevel.name,
            style = MaterialTheme.typography.titleLarge,
            color = PurpleAccent // Variable - Color - Purple
        )

        Spacer(Modifier.height(32.dp))

        // UI - Info Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor1) // Variable - Color - Dark Card
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Target Note",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    currentLevel.targetNote,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(Modifier.height(16.dp))

                // UI - Play Button
                Button(
                    onClick = { playTargetSound() },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = TextWhite)
                    Spacer(Modifier.width(8.dp))
                    Text("Play Tone", color = TextWhite)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // UI - Real-time Feedback
        Text(
            "You represent:",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            "$currentNoteName (${currentPitch.toInt()} Hz)",
            style = MaterialTheme.typography.headlineSmall,
            color = TextWhite
        )

        Spacer(Modifier.height(16.dp))

        // UI - Visualizer Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(CardColor1, CircleShape) // Variable - Color - Dark Track
        ) {
            // UI - Center Marker
            Box(Modifier.align(Alignment.Center).width(4.dp).fillMaxHeight().background(Color.Gray))

            // Logic - Visualizer Offset Calculation
            val diff = (currentPitch - currentLevel.frequency).coerceIn(-100.0, 100.0)
            val offsetX = (diff / 100.0) * 150

            // UI - Moving Cursor
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = offsetX.dp)
                    .size(20.dp)
                    .background(
                        if (abs(diff) < 15) SuccessGreen else ErrorRed,
                        CircleShape
                    )
            )
        }
        Text("Low  < --- >  High", modifier = Modifier.padding(top=8.dp), color = Color.Gray)

        Spacer(Modifier.weight(1f))

        // UI - Progress Bar
        Text(
            "Holding Logic...",
            style = MaterialTheme.typography.labelSmall,
            color = TextWhite
        )
        LinearProgressIndicator(
            progress = { matchProgress },
            modifier = Modifier.fillMaxWidth().height(20.dp),
            color = SuccessGreen,
            trackColor = CardColor1
        )

        Spacer(Modifier.height(24.dp))

        // Flow 6.0: Control Buttons
        if (!isListening) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) {
                Icon(Icons.Default.Mic, null, tint = TextWhite)
                Spacer(Modifier.width(8.dp))
                Text("Start Microphone", color = TextWhite)
            }
        } else {
            OutlinedButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextWhite)
            ) {
                Text("Quit")
            }
        }
    }
}