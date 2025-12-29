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

    var currentLevelIndex by remember { mutableStateOf(0) }
    val currentLevel = levels[currentLevelIndex]

    var isListening by remember { mutableStateOf(false) }
    var currentPitch by remember { mutableStateOf(0f) }
    var currentNoteName by remember { mutableStateOf("--") }
    var matchProgress by remember { mutableStateOf(0f) }

    val pitchDetector = remember { PitchDetector() }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

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


    fun playTargetSound() {
        scope.launch {
            isListening = false
            pitchDetector.stop()

            Toast.makeText(context, "Listen...", Toast.LENGTH_SHORT).show()
            val targetFreq = levels[currentLevelIndex].frequency

            SoundGenerator.playTone(targetFreq, 1000)

            delay(500)
            if (!isListening) {

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


    LaunchedEffect(currentPitch) {
        if (isListening && currentPitch > 0) {

            val diff = abs(currentPitch - currentLevel.frequency)

            if (diff < 20.0) {
                matchProgress += 0.05f
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
                if (matchProgress > 0) matchProgress -= 0.02f
            }
        }
    }


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

        Text("Humming Challenge", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(currentLevel.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(32.dp))


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


        Text("You represent:", style = MaterialTheme.typography.labelMedium)
        Text("$currentNoteName (${currentPitch.toInt()} Hz)", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray, CircleShape)
        ) {

            Box(Modifier.align(Alignment.Center).width(4.dp).fillMaxHeight().background(Color.Black))


            val diff = (currentPitch - currentLevel.frequency).coerceIn(-100.0, 100.0)
            val offsetX = (diff / 100.0) * 150


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

        Text("Holding Logic...", style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(
            progress = { matchProgress },
            modifier = Modifier.fillMaxWidth().height(20.dp),
            color = Color.Green,
            trackColor = Color.LightGray
        )

        Spacer(Modifier.height(24.dp))

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