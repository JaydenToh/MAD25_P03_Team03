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
                SongLibraryScreen(
                    collectionName = "songs",
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun SongLibraryScreen(
    collectionName: String,
    onNavigateBack: () -> Unit
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

    var currentCollection by remember { mutableStateOf(collectionName) }

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

    LaunchedEffect(currentCollection) {
        loading = true
        // Clear list so user sees it's refreshing
        songList = emptyList()
        currentSong = -1

        FirebaseFirestore.getInstance().collection(currentCollection).get()
            .addOnSuccessListener { result ->
                val songs = result.documents.mapNotNull { doc -> doc.toObject(SongItem::class.java) }
                songs.forEach { it.drawableId = getAlbumArtFromName(it.title) }
                songList = songs
                loading = false
            }
            .addOnFailureListener { e -> error = e.message; loading = false }
    }

    LaunchedEffect(collectionName) {
        loading = true
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val songs =
                    result.documents.mapNotNull { doc -> doc.toObject(SongItem::class.java) }

                songs.forEach { song ->
                    song.drawableId = getAlbumArtFromName(song.title)
            }

                songList = songs
                loading = false
            }

            .addOnFailureListener { e ->
                error = e.message
                loading = false
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

    fun toggleRepeat() {
        val newMode = if (repeat == ExoPlayer.REPEAT_MODE_OFF) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_OFF
        exoPlayer.repeatMode = newMode
        repeat = newMode
    }

    val filteredSongs by remember(songList, searchQuery) {
        mutableStateOf(
            if (searchQuery.isBlank()) songList
            else songList.filter {
                it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true)
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (currentSong != -1 && songList.isNotEmpty() && currentSong < songList.size) {
                BottomPlayerBar(
                    song = songList[currentSong],
                    isPlaying = isPlaying,
                    repeatMode = repeatMode,
                    onPlayPause = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    onNext = { if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNext() },
                    onPrevious = { if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPrevious() },
                    onRepeat = { toggleRepeat() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Text("Song Library", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.weight(1f))

                // Toggle button
                Button(
                    onClick = {
                        currentCollection = if (currentCollection == "songs") "songs_mandarin" else "songs"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A50)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (currentCollection == "songs") "EN" else "CN",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
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

                        SongList(
                            songs = filteredSongs,
                            onPlayClick = { song ->
                                val index = songList.indexOf(song)
                                if (index != -1) playSong(index)
                            },
                            currentSong = currentSong,
                            allSongs = songList,
                            isPlaying = isPlaying
                        )
                    }
                }
            }
        }
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

@Composable
fun BottomPlayerBar(
    song: SongItem,
    isPlaying: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRepeat: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth().height(85.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Image & Text
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = song.drawableId),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRepeat) {
                    val color = if (repeatMode == ExoPlayer.REPEAT_MODE_OFF) Color.Gray else Color(0xFFBB86FC)
                    Icon(if (repeatMode == ExoPlayer.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat, "Repeat", tint = color)
                }
                IconButton(onClick = onPrevious) { Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White) }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.background(Color(0xFFBB86FC), shape = RoundedCornerShape(50)).size(40.dp)
                ) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play", tint = Color.Black)
                }
                IconButton(onClick = onNext) { Icon(Icons.Filled.SkipNext, "Next", tint = Color.White) }
            }
        }
    }
}

@Composable
fun SongList(
    songs: List<SongItem>,
    onPlayClick: (SongItem) -> Unit,
    currentSong: Int,
    allSongs: List<SongItem>,
    isPlaying: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(songs) { song ->
            // Check if this song is the one currently playing
            // Find where this song is
                val indexInMainList = allSongs.indexOf(song)
                val isThisSongPlaying = (indexInMainList == currentSong && isPlaying)

                SongRow(
                    song = song,
                    onPlayClick = { onPlayClick(song) },
                isThisSongPlaying = isThisSongPlaying
            )
        }
    }
}

@Composable
fun SongRow(
    song: SongItem,
    onPlayClick: () -> Unit,
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
                onClick = onPlayClick,
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