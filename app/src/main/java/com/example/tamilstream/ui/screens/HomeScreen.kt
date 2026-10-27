package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamilstream.data.model.Channel
import com.example.tamilstream.data.model.Movie
import com.example.tamilstream.data.model.WatchHistoryItem
import com.example.tamilstream.data.model.WatchlistItem
import com.example.tamilstream.ui.components.HeroBanner
import com.example.tamilstream.ui.components.MovieCard
import com.example.tamilstream.ui.theme.*
import com.example.tamilstream.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    featuredMovie: Movie?,
    liveChannels: List<Channel>,
    movies: List<Movie>,
    watchlist: List<WatchlistItem>,
    watchHistory: List<WatchHistoryItem>,
    onPlayChannel: (Channel) -> Unit,
    onPlayMovie: (Movie) -> Unit,
    onSelectMovie: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("home_screen_lazy_column")
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Featured Movie Banner
        if (featuredMovie != null) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HeroBanner(
                        movie = featuredMovie,
                        onPlay = { onPlayMovie(featuredMovie) },
                        onDetails = { onSelectMovie(featuredMovie) }
                    )
                }
            }
        }

        // Live TV Quick Rail
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LiveRed)
                        )
                        Text(
                            text = "Live Tamil TV",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    TextButton(
                        onClick = { onNavigate(Screen.LIVE_TV) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Amber500)
                    ) {
                        Text("View All")
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View All",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(liveChannels) { channel ->
                        LiveChannelChip(
                            channel = channel,
                            onClick = { onPlayChannel(channel) }
                        )
                    }
                }
            }
        }

        // Trending Tamil Blockbusters
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending Blockbusters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    TextButton(
                        onClick = { onNavigate(Screen.MOVIES) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Amber500)
                    ) {
                        Text("Explore")
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Explore",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            isInWatchlist = watchlist.any { it.contentId == movie.id },
                            onClick = { onSelectMovie(movie) },
                            onToggleWatchlist = { onToggleWatchlist(movie) }
                        )
                    }
                }
            }
        }

        // Continue Watching / Watch History (if any)
        if (watchHistory.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Recently Watched",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(watchHistory) { item ->
                            RecentWatchCard(
                                item = item,
                                onClick = {
                                    val matchedChannel = liveChannels.find { it.id == item.contentId }
                                    if (matchedChannel != null) {
                                        onPlayChannel(matchedChannel)
                                    } else {
                                        val matchedMovie = movies.find { it.id == item.contentId }
                                        if (matchedMovie != null) {
                                            onPlayMovie(matchedMovie)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveChannelChip(
    channel: Channel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
            .testTag("home_channel_chip_${channel.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
            width = 0.5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = channel.logo,
                    contentDescription = channel.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = channel.cleanName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = LiveRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "LIVE",
                    color = LiveRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RecentWatchCard(
    item: WatchHistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("recent_watch_${item.contentId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Amber500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
