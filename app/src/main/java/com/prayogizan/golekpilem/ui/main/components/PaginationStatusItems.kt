package com.prayogizan.golekpilem.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Centered circular loading indicator appended at the bottom of the feed grid during pagination.
 *
 * Typically wrapped inside a full-width grid item using `span = { GridItemSpan(maxLineSpan) }`.
 *
 * @param modifier Optional modifier applied to the container box.
 */
@Composable
fun PaginationLoadingItem(modifier: Modifier = Modifier) {
    val cd = stringResource(R.string.cd_loading_indicator)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Inline error recovery row appended at the bottom of the feed grid when pagination append fails.
 *
 * Typically wrapped inside a full-width grid item using `span = { GridItemSpan(maxLineSpan) }`.
 *
 * @param onRetry Callback triggered when user taps the "Retry" button.
 * @param modifier Optional modifier applied to the root container column.
 * @param message Localized error explanation displayed to the user.
 */
@Composable
fun PaginationErrorItem(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.pagination_error_message)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(
                text = stringResource(R.string.pagination_retry_button),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Preview(name = "PaginationLoadingItem - Light", showBackground = true)
@Composable
private fun PaginationLoadingItemPreview() {
    GolekPilemTheme {
        Surface {
            PaginationLoadingItem()
        }
    }
}

@Preview(name = "PaginationErrorItem - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaginationErrorItemPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            PaginationErrorItem(onRetry = {})
        }
    }
}
