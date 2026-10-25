package com.prayogizan.golekpilem.ui.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Reusable section container presenting a stylized heading and detailed narrative body text.
 *
 * @param title Section heading text (e.g. "Overview").
 * @param content Narrative body text describing the synopsis or details.
 * @param modifier Optional modifier applied to the root container.
 */
@Composable
fun InfoSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content.ifBlank { stringResource(R.string.detail_no_overview) },
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "InfoSection - Light Theme", showBackground = true)
@Composable
private fun InfoSectionLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            InfoSection(
                title = "Overview",
                content = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O."
            )
        }
    }
}

@Preview(name = "InfoSection - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InfoSectionDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            InfoSection(
                title = "Overview",
                content = ""
            )
        }
    }
}
