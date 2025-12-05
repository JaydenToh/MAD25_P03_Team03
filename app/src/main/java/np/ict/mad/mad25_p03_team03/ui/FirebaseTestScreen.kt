package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import SongRepository
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.data.SimpleSong

@Composable
fun FirebaseTestScreen(
    songRepository: SongRepository
) {
    var songs by remember { mutableStateOf<List<SimpleSong>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // 加载数据
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val loadedSongs = songRepository.getSongs()
            songs = loadedSongs
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Firebase Test Screen",
            style = MaterialTheme.typography.headlineMedium
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(songs) { song ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = song.title ?: "Unknown Title",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = song.artist ?: "Unknown Artist",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = song.id ?: "No ID",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        val loadedSongs = songRepository.getSongs()
                        songs = loadedSongs
                    } catch (e: Exception) {
                        error = e.message
                    }
                    isLoading = false
                }
            }
        ) {
            Text("Refresh Data")
        }
    }
}