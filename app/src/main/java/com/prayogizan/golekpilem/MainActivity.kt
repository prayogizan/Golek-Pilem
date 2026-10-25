package com.prayogizan.golekpilem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.prayogizan.golekpilem.core.connectivity.ConnectivityObserver
import com.prayogizan.golekpilem.ui.common.NetworkStatusBanner
import com.prayogizan.golekpilem.ui.navigation.GolekPilemNavGraph
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme
import org.koin.android.ext.android.inject

/**
 * Main application activity hosting the top-level Compose navigation graph and root connectivity banner.
 */
class MainActivity : ComponentActivity() {

    private val connectivityObserver: ConnectivityObserver by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GolekPilemTheme {
                val networkStatus by connectivityObserver.status.collectAsStateWithLifecycle(
                    initialValue = ConnectivityObserver.Status.Available
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    Column(modifier = Modifier.fillMaxSize()) {
                        NetworkStatusBanner(networkStatus = networkStatus)
                        Box(modifier = Modifier.weight(1f)) {
                            GolekPilemNavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}