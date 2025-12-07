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
            BottomNavItem.Game,
            BottomNavItem.Identifier,
            BottomNavItem.Library
        )

        items.forEach { item ->
            val selected = currentDestination?.isInHierarchy(item.route) == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    // 避免重复点击当前页
                    if (!selected) {
                        navController.navigate(item.route) {
                            // 清除目标页之上的栈（保持底部导航一致性）
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

// 数据模型
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Game : BottomNavItem("rules", "Game", Icons.Default.MusicNote) // rules → game 流程入口
    object Identifier : BottomNavItem("identifier", "Identifier", Icons.Default.Search)
    object Library : BottomNavItem("library", "Library", Icons.Default.LibraryBooks)
}

// 工具扩展
private fun NavDestination?.isInHierarchy(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true