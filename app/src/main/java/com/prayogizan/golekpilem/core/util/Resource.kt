package com.prayogizan.golekpilem.core.util

/**
 * Generic sealed class representing the lifecycle state of an asynchronous operation or data stream.
 *
 * Provides type-safe encapsulation for [Success], [Error], and [Loading] states.
 *
 * @param T The payload type wrapped within successful responses.
 */
sealed class Resource<out T> {

    /**
     * Successful operation containing the delivered [data].
     *
     * @property data The successfully retrieved or computed payload.
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * Failed operation containing an error [message] and an optional underlying [cause].
     *
     * @property message A human-readable description of the error.
     * @property cause The original exception or failure cause, if available.
     */
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()

    /**
     * Transient state representing an ongoing asynchronous operation.
     */
    data object Loading : Resource<Nothing>()

    /**
     * Returns the contained data if this instance is [Success], or `null` otherwise.
     */
    fun dataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Transforms the successful data using [transform], propagating error or loading states intact.
     */
    inline fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }
}
