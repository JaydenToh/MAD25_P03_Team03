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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.SongIdentifier
import np.ict.mad.mad25_p03_team03.SongLibrary
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.BottomNavBar
import np.ict.mad.mad25_p03_team03.ui.GameScreen
import np.ict.mad.mad25_p03_team03.ui.HomeScreen
import np.ict.mad.mad25_p03_team03.ui.LeaderboardScreen
import np.ict.mad.mad25_p03_team03.ui.ProfileScreen
import np.ict.mad.mad25_p03_team03.ui.RulesScreen



// navigation/AppNavGraph.kt
@Composable
fun AppNavGraph(songRepository: SongRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            // ✅ 只需这一行引入你的导航栏！
            BottomNavBar(navController = navController, currentDestination = currentDestination)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues) // 👈 关键：内容避开底部栏
        ) {
            composable("home") {
                HomeScreen(
                    onStartGame = { navController.navigate("rules") },
                    onOpenLibrary = { navController.navigate("library") },
                    onIdentifySong = { navController.navigate("identifier") }
                )
            }

            composable("rules") {
                RulesScreen(
                    onStartGame = { navController.navigate("game") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("game") {
                GameScreen(
                    songRepository = songRepository,
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

            // library 仍可跳 Activity
            composable("library") {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    context.startActivity(android.content.Intent(context, SongLibrary::class.java))
                    navController.popBackStack() // 立即返回，避免白屏
                }
            }

        }
    }
}