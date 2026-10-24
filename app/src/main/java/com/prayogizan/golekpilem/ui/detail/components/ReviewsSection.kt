package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Review
import com.prayogizan.golekpilem.ui.common.getRatingColor
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Section displaying public critique reviews submitted by community members.
 *
 * Each review card presents the reviewer's avatar, username, publication date,
 * score rating badge, and expandable body narrative.
 *
 * @param reviews Collection of [Review] domain items to render.
 * @param modifier Optional modifier applied to the root section container.
 */
@Composable
fun ReviewsSection(
    reviews: List<Review>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Title & Counter Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.detail_reviews_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (reviews.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = reviews.size.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (reviews.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.detail_no_reviews),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                reviews.forEach { review ->
                    ReviewCard(review = review)
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier
) {
    val isLongContent = review.content.length > 220
    var isExpanded by rememberSaveable { mutableStateOf(!isLongContent) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Author Metadata Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Avatar or Fallback Initials Badge
                    if (!review.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(review.avatarUrl)
                                .crossfade(300)
                                .build(),
                            contentDescription = stringResource(R.string.cd_author_avatar, review.author),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        val initial = review.author.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = review.author,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (review.createdAt.isNotBlank() && review.createdAt != "N/A") {
                            Text(
                                text = review.createdAt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Rating Badge (when score exists)
                if (review.rating != null && review.rating > 0.0) {
                    val ratingColor = getRatingColor(review.rating)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ratingColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ratingColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.rating_score_format, review.rating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ratingColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Review Content Narrative
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Expand / Collapse Action
            if (isLongContent) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isExpanded) {
                        stringResource(R.string.detail_review_show_less)
                    } else {
                        stringResource(R.string.detail_review_read_more)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(role = Role.Button) { isExpanded = !isExpanded }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Previews
// -------------------------------------------------------------------------

private val sampleReviews = listOf(
    Review(
        id = "rev_1",
        author = "CinemaCritic99",
        content = "An absolutely exhilarating cinematic masterclass! The visual fidelity and character chemistry surpassed every expectation. A must-watch experience on the biggest screen possible.",
        rating = 9.0,
        avatarUrl = null,
        createdAt = "2024-07-26"
    ),
    Review(
        id = "rev_2",
        author = "MovieBuff_Alex",
        content = "While the pacing slows down noticeably in the second act, the stellar world-building and memorable soundtrack more than make up for the narrative shortcomings. Solid recommendation for fans of the genre looking for depth and substance.",
        rating = 7.5,
        avatarUrl = null,
        createdAt = "2024-07-28"
    )
)

@Preview(name = "ReviewsSection - Light Theme", showBackground = true)
@Composable
private fun ReviewsSectionLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            ReviewsSection(
                reviews = sampleReviews,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "ReviewsSection - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReviewsSectionDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            ReviewsSection(
                reviews = sampleReviews,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "ReviewsSection Empty - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReviewsSectionEmptyDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            ReviewsSection(
                reviews = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
