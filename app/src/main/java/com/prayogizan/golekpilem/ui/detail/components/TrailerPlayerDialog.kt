package com.prayogizan.golekpilem.ui.detail.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Video

/**
 * Immersive, fullscreen landscape video player overlay for trailers and promotional video clips.
 *
 * Renders directly inside the hosting Activity's window rather than a detached platform dialog,
 * guaranteeing seamless layout scaling, immunity to dialog window detachment upon rotation,
 * and edge-to-edge playback.
 *
 * Temporarily locks the hosting Activity into sensor-driven landscape orientation and hides
 * system bars (immersive sticky mode). Restores the original orientation and window decorations
 * upon dismissal via back gesture, system back button, or explicit close action.
 *
 * @param video The [Video] domain entity to stream in the player.
 * @param onDismiss Callback invoked when the user exits fullscreen playback.
 * @param onNavigateToOfficial Callback invoked when user chooses to open the official video externally.
 * @param modifier Optional modifier applied to the player root container.
 */
@Composable
fun FullScreenTrailerPlayer(
    video: Video,
    onDismiss: () -> Unit,
    onNavigateToOfficial: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Intercept back gesture / hardware back press to dismiss fullscreen
    BackHandler(onBack = onDismiss)

    // Manage orientation lock and immersive system bars mode
    DisposableEffect(activity) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen In-App YouTube Player
        InAppYouTubePlayer(
            videoKey = video.key,
            autoPlay = true,
            modifier = Modifier.fillMaxSize()
        )

        // Overlaid Floating Top Bar Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            // Circular Close Action Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close_player),
                    tint = Color.White
                )
            }

            // Video Title
            Text(
                text = video.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            // Navigate to Official Video on YouTube Button
            OutlinedButton(
                onClick = { onNavigateToOfficial(video) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.65f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.detail_watch_on_youtube),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Backward compatibility alias for [FullScreenTrailerPlayer].
 */
@Composable
fun TrailerPlayerDialog(
    video: Video,
    onDismiss: () -> Unit,
    onNavigateToOfficial: (Video) -> Unit
) {
    FullScreenTrailerPlayer(
        video = video,
        onDismiss = onDismiss,
        onNavigateToOfficial = onNavigateToOfficial
    )
}

/**
 * Traverses the Context wrapper hierarchy to find the enclosing host [Activity].
 */
internal fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
