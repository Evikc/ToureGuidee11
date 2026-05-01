package com.example.toureguidee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.toureguidee.data.model.Attraction
import com.example.toureguidee.location.LocationTracker
import com.example.toureguidee.notification.NotificationHelper
import com.example.toureguidee.ui.screens.AddAttractionScreen
import com.example.toureguidee.ui.screens.AttractionDetailScreen
import com.example.toureguidee.ui.screens.AttractionListScreen
import com.example.toureguidee.ui.screens.MapScreen
import com.example.toureguidee.ui.theme.ToureGuideeTheme
import com.example.toureguidee.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object List : Screen("list")
    object AddAttraction : Screen("add_attraction")
    object AttractionDetail : Screen("attraction_detail/{attractionId}") {
        fun createRoute(attractionId: Long) = "attraction_detail/$attractionId"
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var locationTracker: LocationTracker
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationTracker = LocationTracker(this)
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        setContent {
            ToureGuideeTheme {
                AppNavigation(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (locationTracker.hasLocationPermission()) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        lifecycleScope.launch {
            locationTracker.getLocationUpdates().collect { location ->
                viewModel.updateLocation(location.latitude, location.longitude)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val nearbyAttractions by viewModel.nearbyAttractions.collectAsState()
    
    val currentRoute = navController.currentBackStackEntryFlow.collectAsState(initial = null).value?.destination?.route
    val showBottomBar = currentRoute == Screen.Map.route || currentRoute == Screen.List.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Map.route) {
                MapScreen(
                    attractions = uiState.attractions,
                    currentLocation = null,
                    onLocationRequested = { },
                    onAttractionClick = { attraction ->
                        navController.navigate(Screen.AttractionDetail.createRoute(attraction.id))
                    }
                )
            }

            composable(Screen.List.route) {
                AttractionListScreen(
                    viewModel = viewModel,
                    onNavigateToAddAttraction = {
                        navController.navigate(Screen.AddAttraction.route)
                    },
                    onAttractionClick = { attraction ->
                        navController.navigate(Screen.AttractionDetail.createRoute(attraction.id))
                    }
                )
            }

            composable(Screen.AddAttraction.route) {
                AddAttractionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveAttraction = { name, description, latitude, longitude ->
                        viewModel.addAttraction(name, description, latitude, longitude)
                    }
                )
            }

            composable(Screen.AttractionDetail.route) { backStackEntry ->
                val attractionId = backStackEntry.arguments?.getString("attractionId")?.toLongOrNull()
                val attraction = uiState.attractions.find { it.id == attractionId }

                if (attraction != null) {
                    AttractionDetailScreen(
                        attraction = attraction,
                        currentLatitude = uiState.currentLatitude,
                        currentLongitude = uiState.currentLongitude,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Map, contentDescription = "Карта") },
            label = { Text("Карта") },
            selected = currentRoute == Screen.Map.route,
            onClick = { onNavigate(Screen.Map.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Список") },
            label = { Text("Список") },
            selected = currentRoute == Screen.List.route,
            onClick = { onNavigate(Screen.List.route) }
        )
    }
}