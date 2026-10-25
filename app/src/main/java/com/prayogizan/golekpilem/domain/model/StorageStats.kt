package com.prayogizan.golekpilem.domain.model

/**
 * Encapsulates device storage metrics relating to offline catalog persistence and image disk caches.
 *
 * Provides raw byte metrics alongside sanitized, human-readable display strings.
 *
 * @property dbSizeBytes Database file size in bytes (including journal and write-ahead log files).
 * @property imageCacheSizeBytes Total size in bytes of files persisted in the Coil image cache.
 * @property dbSizeFormatted Pre-formatted human-readable database size string (e.g. "4.2 MB").
 * @property imageCacheSizeFormatted Pre-formatted human-readable image cache size string (e.g. "48.6 MB").
 * @property totalSizeFormatted Pre-formatted human-readable aggregate size string (e.g. "52.8 MB").
 */
data class StorageStats(
    val dbSizeBytes: Long,
    val imageCacheSizeBytes: Long,
    val dbSizeFormatted: String,
    val imageCacheSizeFormatted: String,
    val totalSizeFormatted: String
)
