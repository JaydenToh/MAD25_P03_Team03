package np.ict.mad.mad25_p03_team03

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.ict.mad.mad25_p03_team03.navigation.AppNavGraph
import np.ict.mad.mad25_p03_team03.ui.theme.MAD25_P03_Team03Theme
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import np.ict.mad.mad25_p03_team03.navigation.AppNavGraph
import np.ict.mad.mad25_p03_team03.ui.theme.MAD25_P03_Team03Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MAD25_P03_Team03Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🔥 改成 Navigation
                    AppNavGraph()
                }
            }
        }
    }
}

@Composable
fun SongGuessGameScreen() {
    val context = LocalContext.current

    val questions = listOf(
        SongQuestion(
            correctTitle = "Song A",
            options = listOf("Song A", "Song B", "Song C", "Song D"),
            audioResId = R.raw.song1
        ),
        SongQuestion(
            correctTitle = "Song B",
            options = listOf("Song X", "Song Y", "Song B", "Song Z"),
            audioResId = R.raw.song2
        )
    )

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }

    var message by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(10) }

    // 🔥 MediaPlayer State
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }


    val currentQuestion = questions[currentIndex]

    // 🔥 CountDownTimer
    LaunchedEffect(currentIndex) {
        timeLeft = 10

        object : CountDownTimer(10000, 1000) {
            override fun onTick(ms: Long) {
                timeLeft = (ms / 1000).toInt()
            }

            override fun onFinish() {
                lives -= 1
                message = "Time's up!"


                if (lives > 0 && currentIndex < questions.lastIndex) {
                    currentIndex += 1
                }
            }
        }.start()
    }


    LaunchedEffect(currentIndex) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Song Guesser",
            style = MaterialTheme.typography.headlineMedium
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Score: $score")
            Text("Lives: $lives")
            Text("Time: $timeLeft")
        }


        Button(
            onClick = {

                mediaPlayer?.stop()
                mediaPlayer?.release()


                mediaPlayer = MediaPlayer.create(context, currentQuestion.audioResId)
                mediaPlayer?.start()
            }
        ) {
            Text("Play Song Clip")
        }


        currentQuestion.options.forEach { option ->
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (option == currentQuestion.correctTitle) {
                        score += 10
                        message = "Correct!"
                    } else {
                        lives -= 1
                        message = "Wrong!"
                    }

                    if (lives > 0 && currentIndex < questions.lastIndex) {
                        currentIndex += 1
                    }
                }
            ) {
                Text(option)
            }
        }

        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }


        if (lives <= 0 || currentIndex == questions.lastIndex) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Game Over — Final Score: $score",
                fontWeight = FontWeight.Bold
            )
        }
    }
}


data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioResId: Int
)

