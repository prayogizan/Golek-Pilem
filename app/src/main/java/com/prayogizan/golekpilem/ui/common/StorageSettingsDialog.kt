package com.prayogizan.golekpilem.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.StorageStats
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful route wrapper for the storage diagnostics dialog.
 *
 * Injects [StorageViewModel] via Koin, collects reactive [StorageUiState] with lifecycle awareness,
 * and passes events and dismiss callbacks to the stateless [StorageSettingsDialog].
 *
 * @param onDismissRequest Callback invoked when the user dismisses the dialog.
 * @param modifier Optional modifier applied to the dialog container.
 * @param viewModel Presentation ViewModel injected via Koin.
 */
@Composable
fun StorageSettingsRoute(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StorageViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StorageSettingsDialog(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}

/**
 * Stateless dialog presenting storage diagnostics and disk cache maintenance controls.
 *
 * Displays physical file breakdown (Room database, Coil image disk cache, and total usage),
 * and provides one-tap actions to purge image cache and prune stale feed entries while safely
 * preserving user watchlists.
 *
 * @param uiState Current immutable UI state representing storage statistics and progress.
 * @param onEvent Lambda callback for dispatching user actions ([StorageUiEvent]) to the ViewModel.
 * @param onDismissRequest Callback invoked when the dialog is dismissed.
 * @param modifier Optional modifier applied to the dialog container.
 */
@Composable
fun StorageSettingsDialog(
    uiState: StorageUiState,
    onEvent: (StorageUiEvent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Storage,
                contentDescription = stringResource(R.string.cd_storage_icon),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.storage_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (uiState) {
                    is StorageUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is StorageUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            OutlinedButton(onClick = { onEvent(StorageUiEvent.LoadStats) }) {
                                Text(text = stringResource(R.string.error_retry_button))
                            }
                        }
                    }
                    is StorageUiState.Success -> {
                        // User feedback message banner with auto-dismiss
                        if (uiState.userMessageRes != null) {
                            LaunchedEffect(uiState.userMessageRes) {
                                delay(3000)
                                onEvent(StorageUiEvent.ClearUserMessage)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val messageText = if (uiState.purgedCount != null) {
                                    stringResource(uiState.userMessageRes, uiState.purgedCount)
                                } else {
                                    stringResource(uiState.userMessageRes)
                                }
                                Text(
                                    text = messageText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Storage Metrics Breakdown Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.storage_database_size, ""),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.stats.dbSizeFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.storage_image_cache_size, ""),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.stats.imageCacheSizeFormatted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.storage_total_size, ""),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = uiState.stats.totalSizeFormatted,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.storage_purge_catalog_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Action Controls
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onEvent(StorageUiEvent.ClearImageCache) },
                                enabled = !uiState.isClearing && !uiState.isPurging,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isClearing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.CleaningServices,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(text = stringResource(R.string.storage_clear_images_button))
                            }

                            FilledTonalButton(
                                onClick = { onEvent(StorageUiEvent.PurgeExpiredCatalog(retentionDays = 7)) },
                                enabled = !uiState.isClearing && !uiState.isPurging,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isPurging) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(text = stringResource(R.string.storage_purge_catalog_button))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.storage_close_button))
            }
        },
        modifier = modifier
    )
}

// =============================================================================
// Comprehensive Compose Previews
// =============================================================================

private val previewSampleStats = StorageStats(
    dbSizeBytes = 4_404_019L,
    imageCacheSizeBytes = 50_960_793L,
    dbSizeFormatted = "4.2 MB",
    imageCacheSizeFormatted = "48.6 MB",
    totalSizeFormatted = "52.8 MB"
)

@Preview(name = "StorageSettingsDialog - Light", showBackground = true)
@Composable
private fun StorageSettingsDialogLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        StorageSettingsDialog(
            uiState = StorageUiState.Success(stats = previewSampleStats),
            onEvent = {},
            onDismissRequest = {}
        )
    }
}

@Preview(name = "StorageSettingsDialog - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StorageSettingsDialogDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        StorageSettingsDialog(
            uiState = StorageUiState.Success(
                stats = previewSampleStats,
                userMessageRes = R.string.storage_clear_images_success
            ),
            onEvent = {},
            onDismissRequest = {}
        )
    }
}

@Preview(name = "StorageSettingsDialog - Loading", showBackground = true)
@Composable
private fun StorageSettingsDialogLoadingPreview() {
    GolekPilemTheme(darkTheme = false) {
        StorageSettingsDialog(
            uiState = StorageUiState.Loading,
            onEvent = {},
            onDismissRequest = {}
        )
    }
}
