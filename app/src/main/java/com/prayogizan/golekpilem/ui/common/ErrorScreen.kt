package com.prayogizan.golekpilem.ui.common

import android.content.res.Configuration
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Full-screen error recovery screen presenting an iconography indicator, descriptive
 * error explanation, and an actionable retry button conforming to WCAG AA touch targets.
 *
 * Automatically debounces multiple rapid taps on the "Try Again" action button within
 * a 1000ms threshold to prevent spamming network calls.
 *
 * @param message Human-readable error description or network failure rationale.
 * @param onRetry Callback triggered when user taps the "Try Again" action button.
 * @param modifier Optional modifier applied to the root container.
 * @param title Prominent error heading describing the failure context.
 * @param isNetworkError When `true`, presents offline-specific iconography ([Icons.Filled.WifiOff]).
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_default_title),
    isNetworkError: Boolean = false
) {
    val cdIcon = if (isNetworkError) {
        stringResource(R.string.cd_offline_icon)
    } else {
        stringResource(R.string.cd_error_icon)
    }

    val iconVector = if (isNetworkError) {
        Icons.Filled.WifiOff
    } else {
        Icons.Default.Warning
    }

    var lastClickTime by remember { mutableLongStateOf(0L) }
    val onDebouncedRetry: () -> Unit = {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= 1000L) {
            lastClickTime = currentTime
            onRetry()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = cdIcon,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
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
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            val retryButtonCd = stringResource(R.string.error_retry_button)
            Button(
                onClick = onDebouncedRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 140.dp, minHeight = 48.dp)
                    .semantics { contentDescription = retryButtonCd }
            ) {
                Text(
                    text = stringResource(R.string.error_retry_button),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Preview(name = "ErrorScreen - Light Theme", showBackground = true)
@Composable
private fun ErrorScreenLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            ErrorScreen(
                message = "Unable to connect to TMDB API server. Please check your internet connection.",
                onRetry = {}
            )
        }
    }
}

@Preview(name = "ErrorScreen - Offline Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorScreenOfflineDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            ErrorScreen(
                title = "No Connection",
                message = "No internet connection. Please check your network and try again.",
                isNetworkError = true,
                onRetry = {}
            )
        }
    }
}
