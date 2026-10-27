package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tamilstream.data.model.Movie
import com.example.tamilstream.data.model.WatchlistItem
import com.example.tamilstream.ui.theme.*

@Composable
fun MoviesScreen(
    movies: List<Movie>,
    watchlist: List<WatchlistItem>,
    onSelectMovie: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGenre by remember { mutableStateOf("All") }
    var showOnlyPremium by remember { mutableStateOf(false) }

    val genres = listOf("All", "Action", "Thriller", "Crime", "Dark Comedy", "Historical")

    val publishedMovies = remember(movies) {
        movies.filter { it.status.equals("Published", ignoreCase = true) && it.isEnabled }
    }

    val filteredMovies = publishedMovies.filter { movie ->
        val matchesGenre = if (selectedGenre == "All") true else movie.genre.any { it.equals(selectedGenre, ignoreCase = true) } || movie.category.equals(selectedGenre, ignoreCase = true)
        val matchesPrem = if (showOnlyPremium) movie.isPremium else true
        matchesGenre && matchesPrem
    }

    Column(
        modifier = modifier
            .testTag("movies_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tamil Movies & OTT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // VIP Only filter chip
            FilterChip(
                selected = showOnlyPremium,
                onClick = { showOnlyPremium = !showOnlyPremium },
                label = { Text("VIP Only", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "VIP",
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Amber500,
                    selectedLabelColor = ObsidianBlack,
                    selectedLeadingIconColor = ObsidianBlack,
                    containerColor = DarkSurface,
                    labelColor = TextSecondary,
                    iconColor = TextSecondary
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Genre Rail
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(genres) { genre ->
                val isSelected = selectedGenre == genre
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGenre = genre },
                    label = { Text(genre) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Amber500,
                        selectedLabelColor = ObsidianBlack,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Movies
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .testTag("movies_grid"),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredMovies) { movie ->
                val inWatchlist = watchlist.any { it.contentId == movie.id }
                LargeMoviePosterCard(
                    movie = movie,
                    isInWatchlist = inWatchlist,
                    onClick = { onSelectMovie(movie) },
                    onToggleWatchlist = { onToggleWatchlist(movie) }
                )
            }
        }
    }
}

@Composable
fun LargeMoviePosterCard(
    movie: Movie,
    isInWatchlist: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("large_movie_card_${movie.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                AsyncImage(
                    model = movie.poster,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Rating & VIP Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (movie.isPremium) {
                        Surface(
                            color = Amber500,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "VIP",
                                color = ObsidianBlack,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Amber400,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = movie.rating.toString(),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (isInWatchlist) Amber500 else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${movie.releaseYear} • ${movie.genre.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
