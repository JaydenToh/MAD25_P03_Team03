package np.ict.mad.mad25_p03_team03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import np.ict.mad.mad25_p03_team03.navigation.AppNavGraph
import np.ict.mad.mad25_p03_team03.data.SongRepository


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val songRepository = SongRepository()
                    AppNavGraph(songRepository = songRepository)
                }
            }
        }
    }
}



