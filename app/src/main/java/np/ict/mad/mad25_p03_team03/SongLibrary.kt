package np.ict.mad.mad25_p03_team03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*   // Row, Column, Box, padding, fillMaxSize, etc.
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
//import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Brush
import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.mad25_p03_team03.ui.theme.MAD25_P03_Team03Theme

data class SongItem(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
)

class SongLibrary : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAD25_P03_Team03Theme {
                SongLibraryScreen()
                MusicAppNavigation()
            }
        }
    }
}

@Composable
fun SongLibraryScreen() {

    var songList by remember { mutableStateOf(listOf<SongItem>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    // Load songs from Firestore once
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("songs")
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

    // Filter by title, artist, or album (case-insensitive)
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
                    colors = listOf(
                        Color(0xFF59168B),   // purple
                        Color(0xFF1C398E),   // blue
                        Color(0xFF312C85)    // dark purple
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            loading -> Text("Loading...", color = Color.White)
            error != null -> Text("Error: $error", color = Color.Red)
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()                      // ⬅️ moves everything below status bar
                        .padding(horizontal = 16.dp, vertical = 20.dp // ⬅️ nice breathing room
                        ) // 👈 space from edges/top
                ) {
                    Text(
                        text = "Song Library",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SongList(filteredSongs)
                }
            }
        }
    }
}

@Composable
fun SongList(songs: List<SongItem>) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        songs.forEach { song ->
            SongRow(song)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SongRow(song: SongItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2F2F45)  // card color
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Placeholder "cover" box – acts like the album art square
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF5757FF))   // bright accent color
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = song.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = song.artist,
                    fontSize = 14.sp,
                    color = Color(0xFFC9C9C9)
                )
                Text(
                    text = song.album,
                    fontSize = 12.sp,
                    color = Color(0xFF9A9A9A)
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp          // ⬅ Bigger input text
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFFE0E0E0),
                modifier = Modifier.size(26.dp)     // ⬅ Bigger icon
            )
        },
        placeholder = {
            Text(
                text = "Search songs or artists...",
                fontSize = 16.sp,                  // ⬅ Bigger placeholder
                color = Color(0xFFE0E0E0)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)                         // ⬅ Larger search bar height
            .clip(RoundedCornerShape(18.dp))       // ⬅ Slightly less rounded, more premium
            .background(Color(0xFF3A3A50)),        // ⬅ Improved contrast
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF3A3A50),
            unfocusedContainerColor = Color(0xFF3A3A50),
            disabledContainerColor = Color(0xFF3A3A50),

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        )
    )
}
