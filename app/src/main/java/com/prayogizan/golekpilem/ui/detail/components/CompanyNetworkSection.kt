package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Network
import com.prayogizan.golekpilem.domain.model.ProductionCompany
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * UI representation of a production company or broadcast network item.
 */
data class CreditEntity(
    val id: Int,
    val name: String,
    val logoUrl: String?
)

/**
 * Horizontally scrollable row displaying company or network credits with logos and names.
 *
 * Automatically hides itself if [items] is empty.
 *
 * @param title Section title (e.g. "Production Companies" or "Broadcast Networks").
 * @param items List of credit entities to display.
 * @param modifier Optional modifier applied to the root container.
 */
@Composable
fun CompanyNetworkSection(
    title: String,
    items: List<CreditEntity>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                CreditCard(item = item)
            }
        }
    }
}

@Composable
private fun CreditCard(
    item: CreditEntity,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        modifier = modifier
            .width(130.dp)
            .height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (!item.logoUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 48.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.logoUrl)
                            .crossfade(300)
                            .build(),
                        contentDescription = stringResource(R.string.cd_company_logo, item.name),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Convenience adapter function mapping [ProductionCompany] entities.
 */
fun List<ProductionCompany>.toCreditEntities(): List<CreditEntity> = map {
    CreditEntity(
        id = it.id,
        name = it.name,
        logoUrl = it.logoUrl
    )
}

/**
 * Convenience adapter function mapping [Network] entities.
 */
fun List<Network>.toNetworkCreditEntities(): List<CreditEntity> = map {
    CreditEntity(
        id = it.id,
        name = it.name,
        logoUrl = it.logoUrl
    )
}

@Preview(name = "CompanyNetworkSection - Light Theme", showBackground = true)
@Composable
private fun CompanyNetworkSectionLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            CompanyNetworkSection(
                title = "Production Companies",
                items = listOf(
                    CreditEntity(1, "Marvel Studios", null),
                    CreditEntity(2, "Syncopy", null),
                    CreditEntity(3, "Legendary Pictures", null)
                )
            )
        }
    }
}

@Preview(name = "CompanyNetworkSection - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompanyNetworkSectionDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            CompanyNetworkSection(
                title = "Broadcast Networks",
                items = listOf(
                    CreditEntity(1, "HBO", null),
                    CreditEntity(2, "Netflix", null)
                )
            )
        }
    }
}
