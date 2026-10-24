package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Video
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Section displaying an embedded in-app trailer player along with a horizontal selector
 * carousel for supplementary promotional clips and teasers.
 *
 * Streams videos directly within the application while always providing a prominent
 * action button to navigate to the official video on YouTube.
 *
 * @param videos Collection of [Video] domain entities to display.
 * @param onNavigateToOfficial Callback invoked to open the official video on YouTube externally.
 * @param onOpenFullscreen Callback invoked to launch the video in the fullscreen in-app dialog.
 * @param modifier Optional modifier applied to the root section layout.
 */
@Composable
fun TrailersSection(
    videos: List<Video>,
    onNavigateToOfficial: (Video) -> Unit,
    onOpenFullscreen: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    var selectedVideo by remember(videos) {
        mutableStateOf(
            videos.firstOrNull { it.isYouTube && it.type.equals("Trailer", ignoreCase = true) && it.isOfficial }
                ?: videos.firstOrNull { it.isYouTube && it.type.equals("Trailer", ignoreCase = true) }
                ?: videos.firstOrNull { it.isYouTube }
                ?: videos.firstOrNull()
        )
    }

    var isPlayingInApp by remember(selectedVideo?.id) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Title & Counter Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.detail_trailers_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = videos.size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Featured In-App Player Card
        selectedVideo?.let { activeVideo ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Video Viewport: In-App Player or Playback Poster
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(Color.Black)
                    ) {
                        if (isPlayingInApp) {
                            InAppYouTubePlayer(
                                videoKey = activeVideo.key,
                                autoPlay = true,
                                onFullScreenRequest = {
                                    isPlayingInApp = false
                                    onOpenFullscreen(activeVideo)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            if (activeVideo.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(activeVideo.thumbnailUrl)
                                        .crossfade(300)
                                        .build(),
                                    contentDescription = stringResource(R.string.cd_trailer_thumbnail, activeVideo.name),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            // Dark Scrim Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                            )

                            // Play Overlay Button
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .align(Alignment.Center)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = stringResource(R.string.cd_play_trailer, activeVideo.name)
                                    ) { isPlayingInApp = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    // Metadata & External Navigation Actions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = activeVideo.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                    ) {
                                        Text(
                                            text = activeVideo.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (activeVideo.isOfficial) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.trailer_official_badge),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (activeVideo.publishedAt.isNotBlank()) {
                                        Text(
                                            text = activeVideo.publishedAt,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Fullscreen Overlay Trigger
                            IconButton(
                                onClick = {
                                    isPlayingInApp = false
                                    onOpenFullscreen(activeVideo)
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = stringResource(R.string.cd_fullscreen_trailer),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Button to Navigate to the Official Video externally
                        OutlinedButton(
                            onClick = { onNavigateToOfficial(activeVideo) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.detail_watch_on_youtube),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Carousel for selecting other clips
        if (videos.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = videos,
                    key = { it.id }
                ) { video ->
                    val isSelected = video.id == selectedVideo?.id
                    TrailerThumbnailItem(
                        video = video,
                        isSelected = isSelected,
                        onClick = {
                            selectedVideo = video
                            isPlayingInApp = true
                        }
                    )
                }
            }
        }
    }
}

/**
 * Thumbnail item rendered within the supplementary videos carousel.
 */
@Composable
private fun TrailerThumbnailItem(
    video: Video,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        ),
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.cd_play_trailer, video.name),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnailUrl)
                        .crossfade(300)
                        .build(),
                    contentDescription = stringResource(R.string.cd_trailer_thumbnail, video.name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = video.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------
// Previews
// -------------------------------------------------------------------------

private val sampleVideos = listOf(
    Video(
        id = "vid_1",
        name = "Official Trailer #1",
        key = "73_1biulkYk",
        site = "YouTube",
        type = "Trailer",
        isOfficial = true,
        publishedAt = "2024-04-22"
    ),
    Video(
        id = "vid_2",
        name = "Official Teaser",
        key = "uJMCNJP2ipI",
        site = "YouTube",
        type = "Teaser",
        isOfficial = true,
        publishedAt = "2024-02-11"
    )
)

@Preview(name = "TrailersSection - Light Theme", showBackground = true)
@Composable
private fun TrailersSectionLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            TrailersSection(
                videos = sampleVideos,
                onNavigateToOfficial = {},
                onOpenFullscreen = {},
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Preview(name = "TrailersSection - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrailersSectionDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            TrailersSection(
                videos = sampleVideos,
                onNavigateToOfficial = {},
                onOpenFullscreen = {},
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}
