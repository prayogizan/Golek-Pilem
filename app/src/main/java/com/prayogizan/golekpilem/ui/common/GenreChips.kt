package com.prayogizan.golekpilem.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Renders a horizontal wrapping collection of category/genre chips using [FlowRow].
 *
 * Each chip provides tonal feedback, rounded corners (`8.dp`), and conforms to
 * accessibility recommendations with semantic content descriptions.
 *
 * @param genres List of domain [Genre] items to display.
 * @param modifier Optional modifier applied to the outer [FlowRow].
 * @param onGenreClick Optional callback invoked when a specific genre chip is tapped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreChips(
    genres: List<Genre>,
    modifier: Modifier = Modifier,
    onGenreClick: ((Genre) -> Unit)? = null
) {
    if (genres.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        genres.forEach { genre ->
            val cd = stringResource(R.string.cd_genre_chip, genre.name)
            SuggestionChip(
                onClick = { onGenreClick?.invoke(genre) },
                label = {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.semantics { contentDescription = cd }
            )
        }
    }
}

@Preview(name = "GenreChips - Light Theme", showBackground = true)
@Composable
private fun GenreChipsLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            GenreChips(
                genres = listOf(
                    Genre(id = 28, name = "Action"),
                    Genre(id = 12, name = "Adventure"),
                    Genre(id = 878, name = "Science Fiction"),
                    Genre(id = 53, name = "Thriller")
                )
            )
        }
    }
}

@Preview(name = "GenreChips - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GenreChipsDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            GenreChips(
                genres = listOf(
                    Genre(id = 18, name = "Drama"),
                    Genre(id = 10749, name = "Romance"),
                    Genre(id = 35, name = "Comedy")
                )
            )
        }
    }
}
