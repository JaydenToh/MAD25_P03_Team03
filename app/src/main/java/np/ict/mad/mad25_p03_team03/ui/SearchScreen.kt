package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.data.AppDatabase
import np.ict.mad.mad25_p03_team03.data.SongEntity

@Composable
fun SearchScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).songDao()
    val scope = rememberCoroutineScope()

    var keyword by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SongEntity>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Search Songs",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = keyword,
            onValueChange = {
                keyword = it

                // ⭐ 当输入改变时，自动执行数据库搜索
                scope.launch {
                    results = if (keyword.isBlank()) {
                        emptyList()
                    } else {
                        dao.searchSongs(keyword)
                    }
                }
            },
            label = { Text("Search song title...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (results.isEmpty()) {
            Text("No matching songs.")
        } else {
            LazyColumn {
                items(results) { song ->
                    SongResultItem(song)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SongResultItem(song: SongEntity) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = song.title, style = MaterialTheme.typography.titleMedium)
        Text(text = song.artist, style = MaterialTheme.typography.bodyMedium)
    }
}
