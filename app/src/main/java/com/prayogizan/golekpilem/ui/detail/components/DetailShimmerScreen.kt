package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.common.shimmerEffect
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Skeleton shimmer loading screen replicating the detail view layout.
 *
 * Renders backdrop, overlapping poster card, title, genre chips, and content placeholders
 * while metadata is resolved from the network.
 *
 * @param onBackClick Callback triggered when user navigates back while loading.
 * @param modifier Optional modifier applied to the root container.
 */
@Composable
fun DetailShimmerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Loading media details placeholder" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Backdrop Shimmer Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shimmerEffect()
            )

            // Overlapping Poster and Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Poster Card Placeholder
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmerEffect()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and Meta lines
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }

            // Genre Chips Shimmer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shimmerEffect()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Overview Section Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }

        // Top Back Button with translucent circular background
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
}

@Preview(name = "DetailShimmerScreen - Light", showBackground = true)
@Composable
private fun DetailShimmerScreenLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            DetailShimmerScreen(onBackClick = {})
        }
    }
}

@Preview(name = "DetailShimmerScreen - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailShimmerScreenDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailShimmerScreen(onBackClick = {})
        }
    }
}
