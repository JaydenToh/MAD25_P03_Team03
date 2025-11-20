package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_p03_team03.data.SongEntity

@Composable
fun SearchScreen(onBack: () -> Unit) {

    var keyword by remember { mutableStateOf("") }

    // 搜索结果（还没接 Room，所以暂时 empty list）
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
                // 这里之后会放 Room 搜索逻辑
            },
            label = { Text("Search song title...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 搜索结果列表（之后会改 LazyColumn）
        results.forEach {
            Text(text = it.title)
        }
    }
}
