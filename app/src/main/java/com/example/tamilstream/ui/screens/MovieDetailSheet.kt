package com.example.tamilstream.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.example.tamilstream.data.model.Movie
import com.example.tamilstream.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailSheet(
    movie: Movie,
    isInWatchlist: Boolean,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) },
        modifier = Modifier.testTag("movie_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header: Poster + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = movie.poster,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .size(110.dp, 160.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    if (movie.isPremium) {
                        Surface(
                            color = Amber500,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "VIP",
                                    tint = ObsidianBlack,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "VIP EXCLUSIVE",
                                    color = ObsidianBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Amber400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${movie.rating} / 10",
                            color = Amber400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "• ${movie.releaseYear}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "• ${movie.duration} min",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Genre: ${movie.genre.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (movie.director.isNotBlank()) {
                        Text(
                            text = "Director: ${movie.director}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Stream Now & Watchlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_play_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (movie.isPremium) "Watch with VIP" else "Watch Movie",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onToggleWatchlist,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("detail_watchlist_btn")
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (isInWatchlist) Amber500 else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Synopsis
            Text(
                text = "Synopsis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = movie.synopsis,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            // Cast list
            if (movie.cast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Star Cast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = movie.cast.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Amber400
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
