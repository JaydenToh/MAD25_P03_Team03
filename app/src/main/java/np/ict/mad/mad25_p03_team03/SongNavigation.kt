package np.ict.mad.mad25_p03_team03

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
//import np.ict.mad.mad25_p03_team03.ui.theme.MAD25_P03_Team03Theme

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable


enum class MusicDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    LIBRARY("library", "Library", Icons.Filled.LibraryMusic),
    IDENTIFY("identify", "Identify", Icons.Filled.Mic),
    FAVORITES("favorites", "Favorites", Icons.Filled.Favorite)
}

@Composable
fun MusicAppNavigation() {
    val navController = rememberNavController()

    // Track which tab is selected using your enum
    var currentDestination by rememberSaveable { mutableStateOf(MusicDestination.LIBRARY) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MusicDestination.entries.forEach { destination ->
                val selected = currentDestination == destination

                item(
                    selected = selected,
                    onClick = {
                        currentDestination = destination
                        navController.navigate(destination.route)
                    },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MusicDestination.LIBRARY.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(MusicDestination.LIBRARY.route) {
                    SongLibraryScreen()          // ⬅️ no navController passed
                }
                composable(MusicDestination.IDENTIFY.route) {
                    //SongIdentifierScreen()       // ⬅️ no navController passed
                }
            }
        }
    }
}

