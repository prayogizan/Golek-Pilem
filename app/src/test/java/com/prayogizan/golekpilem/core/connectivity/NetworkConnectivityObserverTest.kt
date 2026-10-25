package com.prayogizan.golekpilem.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConnectivityObserverTest {

    private val context: Context = mockk(relaxed = true)
    private val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    private val mockNetwork: Network = mockk()
    private val mockCapabilities: NetworkCapabilities = mockk()
    private val mockNetworkRequest: NetworkRequest = mockk()
    private val callbackSlot = slot<ConnectivityManager.NetworkCallback>()

    @Before
    fun setUp() {
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.registerNetworkCallback(any<NetworkRequest>(), capture(callbackSlot)) } returns Unit
        every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } returns Unit
        every { connectivityManager.activeNetwork } returns mockNetwork
        every { connectivityManager.getNetworkCapabilities(mockNetwork) } returns mockCapabilities
        every { mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }

    private fun createObserver(ctx: Context = context): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(
            context = ctx,
            networkRequestBuilder = { mockNetworkRequest }
        )
    }

    @Test
    fun `status emits initial Available status when internet capability is present`() = runTest {
        val observer = createObserver()

        observer.status.test {
            assertEquals(ConnectivityObserver.Status.Available, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `status emits initial Unavailable status when active network is null`() = runTest {
        every { connectivityManager.activeNetwork } returns null

        val observer = createObserver()

        observer.status.test {
            assertEquals(ConnectivityObserver.Status.Unavailable, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `status emits dynamic transitions upon callback invocations`() = runTest {
        val observer = createObserver()

        observer.status.test {
            // Initial emission
            assertEquals(ConnectivityObserver.Status.Available, awaitItem())

            // Network dropped
            callbackSlot.captured.onLost(mockNetwork)
            assertEquals(ConnectivityObserver.Status.Lost, awaitItem())

            // Network restored
            callbackSlot.captured.onAvailable(mockNetwork)
            assertEquals(ConnectivityObserver.Status.Available, awaitItem())

            // Network unavailable
            callbackSlot.captured.onUnavailable()
            assertEquals(ConnectivityObserver.Status.Unavailable, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `status unregisters callback upon flow cancellation`() = runTest {
        val observer = createObserver()

        observer.status.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { connectivityManager.unregisterNetworkCallback(callbackSlot.captured) }
    }

    @Test
    fun `status emits Unavailable and completes if ConnectivityManager is null`() = runTest {
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null

        val observer = createObserver()

        observer.status.test {
            assertEquals(ConnectivityObserver.Status.Unavailable, awaitItem())
            awaitComplete()
        }
    }
}
