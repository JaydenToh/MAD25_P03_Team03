// ui/BottomNavBar.kt
package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Library,
            BottomNavItem.Leaderboard,
            BottomNavItem.Profile
        )

        items.forEach { item ->
            val selected = currentDestination?.isInHierarchy(item.route) == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    // avoid multiple copies of the same destination
                    if (!selected) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

// data model for bottom navigation items
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Library : BottomNavItem("library", "Library", Icons.Default.LibraryBooks)

    object Leaderboard : BottomNavItem("leaderboard", "Leaderboard", Icons.Default.SportsScore)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

// tools extension function to check if the current destination is in the hierarchy
private fun NavDestination?.isInHierarchy(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true