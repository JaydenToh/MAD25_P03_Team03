// navigation/AppNavGraph.kt
package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import np.ict.mad.mad25_p03_team03.SongIdentifier
import np.ict.mad.mad25_p03_team03.SongLibraryScreen
import np.ict.mad.mad25_p03_team03.data.Difficulty
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
                    onStartGame = { mode, difficulty ->

                        navController.navigate("game/${mode.name}/${difficulty.name}")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "game/{mode}/{difficulty}",
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val modeString = backStackEntry.arguments?.getString("mode") ?: GameMode.ENGLISH.name
                val mode = try { GameMode.valueOf(modeString) } catch (e: Exception) { GameMode.ENGLISH }


                val diffString = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
                val difficulty = try { Difficulty.valueOf(diffString) } catch (e: Exception) { Difficulty.EASY }

                GameScreen(
                    songRepository = songRepository,
                    gameMode = mode,
                    difficulty = difficulty,
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

            composable("library") {
                SongLibraryScreen()
            }

        }
    }
}