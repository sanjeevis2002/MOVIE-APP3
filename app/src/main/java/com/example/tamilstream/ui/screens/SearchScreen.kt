package com.example.tamilstream.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tamilstream.data.model.Channel
import com.example.tamilstream.data.model.Movie
import com.example.tamilstream.ui.components.ChannelCard
import com.example.tamilstream.ui.components.MovieCard
import com.example.tamilstream.ui.theme.*

@Composable
fun SearchScreen(
    searchQuery: String,
    channels: List<Channel>,
    movies: List<Movie>,
    watchlistIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onPlayChannel: (Channel) -> Unit,
    onSelectMovie: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    onToggleFavoriteChannel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchTab by remember { mutableStateOf(0) } // 0: All, 1: Live TV, 2: Movies

    val matchingChannels = if (searchQuery.isBlank()) emptyList() else channels.filter {
        it.cleanName.contains(searchQuery, ignoreCase = true) || it.group.contains(searchQuery, ignoreCase = true)
    }

    val matchingMovies = if (searchQuery.isBlank()) emptyList() else movies.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.genre.any { g -> g.contains(searchQuery, ignoreCase = true) } || it.cast.any { c -> c.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .testTag("search_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("universal_search_input"),
            placeholder = { Text("Search movies, TV channels, actors...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Amber500
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary
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
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Row: All / Live TV / Movies
        TabRow(
            selectedTabIndex = searchTab,
            containerColor = DarkSurface,
            contentColor = Amber500
        ) {
            Tab(
                selected = searchTab == 0,
                onClick = { searchTab = 0 },
                text = { Text("All (${matchingChannels.size + matchingMovies.size})") }
            )
            Tab(
                selected = searchTab == 1,
                onClick = { searchTab = 1 },
                text = { Text("Live Channels (${matchingChannels.size})") }
            )
            Tab(
                selected = searchTab == 2,
                onClick = { searchTab = 2 },
                text = { Text("Movies (${matchingMovies.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search prompt",
                        tint = TextMuted,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Find Your Favorite Tamil Media",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Search by channel name, movie title, genre, or star cast.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchTab == 0 || searchTab == 1) {
                    if (matchingChannels.isNotEmpty()) {
                        item {
                            Text(
                                text = "Live TV Channels",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Amber500,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(matchingChannels) { channel ->
                            ChannelCard(
                                channel = channel,
                                isCurrentPlaying = false,
                                isCompact = true,
                                onPlay = { onPlayChannel(channel) },
                                onToggleFavorite = { onToggleFavoriteChannel(channel.id) }
                            )
                        }
                    }
                }

                if (searchTab == 0 || searchTab == 2) {
                    if (matchingMovies.isNotEmpty()) {
                        item {
                            Text(
                                text = "Movies & Cinema",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Amber500,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(matchingMovies) { movie ->
                            LargeMoviePosterCard(
                                movie = movie,
                                isInWatchlist = watchlistIds.contains(movie.id),
                                onClick = { onSelectMovie(movie) },
                                onToggleWatchlist = { onToggleWatchlist(movie) }
                            )
                        }
                    }
                }

                if (matchingChannels.isEmpty() && matchingMovies.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for \"$searchQuery\"",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
