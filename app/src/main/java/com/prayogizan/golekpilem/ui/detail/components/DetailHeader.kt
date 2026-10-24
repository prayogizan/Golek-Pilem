package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Hero header composable for the detail screen.
 *
 * Renders a full-width cinematic hero backdrop image (`w780`) with a bottom vertical
 * gradient scrim, a high-contrast circular back navigation button overlaid at the top-left,
 * and an overlapping elevated poster card (`w342`) anchored across the backdrop boundary.
 *
 * @param title Title of the movie or television series for accessibility descriptions.
 * @param backdropUrl Remote image URL for the cinematic hero backdrop, or `null`.
 * @param posterUrl Remote image URL for the primary poster artwork, or `null`.
 * @param onBackClick Navigation callback triggered when the user taps the back button.
 * @param modifier Optional modifier applied to the root container.
 */
@Composable
fun DetailHeader(
    title: String,
    backdropUrl: String?,
    posterUrl: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
    ) {
        // Hero Backdrop Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            if (!backdropUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(backdropUrl)
                        .crossfade(300)
                        .build(),
                    contentDescription = stringResource(R.string.cd_detail_backdrop, title),
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
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Bottom Gradient Scrim Transition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Overlaid High-Contrast Circular Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp)
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Overlapping Elevated Poster Card
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.BottomStart)
                .size(width = 120.dp, height = 180.dp)
        ) {
            if (!posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(posterUrl)
                        .crossfade(300)
                        .build(),
                    contentDescription = stringResource(R.string.cd_detail_poster, title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = stringResource(R.string.cd_poster_placeholder),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "DetailHeader - Light Theme", showBackground = true)
@Composable
private fun DetailHeaderLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            DetailHeader(
                title = "Deadpool & Wolverine",
                backdropUrl = null,
                posterUrl = null,
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "DetailHeader - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailHeaderDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailHeader(
                title = "Stranger Things",
                backdropUrl = null,
                posterUrl = null,
                onBackClick = {}
            )
        }
    }
}
