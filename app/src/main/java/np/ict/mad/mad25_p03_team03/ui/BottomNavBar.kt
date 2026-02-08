// ui/BottomNavBar.kt
package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

private val DarkBackground2 = Color(0xFF121212)

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    NavigationBar(
        containerColor = DarkBackground2,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Library,
            BottomNavItem.Leaderboard,
            BottomNavItem.Profile
        )

        items.forEach { item ->
            val selected = currentDestination?.isInHierarchy(item.route) == true
            NavigationBarItem(
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
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        // Increased size slightly for clarity
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = Color(0xFF2F2F45),

                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                )
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