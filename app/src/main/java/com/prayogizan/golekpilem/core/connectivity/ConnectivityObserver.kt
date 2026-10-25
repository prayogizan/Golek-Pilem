package com.prayogizan.golekpilem.core.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Interface contract for observing reactive network connectivity status.
 *
 * Provides a continuous, cold [Flow] emitting the real-time connectivity status of the device.
 */
interface ConnectivityObserver {

    /**
     * Cold stream emitting [Status] changes as system network availability shifts.
     */
    val status: Flow<Status>

    /**
     * Enumerates potential physical network connectivity states.
     */
    enum class Status {
        /** Active internet-capable network connection is present. */
        Available,

        /** Previously active connection was lost or disconnected. */
        Lost,

        /** No network connection is available or device is offline. */
        Unavailable
    }
}
