package com.prayogizan.golekpilem.core.util

import androidx.annotation.StringRes
import com.prayogizan.golekpilem.R

/**
 * Categorization of application and network errors to drive localized UI messaging and recovery workflows.
 *
 * @property titleRes String resource ID representing the error heading.
 * @property messageRes String resource ID representing the descriptive error message.
 * @property isNetworkIssue Indicates whether the failure stems from a connection drop or offline state.
 */
sealed class ErrorType(
    @get:StringRes val titleRes: Int,
    @get:StringRes val messageRes: Int,
    val isNetworkIssue: Boolean = false
) {
    /**
     * Device is disconnected, in airplane mode, or network socket timed out.
     */
    data object NetworkOffline : ErrorType(
        titleRes = R.string.error_no_internet_title,
        messageRes = R.string.error_no_internet,
        isNetworkIssue = true
    )

    /**
     * TMDB API token is missing, invalid, or unauthorized (HTTP 401).
     */
    data object Unauthorized : ErrorType(
        titleRes = R.string.error_auth_title,
        messageRes = R.string.error_unauthorized
    )

    /**
     * Request quota exceeded or rate limit hit on TMDB API (HTTP 429).
     */
    data object RateLimited : ErrorType(
        titleRes = R.string.error_rate_limit_title,
        messageRes = R.string.error_rate_limited
    )

    /**
     * Remote TMDB server outage or gateway error (HTTP 500..599).
     */
    data object ServerUnavailable : ErrorType(
        titleRes = R.string.error_server_title,
        messageRes = R.string.error_server_unavailable
    )

    /**
     * Fallback generic error.
     *
     * @property customMessage Optional explicit error message if provided by lower layers.
     */
    data class Generic(
        val customMessage: String? = null
    ) : ErrorType(
        titleRes = R.string.error_default_title,
        messageRes = R.string.error_default_message
    )
}
