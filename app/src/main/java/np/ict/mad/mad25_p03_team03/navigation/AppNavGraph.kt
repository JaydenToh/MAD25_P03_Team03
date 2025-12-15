// navigation/AppNavGraph.kt
package np.ict.mad.mad25_p03_team03.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import np.ict.mad.mad25_p03_team03.SongIdentifier
import np.ict.mad.mad25_p03_team03.SongLibrary
import np.ict.mad.mad25_p03_team03.data.GameMode
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.BottomNavBar
import np.ict.mad.mad25_p03_team03.ui.GameScreen
import np.ict.mad.mad25_p03_team03.ui.HomeScreen
import np.ict.mad.mad25_p03_team03.ui.LeaderboardScreen
import np.ict.mad.mad25_p03_team03.ui.ModeSelectionScreen
import np.ict.mad.mad25_p03_team03.ui.ProfileScreen
import np.ict.mad.mad25_p03_team03.ui.RulesScreen



// navigation/AppNavGraph.kt
@Composable
fun AppNavGraph(songRepository: SongRepository,onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentDestination = currentDestination)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues) // handle scaffold padding
        ) {
            composable("home") {
                HomeScreen(
                    onStartGame = { navController.navigate("rules") },
                    onOpenLibrary = { navController.navigate("library") },
                    onIdentifySong = { navController.navigate("identifier") },
                    onSignOut = onSignOut
                )
            }

            composable("rules") {
                RulesScreen(
                    onStartGame = { navController.navigate("mode_selection") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("mode_selection") {
                ModeSelectionScreen(
                    onModeSelected = { mode ->
                        // 跳转到 game 并带上参数，例如: game/ENGLISH 或 game/MANDARIN
                        navController.navigate("game/${mode.name}")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "game/{mode}", // 定义路由包含参数
                arguments = listOf(navArgument("mode") { type = NavType.StringType }) // 定义参数类型
            ) { backStackEntry ->
                // 从路由中解析出 mode 字符串
                val modeString =
                    backStackEntry.arguments?.getString("mode") ?: GameMode.ENGLISH.name
                // 转换回 Enum
                val mode = try {
                    GameMode.valueOf(modeString)
                } catch (e: Exception) {
                    GameMode.ENGLISH // 如果出错，默认英文
                }

                GameScreen(
                    songRepository = songRepository,
                    gameMode = mode,
                    onNavigateBack = { navController.popBackStack() }
                )
            }


            composable("identifier") {
                SongIdentifier()
            }

            composable("leaderboard") {
                LeaderboardScreen()
            }

            composable("profile") {
                ProfileScreen()
            }

            // library still opens a new activity
            composable("library") {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    context.startActivity(android.content.Intent(context, SongLibrary::class.java))
                    navController.popBackStack() // immediately go back after launching to avoid stacking
                }
            }

        }
    }
}