package com.prayogizan.golekpilem.ui.common

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.core.connectivity.ConnectivityObserver
import com.prayogizan.golekpilem.ui.theme.AmberGold40
import com.prayogizan.golekpilem.ui.theme.CrimsonGrey40
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme
import kotlinx.coroutines.delay

/**
 * Visual presentation states supported by the [NetworkStatusBanner].
 */
internal enum class NetworkBannerState {
    /** Banner is completely hidden from view. */
    Hidden,

    /** Active offline banner alerting user that connection was lost. */
    Offline,

    /** Transient reconnection banner indicating restored connectivity. */
    Reconnected
}

/**
 * Non-intrusive animated status banner communicating real-time network connectivity changes.
 *
 * Automatically reveals an offline notification pill when the device loses network connectivity,
 * and transitions to an auto-dismissing reconnection pill when internet access is restored.
 * Suppresses false-positive reconnection notifications during initial clean app launches.
 *
 * @param networkStatus Current [ConnectivityObserver.Status] emitted by the system observer.
 * @param modifier Optional modifier applied to the root banner container.
 */
@Composable
fun NetworkStatusBanner(
    networkStatus: ConnectivityObserver.Status,
    modifier: Modifier = Modifier
) {
    var previousStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var bannerState by remember { mutableStateOf(NetworkBannerState.Hidden) }

    LaunchedEffect(networkStatus) {
        val prev = previousStatus?.let { runCatching { ConnectivityObserver.Status.valueOf(it) }.getOrNull() }

        when (networkStatus) {
            ConnectivityObserver.Status.Lost,
            ConnectivityObserver.Status.Unavailable -> {
                bannerState = NetworkBannerState.Offline
            }
            ConnectivityObserver.Status.Available -> {
                if (prev == ConnectivityObserver.Status.Lost || prev == ConnectivityObserver.Status.Unavailable) {
                    bannerState = NetworkBannerState.Reconnected
                    delay(3000L)
                    bannerState = NetworkBannerState.Hidden
                } else {
                    bannerState = NetworkBannerState.Hidden
                }
            }
        }
        previousStatus = networkStatus.name
    }

    AnimatedVisibility(
        visible = bannerState != NetworkBannerState.Hidden,
        enter = slideInVertically(initialOffsetY = { -it }) + expandVertically() + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        when (bannerState) {
            NetworkBannerState.Offline -> {
                NetworkStatusBannerContent(
                    backgroundColor = CrimsonGrey40,
                    contentColor = Color.White,
                    icon = Icons.Default.WifiOff,
                    iconContentDescription = stringResource(R.string.cd_network_offline),
                    message = stringResource(R.string.network_offline_banner)
                )
            }
            NetworkBannerState.Reconnected -> {
                NetworkStatusBannerContent(
                    backgroundColor = AmberGold40,
                    contentColor = Color.White,
                    icon = Icons.Default.Wifi,
                    iconContentDescription = stringResource(R.string.cd_network_online),
                    message = stringResource(R.string.network_online_banner)
                )
            }
            NetworkBannerState.Hidden -> Unit
        }
    }
}

/**
 * Internal stateless container rendering the styled banner content pill.
 *
 * @param backgroundColor Container tint applied to the banner [Surface].
 * @param contentColor Tint applied to the icon and text elements for high contrast.
 * @param icon Leading [ImageVector] communicating connectivity state.
 * @param iconContentDescription Accessibility label for screen reader users.
 * @param message Localized string message rendered inside the pill.
 * @param modifier Optional modifier applied to the banner [Surface].
 */
@Composable
private fun NetworkStatusBannerContent(
    backgroundColor: Color,
    contentColor: Color,
    icon: ImageVector,
    iconContentDescription: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

// =============================================================================
// Comprehensive Compose Previews
// =============================================================================

@Preview(name = "Network Status Banner - Offline Light", showBackground = true)
@Composable
private fun NetworkStatusBannerOfflineLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        NetworkStatusBannerContent(
            backgroundColor = CrimsonGrey40,
            contentColor = Color.White,
            icon = Icons.Default.WifiOff,
            iconContentDescription = "Offline",
            message = "You are offline. Showing cached content."
        )
    }
}

@Preview(name = "Network Status Banner - Offline Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NetworkStatusBannerOfflineDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        NetworkStatusBannerContent(
            backgroundColor = CrimsonGrey40,
            contentColor = Color.White,
            icon = Icons.Default.WifiOff,
            iconContentDescription = "Offline",
            message = "You are offline. Showing cached content."
        )
    }
}

@Preview(name = "Network Status Banner - Reconnected Light", showBackground = true)
@Composable
private fun NetworkStatusBannerReconnectedLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        NetworkStatusBannerContent(
            backgroundColor = AmberGold40,
            contentColor = Color.White,
            icon = Icons.Default.Wifi,
            iconContentDescription = "Online",
            message = "Back online. Syncing catalog…"
        )
    }
}

@Preview(name = "Network Status Banner - Reconnected Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NetworkStatusBannerReconnectedDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        NetworkStatusBannerContent(
            backgroundColor = AmberGold40,
            contentColor = Color.White,
            icon = Icons.Default.Wifi,
            iconContentDescription = "Online",
            message = "Back online. Syncing catalog…"
        )
    }
}
