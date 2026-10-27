package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.Channel
import com.example.tamilstream.ui.components.ChannelCard
import com.example.tamilstream.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTvScreen(
    channels: List<Channel>,
    currentPlayingChannelId: String?,
    activeCategory: String,
    searchQuery: String,
    viewMode: String, // "grid" or "list"
    showOnlyFavorites: Boolean,
    isRefreshing: Boolean = false,
    syncNotification: String? = null,
    onRefreshPlaylist: () -> Unit,
    onCategorySelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onPlayChannel: (Channel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddChannelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "News", "Entertainment", "Music", "Movies", "Religious", "Sports", "General")

    // Filter channels
    val filteredChannels = channels.filter { channel ->
        val matchesCategory = if (activeCategory == "All") true else channel.group.equals(activeCategory, ignoreCase = true)
        val matchesSearch = channel.cleanName.contains(searchQuery, ignoreCase = true) || channel.name.contains(searchQuery, ignoreCase = true)
        val matchesFav = if (showOnlyFavorites) channel.isFavorite else true
        matchesCategory && matchesSearch && matchesFav
    }

    Scaffold(
        modifier = modifier.testTag("live_tv_screen"),
        containerColor = ObsidianBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddChannelClick,
                containerColor = Amber500,
                contentColor = ObsidianBlack,
                modifier = Modifier.testTag("add_channel_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Channel")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("channel_search_input"),
                    placeholder = { Text("Filter channels...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber500,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Favorites Filter Toggle
                FilledIconToggleButton(
                    checked = showOnlyFavorites,
                    onCheckedChange = { onToggleFavoritesOnly() },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = LiveRed.copy(alpha = 0.2f),
                        checkedContentColor = LiveRed,
                        containerColor = DarkSurface,
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_favorites_toggle")
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites Only"
                    )
                }

                // Grid / List Toggle
                IconButton(
                    onClick = onToggleViewMode,
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(10.dp))
                        .testTag("toggle_view_mode_btn")
                ) {
                    Icon(
                        imageVector = if (viewMode == "grid") Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle View Mode",
                        tint = Amber500
                    )
                }

                // Refresh Playlist Button
                IconButton(
                    onClick = onRefreshPlaylist,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(10.dp))
                        .testTag("refresh_playlist_btn")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Amber500,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Playlist",
                            tint = Amber500
                        )
                    }
                }
            }

            // Sync Notification Banner
            if (syncNotification != null) {
                Surface(
                    color = Amber500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = syncNotification,
                            color = Amber500,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Category Chips Rail
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = activeCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber500,
                            selectedLabelColor = ObsidianBlack,
                            containerColor = DarkSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Amber500 else DarkBorder
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            // Channel count summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredChannels.size} Channels Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (showOnlyFavorites) {
                    Text(
                        text = "Filtered by Favorites",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber500
                    )
                }
            }

            // Channel List / Grid
            if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = "No channels",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Channels Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Try adjusting your search query or category filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = if (viewMode == "grid") GridCells.Fixed(2) else GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("channels_grid"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChannels) { channel ->
                        ChannelCard(
                            channel = channel,
                            isCurrentPlaying = channel.id == currentPlayingChannelId,
                            isCompact = viewMode == "list",
                            onPlay = { onPlayChannel(channel) },
                            onToggleFavorite = { onToggleFavorite(channel.id) }
                        )
                    }
                }
            }
        }
    }
}
