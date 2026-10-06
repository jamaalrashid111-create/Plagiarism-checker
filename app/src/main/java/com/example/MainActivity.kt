package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.screens.CheckingScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MunasarBlueContainer
import com.example.ui.theme.MunasarBluePrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                MunasarApp(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

@Composable
fun MunasarApp(
    viewModel: MainViewModel,
    uiState: MainUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle Error and Info Snackbars
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissErrorMessage()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissInfoMessage()
        }
    }

    // Android Hardware / Gesture Back Navigation
    BackHandler(enabled = uiState.isChecking || uiState.showSourcesScreen || uiState.currentTab != NavigationTab.HOME) {
        when {
            uiState.isChecking -> viewModel.cancelCheck()
            uiState.showSourcesScreen -> viewModel.toggleSourcesScreen(false)
            uiState.currentTab != NavigationTab.HOME -> viewModel.selectTab(NavigationTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!uiState.isChecking) {
                MunasarBottomNavigation(
                    currentTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        if (uiState.isChecking) {
            CheckingScreen(
                uiState = uiState,
                viewModel = viewModel,
                modifier = modifier
            )
        } else {
            when (uiState.currentTab) {
                NavigationTab.HOME -> HomeScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = modifier
                )
                NavigationTab.HISTORY -> HistoryScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = modifier
                )
                NavigationTab.REPORTS -> ReportScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = modifier
                )
                NavigationTab.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun MunasarBottomNavigation(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("munasar_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.HOME,
            onClick = { onTabSelected(NavigationTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MunasarBluePrimary,
                selectedTextColor = MunasarBluePrimary,
                indicatorColor = MunasarBlueContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.HISTORY,
            onClick = { onTabSelected(NavigationTab.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MunasarBluePrimary,
                selectedTextColor = MunasarBluePrimary,
                indicatorColor = MunasarBlueContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_history")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.REPORTS,
            onClick = { onTabSelected(NavigationTab.REPORTS) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
            label = { Text("Reports") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MunasarBluePrimary,
                selectedTextColor = MunasarBluePrimary,
                indicatorColor = MunasarBlueContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_reports")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.SETTINGS,
            onClick = { onTabSelected(NavigationTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MunasarBluePrimary,
                selectedTextColor = MunasarBluePrimary,
                indicatorColor = MunasarBlueContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}
