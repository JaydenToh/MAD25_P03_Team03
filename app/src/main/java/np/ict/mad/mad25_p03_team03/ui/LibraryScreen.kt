package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import np.ict.mad.mad25_p03_team03.data.AppDatabase
import np.ict.mad.mad25_p03_team03.data.SongEntity

@Composable
fun LibraryScreen() {

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val dao = db.songDao()

    var songList by remember { mutableStateOf(listOf<SongEntity>()) }

    // Load songs when screen opens
    LaunchedEffect(true) {
        songList = dao.getAllSongs()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Song Library", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (songList.isEmpty()) {
            Text("No songs in the database.")
        } else {
            songList.forEach { song ->
                Text("${song.title} - ${song.artist}")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

