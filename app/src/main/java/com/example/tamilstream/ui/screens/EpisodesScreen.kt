package com.example.tamilstream.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamilstream.data.model.Episode
import com.example.tamilstream.data.model.Series
import com.example.tamilstream.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesScreen(
    seriesList: List<Series> = emptyList(),
    episodes: List<Episode> = emptyList(),
    onPlayEpisode: (Episode) -> Unit,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Published and enabled episodes
    val publishedEpisodes = remember(episodes) {
        episodes.filter { it.status.equals("Published", ignoreCase = true) && it.isEnabled }
    }

    // Combine explicit seriesList with any series names found in publishedEpisodes
    val allSeries = remember(seriesList, publishedEpisodes) {
        val existingTitles = seriesList.map { it.title.lowercase() }.toSet()
        val derivedFromEpisodes = publishedEpisodes.map { it.seriesName }.distinct()
            .filterNot { existingTitles.contains(it.lowercase()) }
            .map { name ->
                val sampleEp = publishedEpisodes.firstOrNull { it.seriesName.equals(name, ignoreCase = true) }
                Series(
                    id = "series_derived_${name.hashCode()}",
                    title = name,
                    mainThumbnailUrl = sampleEp?.thumbnailUrl ?: "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=800&auto=format&fit=crop&q=80",
                    description = "Popular Tamil serial $name with full episode streaming.",
                    status = "Published",
                    isEnabled = true
                )
            }
        (seriesList.filter { it.status.equals("Published", ignoreCase = true) && it.isEnabled } + derivedFromEpisodes)
            .sortedBy { it.title }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSeries by remember { mutableStateOf<Series?>(null) }

    val filteredSeries = remember(allSeries, searchQuery) {
        if (searchQuery.isBlank()) allSeries
        else allSeries.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    // If user clicked a Series, show Series Detail Page with its child episodes
    if (selectedSeries != null) {
        val series = selectedSeries!!
        BackHandler { selectedSeries = null }

        val childEpisodes = remember(series, publishedEpisodes) {
            publishedEpisodes.filter { ep ->
                ep.seriesId == series.id || ep.seriesName.equals(series.title, ignoreCase = true)
            }.sortedBy { it.episodeNumber }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBlack)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("series_detail_screen")
        ) {
            // Header with Back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedSeries = null },
                    modifier = Modifier.testTag("series_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to All Series",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Serial Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
            }

            // 1. Main Series Title at top
            Text(
                text = series.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.testTag("series_detail_main_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Amber500.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${childEpisodes.size} EPISODES",
                        color = Amber500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Main Series Thumbnail image below title
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("series_detail_main_thumbnail"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = series.mainThumbnailUrl,
                        contentDescription = series.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )

                    Surface(
                        color = ObsidianBlack.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Main Series Banner",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (series.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = series.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Individual Episodes List
            Text(
                text = "Episodes (${childEpisodes.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (childEpisodes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No episodes published for this series yet.", color = TextSecondary)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    childEpisodes.forEach { ep ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayEpisode(ep) }
                                .testTag("user_episode_item_${ep.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Episode Individual Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceElevated)
                                ) {
                                    AsyncImage(
                                        model = ep.thumbnailUrl,
                                        contentDescription = ep.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Amber500),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Episode",
                                            tint = ObsidianBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Surface(
                                        color = ObsidianBlack.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(topStart = 4.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text(
                                            text = "EP ${ep.episodeNumber}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Amber500,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Details
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = ep.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${ep.durationMinutes} min • ${if (ep.description.isNotBlank()) ep.description else "Full episode HD"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Main View: Grid / List of Series Cards
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBlack)
                .padding(16.dp)
                .testTag("episodes_series_main_screen")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tamil TV Serials",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Select a series to browse all published episodes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = Amber500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${filteredSeries.size} SERIES",
                        color = Amber500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search TV serials...", color = TextSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("series_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = Amber500,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (filteredSeries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Text("No serials found", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSeries, key = { it.id }) { s ->
                        val epCount = publishedEpisodes.count { ep ->
                            ep.seriesId == s.id || ep.seriesName.equals(s.title, ignoreCase = true)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable { selectedSeries = s }
                                .testTag("series_card_${s.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = s.mainThumbnailUrl,
                                    contentDescription = s.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                            )
                                        )
                                )

                                Surface(
                                    color = Amber500,
                                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "$epCount EPISODES",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianBlack,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = s.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = if (s.description.isNotBlank()) s.description else "Tap to view episodes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 11.sp
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
