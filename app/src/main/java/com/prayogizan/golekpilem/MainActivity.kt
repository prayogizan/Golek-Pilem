package com.prayogizan.golekpilem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.prayogizan.golekpilem.ui.navigation.GolekPilemNavGraph
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Main application activity hosting the top-level Compose navigation graph.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GolekPilemTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    GolekPilemNavGraph(navController = navController)
                }
            }
        }
    }
}