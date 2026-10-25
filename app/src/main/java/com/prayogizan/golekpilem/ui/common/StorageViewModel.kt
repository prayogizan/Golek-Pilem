package com.prayogizan.golekpilem.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.usecase.ClearImageCacheUseCase
import com.prayogizan.golekpilem.domain.usecase.GetStorageStatsUseCase
import com.prayogizan.golekpilem.domain.usecase.PurgeExpiredCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel managing the storage diagnostics dialog and cache maintenance operations.
 *
 * Implements Unidirectional Data Flow (UDF), coordinates storage size calculations,
 * image cache clearing, and safe catalog pruning via domain use cases.
 *
 * @param getStorageStatsUseCase Interactor for retrieving local storage utilization statistics.
 * @param clearImageCacheUseCase Interactor for purging Coil image disk and memory caches.
 * @param purgeExpiredCatalogUseCase Interactor for pruning stale catalog records.
 */
class StorageViewModel(
    private val getStorageStatsUseCase: GetStorageStatsUseCase,
    private val clearImageCacheUseCase: ClearImageCacheUseCase,
    private val purgeExpiredCatalogUseCase: PurgeExpiredCatalogUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorageUiState>(StorageUiState.Loading)

    /**
     * Immutable state flow exposing the current storage diagnostics UI state.
     */
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init {
        loadStorageStats()
    }

    /**
     * Dispatches user action events to trigger storage calculations or cache purging.
     *
     * @param event The [StorageUiEvent] dispatched by presentation composables.
     */
    fun onEvent(event: StorageUiEvent) {
        when (event) {
            is StorageUiEvent.LoadStats -> loadStorageStats()
            is StorageUiEvent.ClearImageCache -> clearImageCache()
            is StorageUiEvent.PurgeExpiredCatalog -> purgeExpiredCatalog(event.retentionDays)
            is StorageUiEvent.ClearUserMessage -> clearUserMessage()
        }
    }

    private fun loadStorageStats() {
        viewModelScope.launch {
            try {
                val stats = getStorageStatsUseCase()
                _uiState.value = StorageUiState.Success(stats = stats)
            } catch (e: Exception) {
                _uiState.value = StorageUiState.Error(
                    message = e.localizedMessage ?: "Failed to calculate storage usage."
                )
            }
        }
    }

    private fun clearImageCache() {
        val currentState = _uiState.value
        if (currentState is StorageUiState.Success) {
            _uiState.update { currentState.copy(isClearing = true) }
        }
        viewModelScope.launch {
            try {
                clearImageCacheUseCase()
                val refreshedStats = getStorageStatsUseCase()
                _uiState.value = StorageUiState.Success(
                    stats = refreshedStats,
                    isClearing = false,
                    userMessageRes = R.string.storage_clear_images_success
                )
            } catch (e: Exception) {
                if (currentState is StorageUiState.Success) {
                    _uiState.value = currentState.copy(isClearing = false)
                } else {
                    _uiState.value = StorageUiState.Error(
                        message = e.localizedMessage ?: "Failed to clear image cache."
                    )
                }
            }
        }
    }

    private fun purgeExpiredCatalog(retentionDays: Int) {
        val currentState = _uiState.value
        if (currentState is StorageUiState.Success) {
            _uiState.update { currentState.copy(isPurging = true) }
        }
        viewModelScope.launch {
            try {
                val purgedCount = purgeExpiredCatalogUseCase(retentionDays)
                val refreshedStats = getStorageStatsUseCase()
                _uiState.value = StorageUiState.Success(
                    stats = refreshedStats,
                    isPurging = false,
                    userMessageRes = R.string.storage_purge_catalog_success,
                    purgedCount = purgedCount
                )
            } catch (e: Exception) {
                if (currentState is StorageUiState.Success) {
                    _uiState.value = currentState.copy(isPurging = false)
                } else {
                    _uiState.value = StorageUiState.Error(
                        message = e.localizedMessage ?: "Failed to purge catalog."
                    )
                }
            }
        }
    }

    private fun clearUserMessage() {
        val currentState = _uiState.value
        if (currentState is StorageUiState.Success) {
            _uiState.update { (it as StorageUiState.Success).copy(userMessageRes = null) }
        }
    }
}
