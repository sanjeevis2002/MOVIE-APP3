package com.example.tamilstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.tamilstream.data.model.Episode
import com.example.tamilstream.ui.theme.*

/**
 * Form Dialog for adding and editing episodes in the Admin Panel.
 * Includes Title, Thumbnail URL (with live preview and validation),
 * Video URL, Description, Publish Status, and Featured toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeFormDialog(
    episodeToEdit: Episode?,
    seriesList: List<com.example.tamilstream.data.model.Series> = emptyList(),
    defaultSeriesId: String = "",
    onDismiss: () -> Unit,
    onSave: (Episode) -> Unit
) {
    val isEdit = episodeToEdit != null

    var title by remember { mutableStateOf(episodeToEdit?.title ?: "") }
    var thumbnailUrl by remember { mutableStateOf(episodeToEdit?.thumbnailUrl ?: "") }
    var videoUrl by remember { mutableStateOf(episodeToEdit?.videoUrl ?: "") }
    var description by remember { mutableStateOf(episodeToEdit?.description ?: "") }
    var selectedSeriesId by remember { mutableStateOf(episodeToEdit?.seriesId ?: defaultSeriesId) }
    var seriesName by remember {
        mutableStateOf(
            episodeToEdit?.seriesName
                ?: seriesList.find { it.id == defaultSeriesId }?.title
                ?: seriesList.firstOrNull()?.title
                ?: "Bigg Boss"
        )
    }
    var episodeNumberStr by remember { mutableStateOf(episodeToEdit?.episodeNumber?.toString() ?: "1") }
    var durationMinutesStr by remember { mutableStateOf(episodeToEdit?.durationMinutes?.toString() ?: "24") }
    var status by remember { mutableStateOf(episodeToEdit?.status ?: "Published") } // "Draft", "Published", "Unpublished"
    var isFeatured by remember { mutableStateOf(episodeToEdit?.isFeatured ?: false) }
    var isEnabled by remember { mutableStateOf(episodeToEdit?.isEnabled ?: true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val statusOptions = listOf("Draft", "Published", "Unpublished")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("episode_form_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Edit Episode" else "Add New Episode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = DarkBorder)

                // Error alert
                if (errorMessage != null) {
                    Surface(
                        color = LiveRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = LiveRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // 1. Episode Title (Field 1)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Episode Title *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        placeholder = { Text("e.g., Ethirneechal - Mega Climax Ep 450", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("episode_form_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // 2. Thumbnail URL (Field 2) & Live Preview
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Thumbnail URL *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = {
                            thumbnailUrl = it
                            errorMessage = null
                        },
                        placeholder = { Text("https://example.com/images/thumb.jpg", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("episode_form_thumbnail_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Live Thumbnail Preview & Validation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thumbnailUrl.isNotBlank() && (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://"))) {
                            AsyncImage(
                                model = thumbnailUrl,
                                contentDescription = "Live Thumbnail Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = ObsidianBlack.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "LIVE PREVIEW",
                                    color = LiveGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Enter a valid image URL to view live preview",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 3. Episode Video URL (Field 3)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Episode Video / Stream URL *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = {
                            videoUrl = it
                            errorMessage = null
                        },
                        placeholder = { Text("https://.../stream.m3u8 or .mp4", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("episode_form_video_url_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // 4. Description (Field 4)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Description (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Brief plot summary or episode storyline...", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("episode_form_desc_input"),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Series Name & Number Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Series / Show Name", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        OutlinedTextField(
                            value = seriesName,
                            onValueChange = { seriesName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber500,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Episode #", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        OutlinedTextField(
                            value = episodeNumberStr,
                            onValueChange = { episodeNumberStr = it.filter { char -> char.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber500,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // 5. Publish Status Selector (Field 5: Draft, Published, Unpublished)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Publish Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statusOptions.forEach { opt ->
                            val isSelected = status.equals(opt, ignoreCase = true)
                            val optColor = when (opt) {
                                "Published" -> LiveGreen
                                "Draft" -> Amber500
                                else -> LiveRed
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) optColor.copy(alpha = 0.2f) else DarkSurfaceElevated,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(optColor)) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .testTag("episode_status_option_$opt")
                            ) {
                                TextButton(
                                    onClick = { status = opt },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (isSelected) optColor else TextSecondary
                                    )
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Featured Toggle (Field 6) & Enable Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Featured Episode", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text("Highlight in hero sections", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
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
                    Column {
                        Text("Enable Episode", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Text("Allow playback in the platform", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = LiveGreen)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions: Save & Cancel (Fields 7 & 8)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("episode_form_cancel_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter an episode title."
                                return@Button
                            }
                            if (thumbnailUrl.isBlank()) {
                                errorMessage = "Please enter a valid thumbnail URL."
                                return@Button
                            }
                            if (videoUrl.isBlank()) {
                                errorMessage = "Please enter a valid video streaming URL."
                                return@Button
                            }

                            val ep = Episode(
                                id = episodeToEdit?.id ?: "ep_${System.currentTimeMillis()}",
                                title = title.trim(),
                                seriesName = seriesName.ifBlank { "Tamil TV Serial" }.trim(),
                                episodeNumber = episodeNumberStr.toIntOrNull() ?: 1,
                                seasonNumber = episodeToEdit?.seasonNumber ?: 1,
                                thumbnailUrl = thumbnailUrl.trim(),
                                videoUrl = videoUrl.trim(),
                                description = description.trim(),
                                status = status,
                                isFeatured = isFeatured,
                                isEnabled = isEnabled,
                                durationMinutes = durationMinutesStr.toIntOrNull() ?: 24,
                                views = episodeToEdit?.views ?: 0,
                                createdAt = episodeToEdit?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                createdBy = episodeToEdit?.createdBy ?: "admin"
                            )
                            onSave(ep)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("episode_form_save_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber500,
                            contentColor = ObsidianBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isEdit) "Update Episode" else "Save Episode", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Delete Confirmation Dialog for Admin Panel.
 */
