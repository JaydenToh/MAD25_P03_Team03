// navigation/AppNavGraph.kt
package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.data.SongRepository // 👈 新增导入
import np.ict.mad.mad25_p03_team03.ui.GameScreen


@Composable
fun AppNavGraph(songRepository: SongRepository) { // ✅ 加参数

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "game"
    ) {

        composable("game") {
            GameScreen(songRepository = songRepository) // ✅ 传给 GameScreen
        }


    }
}