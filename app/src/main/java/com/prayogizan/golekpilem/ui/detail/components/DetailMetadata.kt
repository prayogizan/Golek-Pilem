package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.common.RatingIndicator
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Metadata presentation component for movie and television titles.
 *
 * Displays primary title, optional italicized tagline, a bullet-separated informational
 * row (release year, runtime or episode counts, status), and a circular score rating indicator
 * with formatted vote totals.
 *
 * @param title Primary title or name of the media.
 * @param tagline Optional promotional tagline or slogan.
 * @param releaseDate Initial release or premiere air date (e.g. `YYYY-MM-DD`).
 * @param status Commercial or broadcast release status (e.g. "Released", "Ended").
 * @param voteAverage User score rating scaled between 0.0 and 10.0.
 * @param voteCount Aggregate user vote submissions count.
 * @param runtime Total movie runtime in minutes, or `null` for TV series.
 * @param numberOfSeasons Total seasons aired, or `null` for movies.
 * @param numberOfEpisodes Total episodes aired, or `null` for movies.
 * @param modifier Optional modifier applied to the root container.
 */
@Composable
fun DetailMetadata(
    title: String,
    tagline: String?,
    releaseDate: String,
    status: String,
    voteAverage: Double,
    voteCount: Int,
    modifier: Modifier = Modifier,
    runtime: Int? = null,
    numberOfSeasons: Int? = null,
    numberOfEpisodes: Int? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Primary Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Italicized Tagline (conditionally displayed)
        if (!tagline.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"$tagline\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bullet-separated Info Row
        val releaseYear = releaseDate.split("-").firstOrNull()?.takeIf { it.length == 4 } ?: "N/A"
        val durationOrEpisodes = when {
            runtime != null && runtime > 0 -> {
                val hours = runtime / 60
                val minutes = runtime % 60
                if (hours > 0) {
                    stringResource(R.string.detail_runtime_hours_minutes, hours, minutes)
                } else {
                    stringResource(R.string.detail_runtime_minutes, minutes)
                }
            }
            numberOfSeasons != null && numberOfEpisodes != null && (numberOfSeasons > 0 || numberOfEpisodes > 0) -> {
                if (numberOfSeasons == 1 && numberOfEpisodes == 1) {
                    stringResource(R.string.detail_season_episode_single, numberOfSeasons, numberOfEpisodes)
                } else {
                    stringResource(R.string.detail_seasons_episodes, numberOfSeasons, numberOfEpisodes)
                }
            }
            else -> null
        }

        val infoItems = buildList {
            if (releaseYear.isNotBlank()) add(releaseYear)
            if (!durationOrEpisodes.isNullOrBlank()) add(durationOrEpisodes)
            if (status.isNotBlank()) add(status)
        }

        if (infoItems.isNotEmpty()) {
            Text(
                text = infoItems.joinToString(separator = "  •  "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rating & Vote Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            RatingIndicator(
                score = voteAverage,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = stringResource(R.string.rating_score_format, voteAverage) + " / 10",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (voteCount > 0) {
                    val formattedVotes = NumberFormat.getIntegerInstance(Locale.getDefault()).format(voteCount)
                    Text(
                        text = stringResource(R.string.detail_vote_count, formattedVotes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "DetailMetadata Movie - Light Theme", showBackground = true)
@Composable
private fun DetailMetadataMovieLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            DetailMetadata(
                title = "Inception",
                tagline = "Your mind is the scene of the crime.",
                releaseDate = "2010-07-16",
                status = "Released",
                voteAverage = 8.364,
                voteCount = 35120,
                runtime = 148
            )
        }
    }
}

@Preview(name = "DetailMetadata TV - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailMetadataTvDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailMetadata(
                title = "Breaking Bad",
                tagline = "Change the equation.",
                releaseDate = "2008-01-20",
                status = "Ended",
                voteAverage = 8.895,
                voteCount = 13800,
                numberOfSeasons = 5,
                numberOfEpisodes = 62
            )
        }
    }
}
