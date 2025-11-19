package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartGame: () -> Unit,
    onOpenLibrary: () -> Unit,
    onSearchSongs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Song Guesser")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onStartGame) {
            Text("Start Game")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onOpenLibrary) {
            Text("Song Library")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSearchSongs) {
            Text("Search Songs")
        }
    }
}
