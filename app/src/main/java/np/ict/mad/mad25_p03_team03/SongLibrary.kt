package np.ict.mad.mad25_p03_team03

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

data class SongItem(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val audioUrl: String = "",
    @get:Exclude // Tells Firebase to ignore this field
    var drawableId: Int = R.drawable.arcanepic
)

class SongLibrary : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAD25_P03_Team03Theme {
                SongLibraryScreen()
            }
        }
    }
}

@Composable
fun SongLibraryScreen(
    collectionName: String,
    onNavigateBack: () -> Unit,
    onSongClick: (String) -> Unit
    ){

    val context = LocalContext.current
    var songList by remember { mutableStateOf(listOf<SongItem>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // 2. SETUP EXOPLAYER
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    // Track playing state
    var currentPlayingUrl by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentSong by remember { mutableStateOf(-1) }
    var repeat by remember { mutableStateOf(ExoPlayer.REPEAT_MODE_OFF) }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Cleanup when leaving screen
    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentSong = exoPlayer.currentMediaItemIndex
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }

    }

    LaunchedEffect(collectionName) {
        loading = true
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val songs = result.documents.mapNotNull { doc -> doc.toObject(SongItem::class.java) }
                songList = songs
                loading = false
            }

            .addOnFailureListener { e ->
                error = e.message
                loading = false
            }
    }

    fun getAlbumArtFromName(songTitle: String): Int {
        return when (songTitle.lowercase().trim()) {
            "heavy is the crown" -> R.drawable.heavyiscrownpic
            "i can't hear it now" -> R.drawable.icanthearitnowpic
            "paint the town blue" -> R.drawable.painttownbluepic
            "remember me" -> R.drawable.remembermepic
            "what have they done to us" -> R.drawable.whathavetheydonetouspic
            "blood sweat & tears" -> R.drawable.bloodsweattearspic
            // Add a default case to prevent crashes if a song title doesn't match
            else -> R.drawable.arcanepic
        }
    }

    fun playSong(index: Int) {
        if(currentSong == index) {
            if(isPlaying) exoPlayer.pause()
            else exoPlayer.play()
            return
        }

        if(exoPlayer.mediaItemCount != songList.size) {
            exoPlayer.clearMediaItems()
            val mediaItems = songList.map { MediaItem.fromUri(it.audioUrl) }
            exoPlayer.setMediaItems(mediaItems)
        }

        exoPlayer.seekTo(index, 0)
        exoPlayer.prepare()
        exoPlayer.play()

        currentSong = index
        isPlaying = true
    }


    val filteredSongs by remember(songList, searchQuery) {
        mutableStateOf(
            if (searchQuery.isBlank()) songList
            else {
                val q = searchQuery.trim().lowercase()
                songList.filter { song ->
                    song.title.lowercase().contains(q) ||
                            song.artist.lowercase().contains(q) ||
                            song.album.lowercase().contains(q)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF59168B), Color(0xFF1C398E), Color(0xFF312C85))
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            loading -> Text("Loading...", color = Color.White, modifier = Modifier.align(Alignment.Center))
            error != null -> Text("Error: $error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text("Song Library", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                    Spacer(modifier = Modifier.height(20.dp))

                    // List with Play Logic
                    SongList(
                        songs = filteredSongs,
                        onPlayClick = { url -> playAudio(url) },
                        currentUrl = currentPlayingUrl,
                        isPlaying = isPlaying
                    )
                }
            }
        }
    }
}

@Composable
fun SongList(
    songs: List<SongItem>,
    onPlayClick: (String) -> Unit,
    currentUrl: String?,
    isPlaying: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(songs) { song ->
            SongRow(
                song = song,
                onPlayClick = onPlayClick,
                isThisSongPlaying = (currentUrl == song.audioUrl && isPlaying)
            )
        }
    }
}

@Composable
fun SongRow(
    song: SongItem,
    onPlayClick: (String) -> Unit,
    isThisSongPlaying: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F2F45)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = song.drawableId),
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = song.artist, fontSize = 14.sp, color = Color(0xFFC9C9C9))
                Text(text = song.album, fontSize = 12.sp, color = Color(0xFF9A9A9A))
            }

            // PLAY BUTTON
            IconButton(
                onClick = {
                    if (song.audioUrl.isNotEmpty()) {
                        onPlayClick(song.audioUrl)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF3A3A50), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = if (isThisSongPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFE0E0E0), modifier = Modifier.size(26.dp)) },
        placeholder = { Text("Search songs...", fontSize = 16.sp, color = Color(0xFFE0E0E0)) },
        modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF3A3A50)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF3A3A50),
            unfocusedContainerColor = Color(0xFF3A3A50),
            focusedTextColor = Color.White,
            cursorColor = Color.White
        )
    )
}