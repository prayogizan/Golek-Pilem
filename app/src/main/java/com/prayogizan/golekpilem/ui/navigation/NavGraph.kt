package com.prayogizan.golekpilem.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.prayogizan.golekpilem.R

/**
 * Top-level navigation graph configuring destinations and transitions for Golek Pilem.
 *
 * Adheres strictly to the architectural directive of injecting navigation lambdas rather
 * than propagating the [NavHostController] instance down through child composables.
 *
 * @param navController The navigation controller orchestrating back stack and destination transitions.
 * @param modifier Optional modifier applied to the root [NavHost].
 * @param mainContent Optional custom composable slot for [Screen.Main] (defaults to placeholder feed).
 * @param detailContent Optional custom composable slot for [Screen.Detail] (defaults to placeholder detail view).
 */
@Composable
fun GolekPilemNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainContent: (@Composable (onItemClick: (id: Int, mediaType: String) -> Unit) -> Unit)? = null,
    detailContent: (@Composable (detail: Screen.Detail, onBackClick: () -> Unit) -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main,
        modifier = modifier
    ) {
        composable<Screen.Main> {
            if (mainContent != null) {
                mainContent { id, mediaType ->
                    navController.navigate(Screen.Detail(id = id, mediaType = mediaType))
                }
            } else {
                MainPlaceholderScreen(
                    onNavigateToDetail = { id, mediaType ->
                        navController.navigate(Screen.Detail(id = id, mediaType = mediaType))
                    }
                )
            }
        }

        composable<Screen.Detail> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<Screen.Detail>()
            if (detailContent != null) {
                detailContent(detailRoute) {
                    navController.popBackStack()
                }
            } else {
                DetailPlaceholderScreen(
                    detail = detailRoute,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Default placeholder screen rendered for [Screen.Main] until Phase 3 feed implementation (Issue #6).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPlaceholderScreen(
    onNavigateToDetail: (id: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_main_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.nav_main_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigateToDetail(533535, "movie") }
                ) {
                    Text(text = "Navigate to Sample Detail (ID: 533535)")
                }
            }
        }
    }
}

/**
 * Default placeholder screen rendered for [Screen.Detail] until Phase 3 detail implementation (Issue #7).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPlaceholderScreen(
    detail: Screen.Detail,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.nav_detail_placeholder_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(
                        R.string.nav_detail_placeholder_description,
                        detail.mediaType,
                        detail.id
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
