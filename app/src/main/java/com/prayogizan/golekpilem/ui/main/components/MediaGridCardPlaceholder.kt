package com.prayogizan.golekpilem.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.ui.common.shimmerEffect
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Skeleton shimmer placeholder card replicating [MediaGridCard] layout and aspect ratio.
 *
 * Utilized across initial loading states and refresh cycles within the 2-column feed grid.
 *
 * @param modifier Optional modifier applied to the outer placeholder card.
 * @param aspectRatio Width-to-height ratio for the shimmer poster box.
 */
@Composable
fun MediaGridCardPlaceholder(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 2f / 3f
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .semantics { contentDescription = "Loading media card placeholder" }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Shimmer Poster box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .shimmerEffect()
            )

            // Shimmer Footer elements
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Title line 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Title line 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Year & Rating placeholder row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Preview(name = "MediaGridCardPlaceholder - Light Mode", showBackground = true)
@Composable
private fun MediaGridCardPlaceholderLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(16.dp)) {
            MediaGridCardPlaceholder()
        }
    }
}

@Preview(name = "MediaGridCardPlaceholder - Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MediaGridCardPlaceholderDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            MediaGridCardPlaceholder()
        }
    }
}
