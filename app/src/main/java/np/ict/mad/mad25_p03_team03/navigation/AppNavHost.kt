package np.ict.mad.mad25_p03_team03.navigation

import SongRepository
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.ui.*

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    songRepository: SongRepository
) {
    NavHost(
        navController = navController,
        startDestination = "test_firebase"  // 暂时改为测试页面
    ) {
        composable("test_firebase") {
            FirebaseTestScreen(songRepository = songRepository)
        }

        composable("game_splash") {
            SongGuessingGameScreen(
                onStartGameClick = {
                    // navController.navigate("game_play")  // ❌ 注释掉，因为 GamePlayScreen 已被注释
                    navController.navigate("test_firebase")  // ✅ 临时导航到测试页面
                },
                onSongLibraryClick = {
                    navController.navigate("library")
                }
            )
        }

        // composable("game_play") {  // ❌ 注释掉整个 GamePlayScreen 的路由
        //     GamePlayScreen(
        //         songRepository = songRepository,
        //         onGameComplete = { navController.navigate("game_splash") }
        //     )
        // }
    }
}