package com.prayogizan.golekpilem.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme
import com.prayogizan.golekpilem.ui.theme.RatingHigh
import com.prayogizan.golekpilem.ui.theme.RatingLow
import com.prayogizan.golekpilem.ui.theme.RatingMedium

/**
 * Computes the semantic status color based on the TMDB rating score.
 *
 * Threshold mapping:
 * - Score >= 7.0: [RatingHigh] (Vibrant Green: #4CAF50)
 * - Score 5.0 - 6.9: [RatingMedium] (Amber/Yellow: #FFC107)
 * - Score < 5.0: [RatingLow] (Red: #F44336)
 *
 * @param score The raw vote average score typically between 0.0 and 10.0.
 * @return The corresponding semantic [Color].
 */
fun getRatingColor(score: Double): Color = when {
    score >= 7.0 -> RatingHigh
    score >= 5.0 -> RatingMedium
    else -> RatingLow
}

/**
 * Circular score badge showing percentage of TMDB vote average with dynamic threshold coloring.
 *
 * Adheres to WCAG AA contrast criteria and includes descriptive accessibility semantics.
 *
 * @param score Raw TMDB rating score scaled from 0.0 to 10.0.
 * @param modifier Optional modifier applied to the outer badge container.
 * @param size Outer dimension for width and height of the circular badge.
 * @param strokeWidth Thickness of the circular progress arc. When unspecified, automatically scales with [size].
 * @param showPercentage When true, displays integer percentage (`(score * 10)%`); otherwise displays score with single decimal place (e.g. `"8.5"`).
 */
@Composable
fun RatingIndicator(
    score: Double,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = Dp.Unspecified,
    showPercentage: Boolean = true
) {
    val boundedScore = score.coerceIn(0.0, 10.0)
    val progress = (boundedScore / 10.0).toFloat()
    val ratingColor = getRatingColor(boundedScore)
    val percentageValue = (boundedScore * 10).toInt()
    val cdText = stringResource(R.string.cd_rating_indicator, boundedScore)

    val effectiveStrokeWidth = if (strokeWidth != Dp.Unspecified && strokeWidth > 0.dp) {
        strokeWidth
    } else {
        when {
            size <= 28.dp -> 2.dp
            size <= 36.dp -> 2.5.dp
            else -> 3.5.dp
        }
    }

    val fontSize = when {
        size <= 28.dp -> 9.sp
        size <= 34.dp -> 10.5.sp
        size <= 42.dp -> 11.5.sp
        else -> 13.sp
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xE610141D)) // Opaque dark surface for high contrast against backdrops
            .semantics { contentDescription = cdText },
        contentAlignment = Alignment.Center
    ) {
        // Background track
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxSize()
                .padding(effectiveStrokeWidth / 2 + 1.dp),
            color = ratingColor.copy(alpha = 0.25f),
            strokeWidth = effectiveStrokeWidth,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )

        // Active progress arc
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .padding(effectiveStrokeWidth / 2 + 1.dp),
            color = ratingColor,
            strokeWidth = effectiveStrokeWidth,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )

        if (showPercentage) {
            Text(
                text = stringResource(R.string.rating_percentage_format, percentageValue),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Text(
                text = stringResource(R.string.rating_score_format, boundedScore),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(name = "RatingIndicator - High Score (Light)", showBackground = true)
@Composable
private fun RatingIndicatorHighPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            RatingIndicator(score = 8.4)
        }
    }
}

@Preview(name = "RatingIndicator - Medium Score (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RatingIndicatorMediumPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            RatingIndicator(score = 6.2)
        }
    }
}

@Preview(name = "RatingIndicator - Low Score (Light)", showBackground = true)
@Composable
private fun RatingIndicatorLowPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            RatingIndicator(score = 4.3)
        }
    }
}

@Preview(name = "RatingIndicator - Compact Card 32dp (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RatingIndicatorCompactCardPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            RatingIndicator(score = 8.5, size = 32.dp, showPercentage = false)
        }
    }
}
