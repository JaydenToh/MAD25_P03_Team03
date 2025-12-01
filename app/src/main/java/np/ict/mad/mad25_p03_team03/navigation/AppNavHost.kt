package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.ui.*

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "game_splash"  // 从你的游戏开始页面开始
    ) {

        composable("game_splash") {
            SongGuessingGameScreen(
                onStartGameClick = {
                    navController.navigate("game_play")
                },
                onSongLibraryClick = {
                    navController.navigate("library")
                }
            )
        }

        composable("game_play") {
            GamePlayScreen(
                onGameComplete = { navController.navigate("game_splash") }
            )
        }

        /*composable("library") {
            LibraryScreen(
                onBack = { navController.popBackStack() }
            )
        }*/
    }
}