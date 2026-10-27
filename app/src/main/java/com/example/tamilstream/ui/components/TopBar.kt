package com.example.tamilstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.User
import com.example.tamilstream.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    user: User,
    onSearchClick: () -> Unit,
    onAdminClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("main_top_app_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ObsidianBlack,
            titleContentColor = TextPrimary
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Amber500, Amber600)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Logo",
                        tint = ObsidianBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TAMIL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = " TV",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Amber500,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Text(
                        text = "LIVE IPTV & CINEMA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )
                    )
                }
            }
        },
        actions = {
            // Live Pulse Pill
            Surface(
                color = LiveRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = null,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                    Text(
                        text = "LIVE",
                        color = LiveRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search Icon
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("topbar_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPrimary
                )
            }

            // Admin Portal Icon
            IconButton(
                onClick = onAdminClick,
                modifier = Modifier.testTag("topbar_admin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Portal",
                    tint = if (user.isAdmin) Amber500 else TextSecondary
                )
            }

            // User Profile Icon
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("topbar_profile_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = Amber500.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (user.displayName.firstOrNull() ?: 'U').uppercase(),
                            color = Amber500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    )
}
