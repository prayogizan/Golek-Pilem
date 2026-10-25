package com.prayogizan.golekpilem.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.MovieFilter
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Expressive multi-layered empty state illustration featuring a central cinema card,
 * layered ambient glow, and floating contextual badges representing empty discovery.
 *
 * @param modifier Optional modifier applied to the illustration container.
 */
@Composable
fun EmptyIllustration(
    modifier: Modifier = Modifier
) {
    val cdIllustration = stringResource(R.string.cd_empty_illustration)

    Box(
        modifier = modifier
            .size(180.dp)
            .semantics { contentDescription = cdIllustration },
        contentAlignment = Alignment.Center
    ) {
        // Outer soft radial ambient glow
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Middle decorative concentric ring
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    shape = CircleShape
                )
        )

        // Elevated central cinema motif card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.size(100.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Theaters,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Floating accent badge (Top-Trailing: Search Off)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 8.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        // Floating accent badge (Bottom-Leading: Movie Filter Off)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 8.dp, y = (-8).dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .border(
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MovieFilter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Full-screen empty state view displayed when a feed returns zero media items.
 *
 * Renders an expressive [EmptyIllustration], descriptive localized headers, and an optional
 * action button enabling users to trigger a feed refresh.
 *
 * @param modifier Optional modifier applied to the root container.
 * @param title Prominent heading describing the empty condition.
 * @param message Contextual explanation advising user on next steps.
 * @param onRefresh Optional callback invoked when the user taps the refresh action button.
 */
@Composable
fun EmptyFeedView(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.empty_feed_title),
    message: String = stringResource(R.string.empty_feed_message),
    onRefresh: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .semantics { contentDescription = "$title: $message" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyIllustration()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (onRefresh != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.empty_feed_refresh_button),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Preview(name = "EmptyFeedView - Light Mode", showBackground = true)
@Composable
private fun EmptyFeedViewLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            EmptyFeedView(onRefresh = {})
        }
    }
}

@Preview(name = "EmptyFeedView - Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyFeedViewDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            EmptyFeedView(onRefresh = {})
        }
    }
}

@Preview(name = "EmptyIllustration - Isolated", showBackground = true)
@Composable
private fun EmptyIllustrationPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(24.dp)) {
            EmptyIllustration()
        }
    }
}
