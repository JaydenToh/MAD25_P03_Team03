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
                    // change to Navigation
                    AppNavGraph()
                }
            }
        }
    }
}




data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioResId: Int
)