@Composable
fun EpisodeDeleteConfirmDialog(
    episode: Episode,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Episode?",
                color = LiveRed,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Are you sure you want to permanently delete this episode?",
                    color = TextPrimary
                )
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = episode.title,
                            fontWeight = FontWeight.Bold,
                            color = Amber500
                        )
                        Text(
                            text = "Series: ${episode.seriesName} • Ep ${episode.episodeNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = "This action will remove it from all users' libraries and cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_delete_episode_btn")
            ) {
                Text("Delete Permanently", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(14.dp)
    )
}

/**
 * Preview Modal Dialog for reviewing an episode stream.
 */
@Composable
fun EpisodePreviewDialog(
    episode: Episode,
    onDismiss: () -> Unit,
    onPlayInPlayer: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("episode_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Episode Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Thumbnail with play button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = episode.thumbnailUrl,
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = onPlayInPlayer,
                        modifier = Modifier
                            .size(54.dp)
                            .background(Amber500, RoundedCornerShape(27.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Now",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Stream URL: ${episode.videoUrl}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (episode.status == "Published") LiveGreen.copy(alpha = 0.2f) else Amber500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = episode.status.uppercase(),
                            color = if (episode.status == "Published") LiveGreen else Amber500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = if (episode.isEnabled) LiveGreen.copy(alpha = 0.2f) else LiveRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (episode.isEnabled) "ENABLED" else "DISABLED",
                            color = if (episode.isEnabled) LiveGreen else LiveRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = onPlayInPlayer,
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Fullscreen Video Player", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSeriesDialog(
    seriesToEdit: com.example.tamilstream.data.model.Series?,
    onDismiss: () -> Unit,
    onSave: (com.example.tamilstream.data.model.Series) -> Unit
) {
    var title by remember { mutableStateOf(seriesToEdit?.title ?: "") }
    var mainThumbnailUrl by remember { mutableStateOf(seriesToEdit?.mainThumbnailUrl ?: "") }
    var description by remember { mutableStateOf(seriesToEdit?.description ?: "") }
    var status by remember { mutableStateOf(seriesToEdit?.status ?: "Published") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isThumbnailValid = mainThumbnailUrl.isBlank() || mainThumbnailUrl.startsWith("http://") || mainThumbnailUrl.startsWith("https://")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("series_form_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                width = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (seriesToEdit != null) "Edit Series" else "Add New Series",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = DarkBorder)

                if (errorMessage != null) {
                    Surface(
                        color = LiveRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = LiveRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Title
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Main Series Title *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; errorMessage = null },
                        placeholder = { Text("e.g., Bigg Boss", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("series_form_title_input"),
                        singleLine = true
                    )
                }

                // Main Thumbnail
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Main Series Thumbnail URL *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = mainThumbnailUrl,
                        onValueChange = { mainThumbnailUrl = it; errorMessage = null },
                        placeholder = { Text("https://example.com/main_thumb.jpg", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("series_form_thumbnail_input"),
                        singleLine = true
                    )

                    if (mainThumbnailUrl.isNotBlank() && isThumbnailValid) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = mainThumbnailUrl,
                                contentDescription = "Series Main Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = ObsidianBlack.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                            ) {
                                Text("MAIN SERIES THUMBNAIL", color = Amber500, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Description (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Brief description of the series...", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("series_form_desc_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Publish Status
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Publish Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Published", "Draft", "Unpublished").forEach { opt ->
                            val isSelected = status.equals(opt, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { status = opt },
                                label = { Text(opt, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (opt == "Published") LiveGreen.copy(alpha = 0.2f) else Amber500.copy(alpha = 0.2f),
                                    selectedLabelColor = if (opt == "Published") LiveGreen else Amber500
                                )
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Series title is required."
                                return@Button
                            }
                            if (mainThumbnailUrl.isBlank()) {
                                errorMessage = "Main thumbnail URL is required."
                                return@Button
                            }
                            val finalSeries = com.example.tamilstream.data.model.Series(
                                id = seriesToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                                title = title.trim(),
                                mainThumbnailUrl = mainThumbnailUrl.trim(),
                                description = description.trim(),
                                status = status,
                                isEnabled = status.equals("Published", ignoreCase = true),
                                createdAt = seriesToEdit?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(finalSeries)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = ObsidianBlack),
                        modifier = Modifier.testTag("save_series_button")
                    ) {
                        Text(if (seriesToEdit == null) "Create Series" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesDeleteConfirmDialog(
    series: com.example.tamilstream.data.model.Series,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Series", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Text("Are you sure you want to delete \"${series.title}\" and all of its associated episodes? This action cannot be undone.", color = TextSecondary)
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = TextPrimary),
                modifier = Modifier.testTag("confirm_delete_series_button")
            ) {
                Text("Delete Series & Episodes", fontWeight = FontWeight.Bold)
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
