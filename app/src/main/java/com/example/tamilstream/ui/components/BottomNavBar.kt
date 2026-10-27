package com.example.tamilstream.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.tamilstream.ui.theme.*
import com.example.tamilstream.ui.viewmodel.Screen

data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(Screen.HOME, "Home", Icons.Default.Home, "nav_item_home"),
        NavItem(Screen.LIVE_TV, "Live TV", Icons.Default.LiveTv, "nav_item_live_tv"),
        NavItem(Screen.MOVIES, "Movies", Icons.Default.Movie, "nav_item_movies"),
        NavItem(Screen.EPISODES, "Episodes", Icons.Default.VideoLibrary, "nav_item_episodes"),
        NavItem(Screen.SUBSCRIPTIONS, "Premium", Icons.Default.WorkspacePremium, "nav_item_premium")
    )

    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Amber500,
                    selectedTextColor = Amber500,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Amber500.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
