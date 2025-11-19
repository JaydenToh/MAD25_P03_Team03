package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.ui.GameScreen
import np.ict.mad.mad25_p03_team03.ui.HomeScreen
//import np.ict.mad.mad25_p03_team03.ui.LibraryScreen
//import np.ict.mad.mad25_p03_team03.ui.SearchScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onStartGame = { navController.navigate("game") },
                onOpenLibrary = { navController.navigate("library") },
                onSearchSongs = { navController.navigate("search") }
            )
        }

        composable("game") {
            GameScreen()
        }

        /*composable("library") {
            LibraryScreen()
        }

        composable("search") {
            SearchScreen()
        }*/
    }
}
