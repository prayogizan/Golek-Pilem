package com.prayogizan.golekpilem.core.util

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility responsible for inspecting [Throwable] exceptions and resolving them into strongly typed
 * [ErrorType] categories with user-facing localized string resources.
 */
object ErrorMessageResolver {

    /**
     * Resolves an arbitrary [Throwable] into a typed [ErrorType].
     *
     * Classifies network connectivity drops ([UnknownHostException], [ConnectException], [SocketTimeoutException]),
     * HTTP status codes from Retrofit [HttpException] (401 Unauthorized, 429 Rate Limited, 500..599 Server Outage),
     * and falls back cleanly to [ErrorType.Generic].
     *
     * @param throwable The exception or failure encountered.
     * @return The corresponding [ErrorType] classification.
     */
    fun resolve(throwable: Throwable?): ErrorType {
        return when (throwable) {
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException -> ErrorType.NetworkOffline

            is HttpException -> {
                when (throwable.code()) {
                    401 -> ErrorType.Unauthorized
                    429 -> ErrorType.RateLimited
                    in 500..599 -> ErrorType.ServerUnavailable
                    else -> ErrorType.Generic(throwable.message())
                }
            }

            is IOException -> ErrorType.NetworkOffline

            else -> ErrorType.Generic(throwable?.localizedMessage)
        }
    }
}
