package com.prayogizan.golekpilem.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.common.RatingIndicator
import com.prayogizan.golekpilem.ui.common.shimmerEffect
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Reusable poster grid item card presenting media artwork, title, release year, and rating.
 *
 * Designed specifically for a 2-column [androidx.compose.foundation.lazy.grid.LazyVerticalGrid]
 * viewport. Features a 2:3 aspect ratio poster container powered by Coil 3, skeleton shimmer
 * loading placeholder, graceful fallback for null/missing artwork, and accessible touch targets.
 *
 * @param id The unique identifier for the media item.
 * @param title The primary display title or name of the media.
 * @param posterUrl Complete TMDB CDN image URL, or `null` if no poster artwork is available.
 * @param releaseYear 4-digit release or first-air year string (e.g. `"2026"`), or `"N/A"`.
 * @param voteAverage Scaled user rating score between 0.0 and 10.0.
 * @param onClick Callback invoked when the user selects the card.
 * @param modifier Optional modifier applied to the root card container.
 * @param aspectRatio Width-to-height ratio for the poster artwork, defaulting to `2f / 3f`.
 */
@Composable
fun MediaGridCard(
    id: Int,
    title: String,
    posterUrl: String?,
    releaseYear: String,
    voteAverage: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 2f / 3f
) {
    val cdText = stringResource(R.string.cd_poster_format, title)

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .semantics {
                role = Role.Button
                contentDescription = cdText
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Poster Artwork Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!posterUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(300)
                            .build(),
                        contentDescription = cdText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmerEffect()
                            )
                        },
                        error = {
                            PosterFallbackContainer()
                        }
                    )
                } else {
                    PosterFallbackContainer()
                }
            }

            // Card Footer Metadata
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = releaseYear,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    RatingIndicator(
                        score = voteAverage,
                        size = 32.dp,
                        showPercentage = false
                    )
                }
            }
        }
    }
}

/**
 * Fallback container rendered when poster artwork is absent, null, or failed to load.
 */
@Composable
private fun PosterFallbackContainer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = stringResource(R.string.cd_poster_placeholder),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Preview(name = "MediaGridCard - Light Mode", showBackground = true)
@Composable
private fun MediaGridCardLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(16.dp)) {
            MediaGridCard(
                id = 1,
                title = "Deadpool & Wolverine",
                posterUrl = null,
                releaseYear = "2026",
                voteAverage = 7.8,
                onClick = {}
            )
        }
    }
}

@Preview(name = "MediaGridCard - Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MediaGridCardDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            MediaGridCard(
                id = 2,
                title = "Stranger Things: Season Finale",
                posterUrl = null,
                releaseYear = "2026",
                voteAverage = 8.6,
                onClick = {}
            )
        }
    }
}
