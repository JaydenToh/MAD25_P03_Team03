// np/ict/mad/mad25_p03_team03/ui/SongDetailScreen.kt
package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.segmentation.subject.Subject
import kotlinx.coroutines.delay
import np.ict.mad.mad25_p03_team03.SongItem
import androidx.media3.common.MediaMetadata
import np.ict.mad.mad25_p03_team03.rememberMediaController

@Composable
fun SongDetailScreen(
    collectionName: String,
    songTitle: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var song by remember { mutableStateOf<SongItem?>(null) }
    var loading by remember { mutableStateOf(true) }

    // ExoPlayer State
    val controller = rememberMediaController()
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }


    var showLyricsDialog by remember { mutableStateOf(false) }

    // Fetch Song Details
    LaunchedEffect(collectionName, songTitle) {
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .whereEqualTo("title", songTitle)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val item = result.documents[0].toObject(SongItem::class.java)
                    song = item
                }
                loading = false
            }
    }

    // Update Slider Progress
    LaunchedEffect(song, controller) {
        if (song != null && controller != null) {
            val currentMediaId = controller.currentMediaItem?.mediaId
            // Only set media item if it's different
            if (currentMediaId != song!!.audioUrl) {
                val metadata = MediaMetadata.Builder()
                    .setTitle(song!!.title)
                    .setArtist(song!!.artist)
                    .setAlbumTitle(song!!.album)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(song!!.audioUrl)
                    .setMediaId(song!!.audioUrl)
                    .setMediaMetadata(metadata)
                    .build()

                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
            }
            // isPlaying state will be updated by the LaunchedEffect loop below
        }
    }

    // Cleanup
    LaunchedEffect(controller) {
        while (true) {
            controller?.let {
                currentPosition = it.currentPosition
                totalDuration = it.duration.coerceAtLeast(0L)
                isPlaying = it.isPlaying
            }
            delay(500)
        }
    }

    // Helper to format time (e.g., 1000ms -> 00:01)
    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3E1066), Color(0xFF102A6E))
                )
            )
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
        } else if (song == null) {
            Text("Song not found", color = Color.White, modifier = Modifier.align(Alignment.Center))
        } else {
            val currentSong = song!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Now Playing", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 2. Album Art Placeholder (Circle)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF59168B).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Song Info
                Text(currentSong.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                Text(currentSong.artist, fontSize = 18.sp, color = Color.LightGray, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Progress Bar & Times
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
                        onValueChange = { newPercent ->
                            val newTime = (newPercent * totalDuration).toLong()
                            controller?.seekTo(newTime)
                            currentPosition = newTime
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF00BCD4),
                            inactiveTrackColor = Color.Gray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(currentPosition), color = Color.LightGray, fontSize = 12.sp)
                        Text(formatTime(totalDuration), color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Play Controls
                IconButton(
                    onClick = {
                        controller?.let {
                            if (it.isPlaying) it.pause() else it.play()
                            // isPlaying state will be updated by the LaunchedEffect loop
                        }
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 6. Lyrics Section (Scrollable)
                OutlinedButton(
                    onClick = { showLyricsDialog = true },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00BCD4)),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Subject, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Lyrics")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
            if (showLyricsDialog) {
                LyricsPopup(
                    lyrics = currentSong.lyrics,
                    onDismiss = { showLyricsDialog = false }
                )
            }
        }
    }
}


@Composable
fun LyricsPopup(lyrics: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2F2F45))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Popup Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lyrics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Lyrics Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = lyrics.replace("\\n", "\n"), 
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}