package com.prayogizan.golekpilem.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Concrete implementation of [ConnectivityObserver] using Android's [ConnectivityManager].
 *
 * Listens for system network capability changes via [ConnectivityManager.NetworkCallback]
 * wrapped in a coroutine [callbackFlow], ensuring zero memory leaks through [awaitClose].
 *
 * @param context Application context used to retrieve the [ConnectivityManager] system service.
 * @param networkRequestBuilder Factory lambda creating the [NetworkRequest] criteria for observation.
 */
class NetworkConnectivityObserver(
    private val context: Context,
    private val networkRequestBuilder: () -> NetworkRequest = {
        NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
    }
) : ConnectivityObserver {

    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override val status: Flow<ConnectivityObserver.Status> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(ConnectivityObserver.Status.Unavailable)
            close()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectivityObserver.Status.Available)
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityObserver.Status.Lost)
            }

            override fun onUnavailable() {
                trySend(ConnectivityObserver.Status.Unavailable)
            }
        }

        val request = try {
            networkRequestBuilder()
        } catch (e: Exception) {
            null
        }

        if (request != null) {
            try {
                cm.registerNetworkCallback(request, callback)
            } catch (e: Exception) {
                trySend(ConnectivityObserver.Status.Unavailable)
            }
        }

        // Emit immediate initial status upon subscription to eliminate collection lag
        val initialStatus = getCurrentStatus(cm)
        trySend(initialStatus)

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore if already unregistered or if connectivity service disconnected
            }
        }
    }.distinctUntilChanged()

    private fun getCurrentStatus(cm: ConnectivityManager): ConnectivityObserver.Status {
        val activeNetwork = cm.activeNetwork ?: return ConnectivityObserver.Status.Unavailable
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return ConnectivityObserver.Status.Unavailable
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            ConnectivityObserver.Status.Available
        } else {
            ConnectivityObserver.Status.Unavailable
        }
    }
}
