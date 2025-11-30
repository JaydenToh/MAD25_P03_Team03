package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

// 🚨 IMPORT SCREEN COMPOSABLES FROM THEIR NEW 'ui' LOCATION
import np.ict.mad.mad25_p03_team03.ui.SongGuessingGameScreen
import np.ict.mad.mad25_p03_team03.ui.GamePlayScreen
import np.ict.mad.mad25_p03_team03.LoginScreen // LoginScreen is being left in MainActivity.kt

// 1. Define the possible screens/destinations (Sealed Class)
sealed class Screen {
    data object Login : Screen()
    data object GameSplash : Screen()
    data object GamePlay : Screen()
    data object SongIdentifier : Screen() // The song ID activity you implemented earlier
}

@Composable
fun AppNavHost() {
    // 2. Track the current screen state. Start on the splash screen for the game.
    var currentScreen by remember { mutableStateOf<Screen>(Screen.GameSplash) }

    when (currentScreen) {
        Screen.Login -> LoginScreen()

        Screen.GameSplash -> SongGuessingGameScreen(
            // Pass a callback to transition to the GamePlay screen
            onStartGameClick = {
                currentScreen = Screen.GamePlay
            },
            // Example for Song Library button
            onSongLibraryClick = {
                // You can add logic here to navigate to a Library screen if you create one
            }
        )

        Screen.GamePlay -> GamePlayScreen()

        Screen.SongIdentifier -> {
            // Placeholder for SongIdentifier screen
            Text("Song Identifier Screen Placeholder")
        }
    }
}