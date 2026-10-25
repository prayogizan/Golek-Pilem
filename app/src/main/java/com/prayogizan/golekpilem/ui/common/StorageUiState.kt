package com.prayogizan.golekpilem.ui.common

import androidx.annotation.StringRes
import com.prayogizan.golekpilem.domain.model.StorageStats

/**
 * Immutable UI state contract modeling the storage diagnostics dialog.
 */
sealed interface StorageUiState {

    /**
     * Initial or recalculating state during storage measurement.
     */
    data object Loading : StorageUiState

    /**
     * Successfully resolved storage metrics and active operations state.
     *
     * @property stats Calculated physical storage statistics for DB and image cache.
     * @property isClearing Indicates whether an image cache purge operation is in progress.
     * @property isPurging Indicates whether a catalog pruning operation is in progress.
     * @property userMessageRes String resource identifier for transient feedback snackbar/toast, if any.
     * @property purgedCount Number of catalog items removed during the last purge, or `null`.
     */
    data class Success(
        val stats: StorageStats,
        val isClearing: Boolean = false,
        val isPurging: Boolean = false,
        @param:StringRes val userMessageRes: Int? = null,
        val purgedCount: Int? = null
    ) : StorageUiState

    /**
     * Failure state when storage metrics resolution encounters an error.
     *
     * @property message Descriptive failure explanation.
     */
    data class Error(val message: String) : StorageUiState
}

/**
 * UI user action events dispatched to [StorageViewModel].
 */
sealed interface StorageUiEvent {

    /**
     * Requests fresh calculation of storage metrics.
     */
    data object LoadStats : StorageUiEvent

    /**
     * Requests clearance of Coil disk and memory caches.
     */
    data object ClearImageCache : StorageUiEvent

    /**
     * Requests pruning of catalog feed entries older than [retentionDays], exempting watchlists.
     *
     * @property retentionDays The maximum retention age in days (defaults to 7).
     */
    data class PurgeExpiredCatalog(val retentionDays: Int = 7) : StorageUiEvent

    /**
     * Dismisses any active transient user feedback message.
     */
    data object ClearUserMessage : StorageUiEvent
}
