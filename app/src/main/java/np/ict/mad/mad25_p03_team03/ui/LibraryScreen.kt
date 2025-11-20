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
fun LibraryScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val songDao = db.songDao()

    var songs by remember { mutableStateOf<List<SongEntity>>(emptyList()) }

    // Load data once when screen appears
    LaunchedEffect(true) {
        songs = songDao.getAllSongs()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {

        Text("Song Library", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(songs) { song ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(song.title, style = MaterialTheme.typography.titleMedium)
                        Text(song.artist, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
