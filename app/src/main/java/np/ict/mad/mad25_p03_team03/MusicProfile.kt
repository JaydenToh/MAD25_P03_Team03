package np.ict.mad.mad25_p03_team03

import android.R.attr.repeatMode
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.mad25_p03_team03.ui.theme.MAD25_P03_Team03Theme

class MusicProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("TITLE") ?: "Unknown"
        val artist = intent.getStringExtra("ARTIST") ?: "Unknown"
        val lyrics = intent.getStringExtra("LYRICS") ?: "No lyrics available."
        val drawableId = intent.getIntExtra("IMAGE_ID", R.drawable.arcanepic)
        setContent {
            MAD25_P03_Team03Theme {
                        MusicProfileScreen(title, artist, lyrics, drawableId, onBack = { finish() })
            }
        }
    }
}

@Composable
fun MusicProfileScreen(
    title: String,
    artist: String,
    lyrics: String,
    drawableId: Int,
    onBack: () -> Unit
){
    
}