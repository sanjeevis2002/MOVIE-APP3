package com.example.tamilstream.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamilstream.data.model.*
import com.example.tamilstream.data.repository.M3uPlaylistService
import com.example.tamilstream.ui.components.*
import com.example.tamilstream.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    channels: List<Channel>,
    movies: List<Movie>,
    movieCategories: List<String> = emptyList(),
    seriesList: List<Series> = emptyList(),
    episodes: List<Episode> = emptyList(),
    registeredUsers: List<User> = emptyList(),
    activityLogs: List<AdminActivityLog> = emptyList(),
    lastPlaybackDiagnostic: PlaybackDiagnostic? = null,
    onAddOrEditChannel: (Channel?) -> Unit,
    onDeleteChannel: (String) -> Unit,
    onAddMovie: (Movie) -> Unit = {},
    onAddOrUpdateMovie: (Movie) -> Unit = {},
    onDeleteMovie: (String) -> Unit = {},
    onToggleMoviePublish: (String) -> Unit = {},
    onToggleMovieFeatured: (String) -> Unit = {},
    onPlayMovie: (Movie) -> Unit = {},
    onAddMovieCategory: (String) -> Unit = {},
    onDeleteMovieCategory: (String) -> Unit = {},
    onAddOrUpdateSeries: (Series) -> Unit = {},
    onDeleteSeries: (String) -> Unit = {},
    onToggleSeriesPublish: (String) -> Unit = {},
    onAddOrUpdateEpisode: (Episode) -> Unit = {},
    onDeleteEpisode: (String) -> Unit = {},
    onToggleEpisodePublish: (String) -> Unit = {},
    onToggleEpisodeEnabled: (String) -> Unit = {},
    onPlayEpisode: (Episode) -> Unit = {},
    onSyncPlaylist: (String) -> Unit = {},
    onTestConnectivity: (String) -> Unit = {},
    onToggleUserStatus: (String) -> Unit = {},
    onToggleUserRole: (String) -> Unit = {},
    onDeleteUser: (String) -> Unit = {},
    adConfig: AdConfig = AdConfig(),
    onSaveAdConfig: (String, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _ -> },
    onToggleAdEnabled: () -> Unit = {},
    onToggleAdTopEnabled: () -> Unit = {},
    onToggleAdBottomEnabled: () -> Unit = {},
    onClearAdConfig: () -> Unit = {},
    isSyncing: Boolean = false,
    isAdSaving: Boolean = false,
    syncStatus: String? = null,
    // Manual UPI Payment & Premium Parameters
    plans: List<SubscriptionPlan> = emptyList(),
    manualUpiConfig: ManualUpiConfig = ManualUpiConfig(),
    manualPaymentRequests: List<ManualPaymentRequest> = emptyList(),
    isMasterPremiumEnabled: Boolean = true,
    onSaveManualUpiConfig: (ManualUpiConfig) -> Unit = {},
    onAcceptManualPayment: (String) -> Unit = {},
    onRejectManualPayment: (String, String) -> Unit = { _, _ -> },
    onGrantUserPremium: (String) -> Unit = {},
    onRevokeUserPremium: (String) -> Unit = {},
    onToggleMasterPremium: () -> Unit = {},
    onSavePlan: (SubscriptionPlan) -> Unit = {},
    onDeletePlan: (String) -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tabs: 0: Overview, 1: Users, 2: Channels, 3: Episodes, 4: Movies, 5: Diagnostics, 6: M3U Sync, 7: Settings, 8: Audit Logs, 9: Advertisement Management, 10: Payment Settings & Manual Approvals
    var adminTab by remember { mutableStateOf(0) }
    var selectedDiagnosticChannelId by remember { mutableStateOf(channels.firstOrNull()?.id ?: "") }
    var userSearchText by remember { mutableStateOf("") }

    var showPlanDialog by remember { mutableStateOf(false) }
    var planToEdit by remember { mutableStateOf<SubscriptionPlan?>(null) }

    // Series & Episodes Management State
    var showSeriesFormDialog by remember { mutableStateOf(false) }
    var seriesToEdit by remember { mutableStateOf<Series?>(null) }
    var seriesToDelete by remember { mutableStateOf<Series?>(null) }
    var expandedSeriesIds by remember { mutableStateOf(setOf<String>()) }
    var defaultSeriesIdForNewEpisode by remember { mutableStateOf("") }

    var showEpisodeFormDialog by remember { mutableStateOf(false) }
    var episodeToEdit by remember { mutableStateOf<Episode?>(null) }
    var episodeToDelete by remember { mutableStateOf<Episode?>(null) }
    var episodeToPreview by remember { mutableStateOf<Episode?>(null) }
    var episodeSearchText by remember { mutableStateOf("") }
    var episodeFilterStatus by remember { mutableStateOf("All") }

    // Movies Management State
    var showMovieFormDialog by remember { mutableStateOf(false) }
    var movieToEdit by remember { mutableStateOf<Movie?>(null) }
    var movieToDelete by remember { mutableStateOf<Movie?>(null) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var movieSearchText by remember { mutableStateOf("") }
    var movieFilterStatus by remember { mutableStateOf("All") }
    var movieFilterCategory by remember { mutableStateOf("All") }

    Column(
        modifier = modifier
            .testTag("admin_portal_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = Amber500
                )
                Text(
                    text = "Control Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            TextButton(
                onClick = onLogout,
                colors = ButtonDefaults.textButtonColors(contentColor = LiveRed),
                modifier = Modifier.testTag("admin_logout_btn")
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Exit", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = adminTab,
            containerColor = DarkSurface,
            contentColor = Amber500,
            edgePadding = 8.dp
        ) {
            Tab(selected = adminTab == 0, onClick = { adminTab = 0 }, text = { Text("Overview") })
            Tab(selected = adminTab == 1, onClick = { adminTab = 1 }, text = { Text("Users (${registeredUsers.size})") })
            Tab(selected = adminTab == 2, onClick = { adminTab = 2 }, text = { Text("Channels (${channels.size})") })
            Tab(selected = adminTab == 3, onClick = { adminTab = 3 }, text = { Text("Episodes (${episodes.size})") }, modifier = Modifier.testTag("admin_tab_episodes"))
            Tab(selected = adminTab == 4, onClick = { adminTab = 4 }, text = { Text("Movies (${movies.size})") }, modifier = Modifier.testTag("admin_tab_movies"))
            Tab(selected = adminTab == 5, onClick = { adminTab = 5 }, text = { Text("Diagnostics") })
            Tab(selected = adminTab == 6, onClick = { adminTab = 6 }, text = { Text("M3U Sync") })
            Tab(selected = adminTab == 7, onClick = { adminTab = 7 }, text = { Text("Settings") })
            Tab(selected = adminTab == 8, onClick = { adminTab = 8 }, text = { Text("Audit Logs") })
            Tab(selected = adminTab == 9, onClick = { adminTab = 9 }, text = { Text("Advertisement Management") }, modifier = Modifier.testTag("admin_tab_ads"))
            Tab(selected = adminTab == 10, onClick = { adminTab = 10 }, text = { Text("Premium & IMB Gateway") }, modifier = Modifier.testTag("admin_tab_imb"))
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (adminTab) {
            0 -> {
                // Tab 0: Dashboard Overview
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Dashboard Overview & Platform Metrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Users",
                                value = "${registeredUsers.size}",
                                subtitle = "${registeredUsers.count { it.isActive }} Active Accounts",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Live Streams",
                                value = "${channels.size}",
                                subtitle = "${channels.count { it.enabled }} Active Feeds",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "OTT Movies",
                                value = "${movies.size}",
                                subtitle = "Ready to Stream",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Episodes & Serials",
                                value = "${episodes.size}",
                                subtitle = "${episodes.count { it.status == "Published" }} Published",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Firebase Realtime Database Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                HorizontalDivider(color = DarkBorder)
                                Text("• Auth Provider: Firebase Email/Password & Username Resolution", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("• Security Schema: /users, /usernames, /channels, /app_settings", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("• Live M3U URL: https://iptv-org.github.io/iptv/languages/tam.m3u", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("• System Integrity: Operational", style = MaterialTheme.typography.bodySmall, color = LiveGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: User Management (Section 6)
                val filteredUsers = remember(registeredUsers, userSearchText) {
                    if (userSearchText.isBlank()) registeredUsers
                    else {
                        val query = userSearchText.trim().lowercase()
                        registeredUsers.filter {
                            it.displayName.lowercase().contains(query) ||
                            it.username.lowercase().contains(query) ||
                            it.email.lowercase().contains(query)
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registered Users (${filteredUsers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userSearchText,
                        onValueChange = { userSearchText = it },
                        label = { Text("Search by Name, Username or Email") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Amber500) },
                        trailingIcon = {
                            if (userSearchText.isNotEmpty()) {
                                IconButton(onClick = { userSearchText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_user_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUsers) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("admin_user_card_${u.uid}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = u.displayName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                if (u.isAdmin) {
                                                    Surface(color = Amber500.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                                        Text("ADMIN", color = Amber500, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                            Text(text = "@${u.username} • ${u.email}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(u.createdAt))
                                            Text(text = "Status: ${u.accountStatus.uppercase()} • Joined: $dateStr", style = MaterialTheme.typography.labelSmall, color = if (u.isActive) LiveGreen else LiveRed)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Toggle Status
                                            IconButton(
                                                onClick = { onToggleUserStatus(u.uid) },
                                                enabled = u.uid != "admin_root_master"
                                            ) {
                                                Icon(
                                                    imageVector = if (u.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                                    contentDescription = "Toggle Status",
                                                    tint = if (u.isActive) LiveRed else LiveGreen
                                                )
                                            }

                                            // Toggle Role
                                            IconButton(
                                                onClick = { onToggleUserRole(u.uid) },
                                                enabled = u.uid != "admin_root_master"
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AdminPanelSettings,
                                                    contentDescription = "Toggle Role",
                                                    tint = if (u.isAdmin) Amber500 else TextSecondary
                                                )
                                            }

                                            // Delete User
                                            IconButton(
                                                onClick = { onDeleteUser(u.uid) },
                                                enabled = u.uid != "admin_root_master"
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete User",
                                                    tint = LiveRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Channels Manager (Existing)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Manage IPTV Streams", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Button(
                        onClick = { onAddOrEditChannel(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_add_channel_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Channel")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(channels) { ch ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("admin_channel_row_${ch.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = ch.cleanName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "${ch.group} • ${ch.resolution} • ${if (ch.enabled) "Active" else "Disabled"}", style = MaterialTheme.typography.bodySmall, color = if (ch.enabled) LiveGreen else TextMuted)
                                    if (ch.fallbackUrl != null) {
                                        Text(text = "Backup stream configured", style = MaterialTheme.typography.labelSmall, color = Amber500)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { onAddOrEditChannel(ch) }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Amber500)
                                    }
                                    IconButton(onClick = { onDeleteChannel(ch.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Tab 3: Episodes Management
                val filteredEpisodes = remember(episodes, episodeSearchText, episodeFilterStatus) {
                    episodes.filter { ep ->
                        val matchesStatus = when (episodeFilterStatus) {
                            "Published" -> ep.status.equals("Published", ignoreCase = true)
                            "Draft" -> ep.status.equals("Draft", ignoreCase = true)
                            "Unpublished" -> ep.status.equals("Unpublished", ignoreCase = true)
                            else -> true
                        }
                        val matchesSearch = episodeSearchText.isBlank() ||
                                ep.title.contains(episodeSearchText, ignoreCase = true) ||
                                ep.seriesName.contains(episodeSearchText, ignoreCase = true) ||
                                ep.description.contains(episodeSearchText, ignoreCase = true)
                        matchesStatus && matchesSearch
                    }
                }

                // Tab 3: Series & Episodes Management
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with Add Series and Add Episode Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Series & Episodes Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${seriesList.size} Series • ${episodes.size} Episodes Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    seriesToEdit = null
                                    showSeriesFormDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_add_series_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Series", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    defaultSeriesIdForNewEpisode = ""
                                    episodeToEdit = null
                                    showEpisodeFormDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_add_episode_btn")
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp), tint = Amber500)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Episode", color = Amber500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Series Search Input
                    OutlinedTextField(
                        value = episodeSearchText,
                        onValueChange = { episodeSearchText = it },
                        placeholder = { Text("Search by series title or episode name...", color = TextSecondary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (episodeSearchText.isNotEmpty()) {
                                IconButton(onClick = { episodeSearchText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_series_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Render Series List
                    val filteredSeriesList = remember(seriesList, episodeSearchText, episodes) {
                        if (episodeSearchText.isBlank()) seriesList
                        else seriesList.filter { s ->
                            s.title.contains(episodeSearchText, ignoreCase = true) ||
                                    episodes.any { ep -> (ep.seriesId == s.id || ep.seriesName.equals(s.title, ignoreCase = true)) && ep.title.contains(episodeSearchText, ignoreCase = true) }
                        }
                    }

                    if (filteredSeriesList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tv, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                                Text("No series found", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Click 'Add Series' to create your first series (e.g., Bigg Boss).", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredSeriesList, key = { it.id }) { series ->
                                val childEps = episodes.filter { ep ->
                                    ep.seriesId == series.id || ep.seriesName.equals(series.title, ignoreCase = true)
                                }.sortedBy { it.episodeNumber }

                                val isExpanded = expandedSeriesIds.contains(series.id)

                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("admin_series_card_${series.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Main Series Banner & Details
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            // Main Series Thumbnail
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 100.dp, height = 75.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkSurfaceElevated)
                                            ) {
                                                AsyncImage(
                                                    model = series.mainThumbnailUrl,
                                                    contentDescription = series.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    color = Amber500,
                                                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                ) {
                                                    Text("MAIN THUMB", fontSize = 8.sp, color = ObsidianBlack, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }

                                            // Series Meta
                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = series.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "${childEps.size} Episodes • One Main Thumbnail",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Amber500,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = if (series.description.isNotBlank()) series.description else "No description provided",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))

                                        // Series Controls & Plus Button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Status Badge
                                            Surface(
                                                color = if (series.status == "Published") LiveGreen.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = series.status.uppercase(),
                                                    color = if (series.status == "Published") LiveGreen else Amber500,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            // Action Buttons: + Add Episode inside THIS series, Expand, Edit, Delete
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                // Specific + Add Episode Button for this series
                                                Button(
                                                    onClick = {
                                                        defaultSeriesIdForNewEpisode = series.id
                                                        episodeToEdit = null
                                                        showEpisodeFormDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.testTag("admin_series_plus_add_ep_${series.id}")
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Add Episode to Series", modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("+ Add Episode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                // Toggle Manage Episodes
                                                IconButton(
                                                    onClick = {
                                                        expandedSeriesIds = if (isExpanded) expandedSeriesIds - series.id else expandedSeriesIds + series.id
                                                    },
                                                    modifier = Modifier.size(32.dp).testTag("admin_manage_episodes_btn_${series.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = "Manage Episodes",
                                                        tint = TextPrimary
                                                    )
                                                }

                                                // Edit Series
                                                IconButton(
                                                    onClick = {
                                                        seriesToEdit = series
                                                        showSeriesFormDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp).testTag("admin_edit_series_btn_${series.id}")
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Series", tint = TextPrimary, modifier = Modifier.size(18.dp))
                                                }

                                                // Toggle Publish
                                                IconButton(
                                                    onClick = { onToggleSeriesPublish(series.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (series.status == "Published") Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = "Toggle Status",
                                                        tint = if (series.status == "Published") LiveGreen else TextMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                // Delete Series
                                                IconButton(
                                                    onClick = { seriesToDelete = series },
                                                    modifier = Modifier.size(32.dp).testTag("admin_delete_series_btn_${series.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete Series", tint = LiveRed, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }

                                        // Child Episodes Accordion
                                        AnimatedVisibility(visible = isExpanded) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Episodes inside \"${series.title}\" (${childEps.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Amber500)
                                                    Text("Individual Thumbnails & Video URLs", fontSize = 10.sp, color = TextSecondary)
                                                }

                                                if (childEps.isEmpty()) {
                                                    Text("No episodes added yet. Click '+ Add Episode' to add Episode 1.", fontSize = 12.sp, color = TextSecondary)
                                                } else {
                                                    childEps.forEach { ep ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().testTag("admin_child_ep_${ep.id}"),
                                                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                // Episode Individual Thumbnail
                                                                Box(
                                                                    modifier = Modifier.size(width = 65.dp, height = 45.dp).clip(RoundedCornerShape(4.dp)).background(ObsidianBlack)
                                                                ) {
                                                                    AsyncImage(model = ep.thumbnailUrl, contentDescription = ep.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                                                    Surface(color = ObsidianBlack.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.BottomEnd)) {
                                                                        Text("Ep ${ep.episodeNumber}", fontSize = 8.sp, color = Amber500, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                                                                    }
                                                                }

                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(ep.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                                                                    Text("Stream: ${ep.videoUrl}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
                                                                }

                                                                Row {
                                                                    IconButton(onClick = { episodeToPreview = ep }, modifier = Modifier.size(28.dp)) {
                                                                        Icon(Icons.Default.PlayCircle, contentDescription = "Preview", tint = Amber500, modifier = Modifier.size(16.dp))
                                                                    }
                                                                    IconButton(onClick = { episodeToEdit = ep; showEpisodeFormDialog = true }, modifier = Modifier.size(28.dp)) {
                                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                                                    }
                                                                    IconButton(onClick = { onToggleEpisodePublish(ep.id) }, modifier = Modifier.size(28.dp)) {
                                                                        Icon(if (ep.status == "Published") Icons.Default.VisibilityOff else Icons.Default.Publish, contentDescription = "Status", tint = if (ep.status == "Published") LiveGreen else TextMuted, modifier = Modifier.size(16.dp))
                                                                    }
                                                                    IconButton(onClick = { episodeToDelete = ep }, modifier = Modifier.size(28.dp)) {
                                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed, modifier = Modifier.size(16.dp))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Tab 4: Movies Management
                val categoriesList = if (movieCategories.isNotEmpty()) movieCategories else listOf("Action", "Drama", "Comedy", "Romance", "Thriller", "Family", "Crime", "Historical")

                val filteredMovies = remember(movies, movieSearchText, movieFilterStatus, movieFilterCategory) {
                    movies.filter { mv ->
                        val matchesStatus = when (movieFilterStatus) {
                            "Published" -> mv.status.equals("Published", ignoreCase = true)
                            "Draft" -> mv.status.equals("Draft", ignoreCase = true)
                            "Unpublished" -> mv.status.equals("Unpublished", ignoreCase = true)
                            else -> true
                        }
                        val matchesCategory = if (movieFilterCategory == "All") true else mv.category.equals(movieFilterCategory, ignoreCase = true) || mv.genre.any { it.equals(movieFilterCategory, ignoreCase = true) }
                        val matchesSearch = movieSearchText.isBlank() ||
                                mv.title.contains(movieSearchText, ignoreCase = true) ||
                                mv.description.contains(movieSearchText, ignoreCase = true) ||
                                mv.director.contains(movieSearchText, ignoreCase = true) ||
                                mv.cast.any { it.contains(movieSearchText, ignoreCase = true) }
                        matchesStatus && matchesCategory && matchesSearch
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header with Add Movie & Manage Categories Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Movies Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${movies.size} Total Movies • ${movies.count { it.status == "Published" && it.isEnabled }} Live in Catalog",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCategoryManagerDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500),
                                modifier = Modifier.testTag("admin_manage_movie_categories_btn")
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Categories", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    movieToEdit = null
                                    showMovieFormDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_add_movie_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Movie", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Movie", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = movieSearchText,
                        onValueChange = { movieSearchText = it },
                        placeholder = { Text("Search by title, director, cast or summary...", color = TextSecondary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (movieSearchText.isNotEmpty()) {
                                IconButton(onClick = { movieSearchText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_movie_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Filters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Published", "Draft", "Unpublished").forEach { st ->
                            val isSelected = movieFilterStatus == st
                            FilterChip(
                                selected = isSelected,
                                onClick = { movieFilterStatus = st },
                                label = { Text(st, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber500,
                                    selectedLabelColor = ObsidianBlack,
                                    containerColor = DarkSurface,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Movies List Table
                    if (filteredMovies.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No movies match the selected criteria.", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredMovies, key = { it.id }) { mv ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_movie_row_${mv.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Poster Thumbnail
                                        Box(
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(84.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkSurfaceElevated)
                                        ) {
                                            AsyncImage(
                                                model = mv.poster,
                                                contentDescription = mv.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        // Movie Info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = mv.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (mv.isFeatured) {
                                                    Surface(
                                                        color = Amber500.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "FEATURED",
                                                            color = Amber500,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "${mv.releaseYear} • ${mv.language} • ${mv.category} • ${mv.duration} min",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = when (mv.status.lowercase()) {
                                                        "published" -> LiveGreen.copy(alpha = 0.2f)
                                                        "draft" -> Amber500.copy(alpha = 0.2f)
                                                        else -> LiveRed.copy(alpha = 0.2f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = mv.status.uppercase(),
                                                        color = when (mv.status.lowercase()) {
                                                            "published" -> LiveGreen
                                                            "draft" -> Amber500
                                                            else -> LiveRed
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                if (mv.isPremium) {
                                                    Surface(
                                                        color = Purple500.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "VIP ONLY",
                                                            color = Amber400,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Actions Row
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Preview Stream
                                            IconButton(
                                                onClick = { onPlayMovie(mv) },
                                                modifier = Modifier.testTag("admin_preview_movie_${mv.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Preview Stream",
                                                    tint = Amber500
                                                )
                                            }

                                            // Edit
                                            IconButton(
                                                onClick = {
                                                    movieToEdit = mv
                                                    showMovieFormDialog = true
                                                },
                                                modifier = Modifier.testTag("admin_edit_movie_${mv.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = TextPrimary
                                                )
                                            }

                                            // Toggle Publish
                                            IconButton(
                                                onClick = { onToggleMoviePublish(mv.id) },
                                                modifier = Modifier.testTag("admin_toggle_movie_publish_${mv.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (mv.status.equals("Published", ignoreCase = true)) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle Status",
                                                    tint = if (mv.status.equals("Published", ignoreCase = true)) LiveGreen else TextMuted
                                                )
                                            }

                                            // Delete
                                            IconButton(
                                                onClick = { movieToDelete = mv },
                                                modifier = Modifier.testTag("admin_delete_movie_${mv.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = LiveRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // Tab 5: Stream Diagnostics Tool (Existing Section 7)
                val targetChannel = channels.find { it.id == selectedDiagnosticChannelId } ?: channels.firstOrNull()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(text = "Stream Diagnostic & Health Monitor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Select Channel to Diagnose:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                            Text(text = targetChannel?.cleanName ?: "Select Channel", color = TextPrimary, maxLines = 1)
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            channels.forEach { ch ->
                                                DropdownMenuItem(text = { Text(ch.cleanName) }, onClick = {
                                                    selectedDiagnosticChannelId = ch.id
                                                    expanded = false
                                                })
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = { targetChannel?.let { onTestConnectivity(it.id) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Probe Stream", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (targetChannel != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = targetChannel.cleanName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Amber500)
                                    Text("Primary Stream URL: ${targetChannel.url}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text("Probe Result: ${targetChannel.lastCheckStatus ?: "Not probed yet."}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LiveGreen)
                                }
                            }
                        }
                    }
                }
            }

            6 -> {
                // Tab 6: M3U Playlist Sync
                var playlistUrlInput by remember { mutableStateOf(M3uPlaylistService.DEFAULT_M3U_URL) }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "M3U Playlist Source & Synchronization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = playlistUrlInput,
                        onValueChange = { playlistUrlInput = it },
                        label = { Text("M3U Playlist URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { onSyncPlaylist(playlistUrlInput) },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSyncing) Text("Fetching & Parsing M3U...")
                        else Text("Synchronize Playlist Now", fontWeight = FontWeight.Bold)
                    }
                    if (syncStatus != null) {
                        Text(text = syncStatus, color = Amber500, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                }
            }

            7 -> {
                // Tab 7: App Settings
                var appNameInput by remember { mutableStateOf("LIVE TAMIL TV") }
                var maintenanceMode by remember { mutableStateOf(false) }
                var guestModeEnabled by remember { mutableStateOf(true) }

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Platform & Application Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(value = appNameInput, onValueChange = { appNameInput = it }, label = { Text("App Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Enable Guest Browsing", color = TextPrimary)
                        Switch(checked = guestModeEnabled, onCheckedChange = { guestModeEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Amber500))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Maintenance Mode", color = TextPrimary)
                        Switch(checked = maintenanceMode, onCheckedChange = { maintenanceMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = LiveRed))
                    }
                }
            }

            8 -> {
                // Tab 8: Audit Activity Logs (Section 6)
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Administrative Activity Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activityLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = log.action, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Amber500)
                                        val timeStr = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                        Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Text(text = "Admin: ${log.adminEmail} • Target: ${log.targetId}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(text = log.details, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            9 -> {
                // Tab 9: Advertisement Management
                AdvertisementManagementSection(
                    adConfig = adConfig,
                    isAdSaving = isAdSaving,
                    onSaveAdConfig = onSaveAdConfig,
                    onToggleAdEnabled = onToggleAdEnabled,
                    onToggleAdTopEnabled = onToggleAdTopEnabled,
                    onToggleAdBottomEnabled = onToggleAdBottomEnabled,
                    onClearAdConfig = onClearAdConfig
                )
            }
            10 -> {
                // Tab 10: Manual UPI Payment Settings & Approval Dashboard
                var upiIdInput by remember(manualUpiConfig) { mutableStateOf(manualUpiConfig.paytmUpiId) }
                var qrUrlInput by remember(manualUpiConfig) { mutableStateOf(manualUpiConfig.qrCodeUrl) }
                var instructionsInput by remember(manualUpiConfig) { mutableStateOf(manualUpiConfig.instructions) }
                var upiEnabled by remember(manualUpiConfig) { mutableStateOf(manualUpiConfig.enabled) }
                var paymentStatusFilter by remember { mutableStateOf("All") }
                var paymentSearchQuery by remember { mutableStateOf("") }
                var saveSuccessMsg by remember { mutableStateOf<String?>(null) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Master Premium System Toggle
                    item {
                        Surface(
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(Icons.Default.WorkspacePremium, contentDescription = "VIP", tint = Amber500)
                                        Column {
                                            Text("Master Premium System Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("Globally enable or disable Premium subscriptions", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                    }
                                    Switch(
                                        checked = isMasterPremiumEnabled,
                                        onCheckedChange = { onToggleMasterPremium() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Amber500)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Manual UPI Payment Settings
                    item {
                        Surface(
                            color = DarkSurface,
                            border = BorderStroke(1.dp, Amber500.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(Icons.Default.QrCode2, contentDescription = "UPI", tint = Amber500)
                                        Column {
                                            Text("Manual UPI Payment Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("Configure Paytm UPI ID, QR code, and payment instructions", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                    }
                                    Switch(
                                        checked = upiEnabled,
                                        onCheckedChange = { upiEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Amber500)
                                    )
                                }

                                OutlinedTextField(
                                    value = upiIdInput,
                                    onValueChange = { upiIdInput = it },
                                    label = { Text("Paytm UPI ID *") },
                                    placeholder = { Text("merchant.paytm@paytm") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_upi_id_input")
                                )

                                OutlinedTextField(
                                    value = qrUrlInput,
                                    onValueChange = { qrUrlInput = it },
                                    label = { Text("UPI QR Code Image URL *") },
                                    placeholder = { Text("https://example.com/qr.jpg") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_qr_url_input")
                                )

                                if (qrUrlInput.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = qrUrlInput,
                                            contentDescription = "QR Preview",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = instructionsInput,
                                    onValueChange = { instructionsInput = it },
                                    label = { Text("Payment Instructions for Users") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (saveSuccessMsg != null) {
                                    Text(text = saveSuccessMsg!!, color = LiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val updated = ManualUpiConfig(
                                            enabled = upiEnabled,
                                            paytmUpiId = upiIdInput.trim(),
                                            qrCodeUrl = qrUrlInput.trim(),
                                            instructions = instructionsInput.trim()
                                        )
                                        onSaveManualUpiConfig(updated)
                                        saveSuccessMsg = "Manual UPI configuration saved successfully!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_save_upi_config_btn")
                                ) {
                                    Text("Save UPI Configuration", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3. Manual Payments Approval Dashboard
                    item {
                        Surface(
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Manual Payments Approval Dashboard (${manualPaymentRequests.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("All", "PENDING", "ACCEPTED", "REJECTED").forEach { st ->
                                        FilterChip(
                                            selected = paymentStatusFilter.equals(st, ignoreCase = true),
                                            onClick = { paymentStatusFilter = st },
                                            label = { Text(st, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Amber500,
                                                selectedLabelColor = ObsidianBlack,
                                                containerColor = DarkSurfaceElevated,
                                                labelColor = TextSecondary
                                            )
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = paymentSearchQuery,
                                    onValueChange = { paymentSearchQuery = it },
                                    label = { Text("Search by User Name, Email or UTR ID") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Amber500) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val filteredRequests = remember(manualPaymentRequests, paymentStatusFilter, paymentSearchQuery) {
                                    manualPaymentRequests.filter { req ->
                                        val matchesStatus = paymentStatusFilter.equals("All", ignoreCase = true) ||
                                                req.status.equals(paymentStatusFilter, ignoreCase = true)
                                        val matchesSearch = paymentSearchQuery.isBlank() ||
                                                req.userName.contains(paymentSearchQuery, ignoreCase = true) ||
                                                req.userEmail.contains(paymentSearchQuery, ignoreCase = true) ||
                                                req.paymentIdentifier.contains(paymentSearchQuery, ignoreCase = true) ||
                                                req.planName.contains(paymentSearchQuery, ignoreCase = true)
                                        matchesStatus && matchesSearch
                                    }
                                }

                                if (filteredRequests.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No payment requests found matching filter.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        filteredRequests.forEach { req ->
                                            Surface(
                                                color = DarkSurfaceElevated,
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(0.5.dp, when(req.status) {
                                                    "ACCEPTED" -> LiveGreen.copy(alpha = 0.5f)
                                                    "REJECTED" -> LiveRed.copy(alpha = 0.5f)
                                                    else -> Amber500.copy(alpha = 0.5f)
                                                }),
                                                modifier = Modifier.fillMaxWidth().testTag("admin_payment_req_${req.requestId}")
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(req.userName, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                                            Text(req.userEmail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                        }
                                                        Surface(
                                                            color = when(req.status) {
                                                                "ACCEPTED" -> LiveGreen.copy(alpha = 0.2f)
                                                                "REJECTED" -> LiveRed.copy(alpha = 0.2f)
                                                                else -> Amber500.copy(alpha = 0.2f)
                                                            },
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = req.status,
                                                                color = when(req.status) {
                                                                    "ACCEPTED" -> LiveGreen
                                                                    "REJECTED" -> LiveRed
                                                                    else -> Amber500
                                                                },
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = "Plan: ${req.planName} • Amount: ₹${req.amountInr} INR (${req.durationDays} Days)",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Amber500
                                                    )

                                                    Text(
                                                        text = "UTR / Transaction ID: ${req.paymentIdentifier}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )

                                                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(req.submittedAt))
                                                    Text("Submitted At: $dateStr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                                                    if (req.screenshotUrl.isNotBlank()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(120.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(ObsidianBlack),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            AsyncImage(
                                                                model = req.screenshotUrl,
                                                                contentDescription = "Payment Proof",
                                                                contentScale = ContentScale.Fit,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }

                                                    if (req.status == "PENDING") {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            Button(
                                                                onClick = { onAcceptManualPayment(req.requestId) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = LiveGreen, contentColor = ObsidianBlack),
                                                                modifier = Modifier.weight(1f).testTag("accept_payment_btn_${req.requestId}")
                                                            ) {
                                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Accept & Activate VIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            }

                                                            OutlinedButton(
                                                                onClick = { onRejectManualPayment(req.requestId, "Invalid UTR or screenshot mismatch.") },
                                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                                                                modifier = Modifier.weight(1f).testTag("reject_payment_btn_${req.requestId}")
                                                            ) {
                                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            }
                                                        }
                                                    } else if (req.rejectionReason != null) {
                                                        Text("Rejection Reason: ${req.rejectionReason}", color = LiveRed, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. VIP / Premium Plans Management
                    item {
                        Surface(
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("VIP / Premium Plans Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Configure pricing, validity duration, badges, and activation status", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Button(
                                        onClick = {
                                            planToEdit = null
                                            showPlanDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                                        modifier = Modifier.testTag("admin_create_plan_btn")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Plan", fontWeight = FontWeight.Bold)
                                    }
                                }

                                plans.sortedBy { it.displayOrder }.forEach { plan ->
                                    Surface(
                                        color = DarkSurfaceElevated,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(0.5.dp, if (plan.isActive) DarkBorder else LiveRed.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth().testTag("admin_plan_row_${plan.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(plan.name, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                                    if (plan.badge != null) {
                                                        Surface(color = Amber500, shape = RoundedCornerShape(8.dp)) {
                                                            Text(plan.badge!!, color = ObsidianBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                        }
                                                    }
                                                }
                                                Surface(
                                                    color = if (plan.isActive) LiveGreen.copy(alpha = 0.15f) else LiveRed.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = if (plan.isActive) "ACTIVE" else "INACTIVE",
                                                        color = if (plan.isActive) LiveGreen else LiveRed,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text("₹${plan.priceInr} INR • Validity: ${plan.durationDays} Days (${plan.billingPeriodLabel})", style = MaterialTheme.typography.bodySmall, color = Amber500)
                                            Text(plan.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Featured: ${if (plan.isFeatured) "Yes" else "No"} • Features: ${plan.features.size}", style = MaterialTheme.typography.labelSmall, color = TextMuted)

                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    IconButton(
                                                        onClick = {
                                                            val updated = plan.copy(isActive = !plan.isActive)
                                                            onSavePlan(updated)
                                                        },
                                                        modifier = Modifier.size(32.dp).testTag("admin_toggle_plan_${plan.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = if (plan.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                                            contentDescription = "Toggle Status",
                                                            tint = if (plan.isActive) LiveRed else LiveGreen,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            planToEdit = plan
                                                            showPlanDialog = true
                                                        },
                                                        modifier = Modifier.size(32.dp).testTag("admin_edit_plan_btn_${plan.id}")
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Amber500, modifier = Modifier.size(18.dp))
                                                    }

                                                    IconButton(
                                                        onClick = { onDeletePlan(plan.id) },
                                                        modifier = Modifier.size(32.dp).testTag("admin_delete_plan_btn_${plan.id}")
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Registered Premium Users
                    item {
                        Surface(
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Registered Users & Premium Entitlements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                                registeredUsers.forEach { user ->
                                    Surface(
                                        color = DarkSurfaceElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(user.displayName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text("${user.email} (${user.role})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                Text(
                                                    text = if (user.isPremium) "VIP Premium Active" else "Free Account",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (user.isPremium) LiveGreen else TextMuted
                                                )
                                            }

                                            if (user.isPremium) {
                                                TextButton(
                                                    onClick = { onRevokeUserPremium(user.id) },
                                                    colors = ButtonDefaults.textButtonColors(contentColor = LiveRed)
                                                ) {
                                                    Text("Revoke VIP")
                                                }
                                            } else {
                                                Button(
                                                    onClick = { onGrantUserPremium(user.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack)
                                                ) {
                                                    Text("Grant VIP")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Series Modals & Dialogs
        if (showSeriesFormDialog) {
            EditSeriesDialog(
                seriesToEdit = seriesToEdit,
                onDismiss = {
                    showSeriesFormDialog = false
                    seriesToEdit = null
                },
                onSave = { s ->
                    onAddOrUpdateSeries(s)
                    showSeriesFormDialog = false
                    seriesToEdit = null
                }
            )
        }

        if (seriesToDelete != null) {
            SeriesDeleteConfirmDialog(
                series = seriesToDelete!!,
                onDismiss = { seriesToDelete = null },
                onConfirmDelete = {
                    onDeleteSeries(seriesToDelete!!.id)
                    seriesToDelete = null
                }
            )
        }

        // Episodes Modals & Dialogs
        if (showEpisodeFormDialog) {
            EpisodeFormDialog(
                episodeToEdit = episodeToEdit,
                seriesList = seriesList,
                defaultSeriesId = defaultSeriesIdForNewEpisode,
                onDismiss = {
                    showEpisodeFormDialog = false
                    episodeToEdit = null
                },
                onSave = { ep ->
                    onAddOrUpdateEpisode(ep)
                    showEpisodeFormDialog = false
                    episodeToEdit = null
                }
            )
        }

        if (episodeToDelete != null) {
            EpisodeDeleteConfirmDialog(
                episode = episodeToDelete!!,
                onDismiss = { episodeToDelete = null },
                onConfirmDelete = {
                    onDeleteEpisode(episodeToDelete!!.id)
                    episodeToDelete = null
                }
            )
        }

        if (showPlanDialog) {
            EditSubscriptionPlanDialog(
                planToEdit = planToEdit,
                onDismiss = {
                    showPlanDialog = false
                    planToEdit = null
                },
                onSave = { plan ->
                    onSavePlan(plan)
                    showPlanDialog = false
                    planToEdit = null
                }
            )
        }

        if (episodeToPreview != null) {
            EpisodePreviewDialog(
                episode = episodeToPreview!!,
                onDismiss = { episodeToPreview = null },
                onPlayInPlayer = {
                    val ep = episodeToPreview!!
                    episodeToPreview = null
                    onPlayEpisode(ep)
                }
            )
        }

        // Movies Modals & Dialogs
        if (showMovieFormDialog) {
            EditMovieDialog(
                movieToEdit = movieToEdit,
                availableCategories = if (movieCategories.isNotEmpty()) movieCategories else listOf("Action", "Drama", "Comedy", "Romance", "Thriller", "Family", "Crime", "Historical"),
                onDismiss = {
                    showMovieFormDialog = false
                    movieToEdit = null
                },
                onSave = { mv ->
                    onAddOrUpdateMovie(mv)
                    showMovieFormDialog = false
                    movieToEdit = null
                }
            )
        }

        if (movieToDelete != null) {
            MovieDeleteConfirmDialog(
                movie = movieToDelete!!,
                onDismiss = { movieToDelete = null },
                onConfirmDelete = {
                    onDeleteMovie(movieToDelete!!.id)
                    movieToDelete = null
                }
            )
        }

        if (showCategoryManagerDialog) {
            ManageMovieCategoriesDialog(
                categories = if (movieCategories.isNotEmpty()) movieCategories else listOf("Action", "Drama", "Comedy", "Romance", "Thriller", "Family", "Crime", "Historical"),
                onDismiss = { showCategoryManagerDialog = false },
                onAddCategory = { onAddMovieCategory(it) },
                onDeleteCategory = { onDeleteMovieCategory(it) }
            )
        }
    }
}


@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
            width = 0.5.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Amber500
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMovieDialog(
    movieToEdit: Movie?,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Movie) -> Unit
) {
    var title by remember { mutableStateOf(movieToEdit?.title ?: "") }
    var posterUrl by remember { mutableStateOf(movieToEdit?.poster ?: "") }
    var videoUrl by remember { mutableStateOf(movieToEdit?.videoUrl ?: "") }
    var description by remember { mutableStateOf(movieToEdit?.description ?: "") }
    var releaseYear by remember { mutableStateOf(movieToEdit?.releaseYear?.toString() ?: "2024") }
    var category by remember { mutableStateOf(movieToEdit?.category ?: availableCategories.firstOrNull() ?: "Action") }
    var language by remember { mutableStateOf(movieToEdit?.language ?: "Tamil") }
    var duration by remember { mutableStateOf(movieToEdit?.duration?.toString() ?: "140") }
    var isFeatured by remember { mutableStateOf(movieToEdit?.isFeatured ?: false) }
    var isVipOnly by remember { mutableStateOf(movieToEdit?.isPremium ?: false) }
    var status by remember { mutableStateOf(movieToEdit?.status ?: "Published") }

    val isVideoUrlValid = videoUrl.isBlank() || videoUrl.startsWith("http://", ignoreCase = true) || videoUrl.startsWith("https://", ignoreCase = true)
    val canSave = title.isNotBlank() && videoUrl.isNotBlank() && isVideoUrlValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (movieToEdit == null) "Add New Movie" else "Edit Movie",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Poster Preview
                if (posterUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = "Poster Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = ObsidianBlack.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                        ) {
                            Text("Poster Preview", fontSize = 10.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Movie Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("movie_input_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = posterUrl,
                    onValueChange = { posterUrl = it },
                    label = { Text("Poster Image URL") },
                    placeholder = { Text("https://example.com/poster.jpg") },
                    modifier = Modifier.fillMaxWidth().testTag("movie_input_poster"),
                    singleLine = true
                )

                val supportingTextLambda: @Composable (() -> Unit)? = if (!isVideoUrlValid) {
                    { Text("URL must begin with http:// or https://", color = LiveRed, fontSize = 11.sp) }
                } else null

                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Video Stream URL * (HLS/MP4)") },
                    placeholder = { Text("https://example.com/movie.m3u8") },
                    isError = !isVideoUrlValid,
                    supportingText = supportingTextLambda,
                    modifier = Modifier.fillMaxWidth().testTag("movie_input_videourl"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Movie Synopsis / Description") },
                    modifier = Modifier.fillMaxWidth().testTag("movie_input_description"),
                    minLines = 2,
                    maxLines = 4
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f).testTag("movie_input_category"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = releaseYear,
                        onValueChange = { releaseYear = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f).testTag("movie_input_year"),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        label = { Text("Language") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (Mins)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Category Chips Selector
                Text("Select Category:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber500.copy(alpha = 0.2f),
                                selectedLabelColor = Amber500
                            )
                        )
                    }
                }

                // Status Choice Chips
                Text("Publishing Status:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Published", "Draft", "Unpublished").forEach { st ->
                        FilterChip(
                            selected = status.equals(st, ignoreCase = true),
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (st == "Published") LiveGreen.copy(alpha = 0.2f) else Amber500.copy(alpha = 0.2f),
                                selectedLabelColor = if (st == "Published") LiveGreen else Amber500
                            )
                        )
                    }
                }

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Movie", color = TextPrimary)
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Amber500)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VIP Members Only", color = TextPrimary)
                    Switch(
                        checked = isVipOnly,
                        onCheckedChange = { isVipOnly = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Amber500)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMovie = Movie(
                        id = movieToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title.trim(),
                        poster = posterUrl.trim(),
                        thumbnail = posterUrl.trim(),
                        backdrop = posterUrl.trim(),
                        description = description.trim(),
                        synopsis = description.trim(),
                        category = category.trim(),
                        releaseYear = releaseYear.toIntOrNull() ?: 2024,
                        language = language.trim(),
                        duration = duration.toIntOrNull() ?: 140,
                        videoUrl = videoUrl.trim(),
                        status = status,
                        isEnabled = status.equals("Published", ignoreCase = true),
                        isFeatured = isFeatured,
                        isPremium = isVipOnly,
                        createdAt = movieToEdit?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(finalMovie)
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("save_movie_button")
            ) {
                Text(if (movieToEdit == null) "Create Movie" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun MovieDeleteConfirmDialog(
    movie: Movie,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Movie", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Text("Are you sure you want to delete \"${movie.title}\"? This action cannot be undone.", color = TextSecondary)
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = TextPrimary),
                modifier = Modifier.testTag("confirm_delete_movie_button")
            ) {
                Text("Delete Permanently", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun ManageMovieCategoriesDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Movie Categories", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("New Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                onAddCategory(newCatName.trim())
                                newCatName = ""
                            }
                        },
                        enabled = newCatName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("Existing Categories:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat, color = TextPrimary, fontWeight = FontWeight.Medium)
                                IconButton(
                                    onClick = { onDeleteCategory(cat) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementManagementSection(
    adConfig: AdConfig,
    isAdSaving: Boolean = false,
    onSaveAdConfig: (String, Boolean, Boolean, Boolean) -> Unit,
    onToggleAdEnabled: () -> Unit,
    onToggleAdTopEnabled: () -> Unit,
    onToggleAdBottomEnabled: () -> Unit,
    onClearAdConfig: () -> Unit
) {
    var htmlInput by remember(adConfig.htmlContent) { mutableStateOf(adConfig.htmlContent) }
    var adEnabled by remember(adConfig.enabled) { mutableStateOf(adConfig.enabled) }
    var topEnabled by remember(adConfig.topEnabled) { mutableStateOf(adConfig.topEnabled) }
    var bottomEnabled by remember(adConfig.bottomEnabled) { mutableStateOf(adConfig.bottomEnabled) }
    var showPreview by remember { mutableStateOf(true) }
    var showClearDialog by remember { mutableStateOf(false) }
    var previewRefreshKey by remember { mutableStateOf(0) }
    var adScriptLoaded by remember { mutableStateOf(false) }
    var adScriptError by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("ad_management_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Global Advertisement System",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Centralized HTML ad provider snippet published across Top Header and Bottom Footer placements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Success / Error Message Banner
        if (statusMessage != null) {
            Surface(
                color = LiveGreen.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, LiveGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_save_success_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = LiveGreen)
                        Text(statusMessage!!, color = LiveGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LiveGreen, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (errorMessage != null) {
            Surface(
                color = LiveRed.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, LiveRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = LiveRed)
                        Text(errorMessage!!, color = LiveRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LiveRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val statusText = when {
                            !adConfig.enabled -> "Master Disabled"
                            adConfig.htmlContent.isBlank() -> "Empty Code"
                            else -> "Active v${adConfig.version}"
                        }
                        val statusColor = when {
                            !adConfig.enabled -> LiveRed
                            adConfig.htmlContent.isBlank() -> TextMuted
                            else -> LiveGreen
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, statusColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusColor)
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Diagnostic Script Status Badge
                        val scriptBadgeText = if (adScriptLoaded) "Script Initialized" else "Script Pending"
                        val scriptBadgeColor = if (adScriptLoaded) LiveGreen else Amber500
                        Surface(
                            color = scriptBadgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, scriptBadgeColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = scriptBadgeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = scriptBadgeColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Master Ad Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (adEnabled) "Master On" else "Master Off",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (adEnabled) LiveGreen else TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = adEnabled,
                            onCheckedChange = {
                                adEnabled = it
                                onToggleAdEnabled()
                                statusMessage = if (it) "Master advertisement system enabled!" else "Master advertisement system disabled."
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = LiveGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceElevated
                            ),
                            modifier = Modifier.testTag("ad_enable_switch")
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Independent Placement Controls
                Text("Independent Placement Controls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Amber500)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Placement A — Header Advertisement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Positioned immediately below Top Header bar", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = topEnabled,
                        onCheckedChange = {
                            topEnabled = it
                            onToggleAdTopEnabled()
                            statusMessage = if (it) "Header advertisement placement enabled." else "Header advertisement placement disabled."
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = LiveGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.testTag("ad_top_enable_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Placement B — Footer Advertisement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Positioned immediately above Bottom Navigation bar", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = bottomEnabled,
                        onCheckedChange = {
                            bottomEnabled = it
                            onToggleAdBottomEnabled()
                            statusMessage = if (it) "Footer advertisement placement enabled." else "Footer advertisement placement disabled."
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = LiveGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.testTag("ad_bottom_enable_switch")
                    )
                }

                // Metadata info
                val dateStr = remember(adConfig.updatedAt) {
                    SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(adConfig.updatedAt))
                }
                Text(
                    text = "Last Published: $dateStr • Published By: ${adConfig.updatedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // HTML Code Editor Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Advertisement HTML Provider Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = htmlInput,
                    onValueChange = { htmlInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 300.dp)
                        .testTag("ad_html_textarea"),
                    placeholder = {
                        Text("Paste complete Adsterra advertisement script / iframe HTML code here...", color = TextMuted)
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber500,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (htmlInput.trim().isEmpty() && adEnabled) {
                                errorMessage = "Please paste your Adsterra advertisement HTML code or disable ads."
                            } else {
                                errorMessage = null
                                onSaveAdConfig(htmlInput, adEnabled, topEnabled, bottomEnabled)
                                previewRefreshKey++
                                statusMessage = "Advertisement configuration saved and published successfully!"
                            }
                        },
                        enabled = !isAdSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_ad_button")
                    ) {
                        if (isAdSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saving Ad...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Publish Ad", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showPreview = !showPreview },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_ad_button")
                    ) {
                        Icon(
                            if (showPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showPreview) "Hide Preview" else "Preview", color = TextPrimary)
                    }

                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_ad_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = LiveRed)
                    }
                }
            }
        }

        // Live Preview Section
        if (showPreview) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Preview, contentDescription = "Preview", tint = Amber500)
                            Text(
                                text = "Live Placement Previews",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Refresh Preview Button
                        IconButton(
                            onClick = { previewRefreshKey++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Preview", tint = Amber500)
                        }
                    }

                    if (htmlInput.isBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No advertisement HTML code provided.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Placement A (Header Preview):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            AdBannerView(
                                adConfig = adConfig,
                                isPreviewMode = true,
                                customHtmlCode = htmlInput,
                                placementTag = "preview_header_ad",
                                refreshKey = previewRefreshKey,
                                onAdStatusChanged = { isLoaded, err ->
                                    adScriptLoaded = isLoaded
                                    adScriptError = err
                                }
                            )

                            Text("Placement B (Footer Preview):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            AdBannerView(
                                adConfig = adConfig,
                                isPreviewMode = true,
                                customHtmlCode = htmlInput,
                                placementTag = "preview_footer_ad",
                                refreshKey = previewRefreshKey
                            )
                        }
                    }
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear Advertisement Code", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Text(
                        "Are you sure you want to clear all advertisement HTML code and disable ads? This will immediately remove ads from both Header and Footer placements.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            htmlInput = ""
                            adEnabled = false
                            topEnabled = false
                            bottomEnabled = false
                            onClearAdConfig()
                            showClearDialog = false
                            statusMessage = "Advertisement code cleared and disabled."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = TextPrimary),
                        modifier = Modifier.testTag("confirm_clear_ad_button")
                    ) {
                        Text("Clear Code", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}



