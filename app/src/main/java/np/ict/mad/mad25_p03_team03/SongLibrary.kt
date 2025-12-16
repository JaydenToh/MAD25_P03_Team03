// np/ict/mad/mad25_p03_team03/SongLibrary.kt
package np.ict.mad.mad25_p03_team03

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController


data class SongItem(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val audioUrl: String = "",
    val lyrics: String = "No lyrics available for this song.",
)


@Composable
fun SongLibraryScreen(
    collectionName: String,
    onNavigateBack: () -> Unit,
    onSongClick: (String) -> Unit
) {
    val context = LocalContext.current
    var songList by remember { mutableStateOf(listOf<SongItem>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val controller = rememberMediaController()



    var currentPlayingUrl by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }


    LaunchedEffect(collectionName) {
        loading = true
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val songs = result.documents.mapNotNull { doc ->
                    doc.toObject(SongItem::class.java)
                }
                songList = songs
                loading = false
            }
            .addOnFailureListener { e ->
                error = e.message
                loading = false
            }
    }


    fun playAudio(song: SongItem) {
        if (controller == null) return

        val currentMediaId = controller.currentMediaItem?.mediaId
        val isPlaying = controller.isPlaying

        if (currentMediaId == song.audioUrl && isPlaying) {
            controller.pause()
        } else {
            // 【重要】设置 MediaItem 时带上 Metadata，这样通知栏就有字了
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(song.audioUrl)
                .setMediaId(song.audioUrl)
                .setMediaMetadata(metadata)
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
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

    LaunchedEffect(controller) {
        while(true) {
            controller?.let {
                currentPlayingUrl = it.currentMediaItem?.mediaId
                isPlaying = it.isPlaying
            }
            kotlinx.coroutines.delay(500) // 每 0.5 秒检查一次
        }
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
            loading -> Text("Loading $collectionName...", color = Color.White, modifier = Modifier.align(Alignment.Center))
            error != null -> Text("Error: $error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Header with Back Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "English Songs",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                    Spacer(modifier = Modifier.height(20.dp))

                    SongList(
                        songs = filteredSongs,
                        onSongRowClick = { title -> onSongClick(title) },
                        onPlayClick = { song -> playAudio(song) },
                        currentUrl = currentPlayingUrl,
                        isPlaying = isPlaying
                    )
                }
            }
        }
    }
}

// SongList, SongRow, SearchBar
@Composable
fun SongList(songs: List<SongItem>, onSongRowClick: (String) -> Unit, onPlayClick: (SongItem) -> Unit, currentUrl: String?, isPlaying: Boolean) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(songs) { song ->
            SongRow(song = song,
                onClick = { onSongRowClick(song.title) }, // Pass click
                onPlayClick = {onPlayClick(song) }, // Pass song
                isThisSongPlaying = (currentUrl == song.audioUrl && isPlaying))
        }
    }
}

@Composable
fun SongRow(song: SongItem,onClick: () -> Unit, onPlayClick: () -> Unit, isThisSongPlaying: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F2F45)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = song.artist, fontSize = 14.sp, color = Color(0xFFC9C9C9))
                Text(text = song.album, fontSize = 12.sp, color = Color(0xFF9A9A9A))
            }
            IconButton(
                onClick = { if (song.audioUrl.isNotEmpty()) onPlayClick() },
                modifier = Modifier.size(48.dp).background(Color(0xFF3A3A50), shape = RoundedCornerShape(50))
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