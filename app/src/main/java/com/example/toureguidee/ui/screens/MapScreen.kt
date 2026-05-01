package com.example.toureguidee.ui.screens

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    attractions: List<com.example.toureguidee.data.model.Attraction>,
    currentLocation: Location?,
    onLocationRequested: () -> Unit,
    onAttractionClick: (com.example.toureguidee.data.model.Attraction) -> Unit
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val cameraPositionState = rememberCameraPositionState {
        position = if (currentLocation != null) {
            CameraPosition.fromLatLngZoom(
                LatLng(currentLocation.latitude, currentLocation.longitude),
                15f
            )
        } else {
            // Default to Vilnius center
            CameraPosition.fromLatLngZoom(LatLng(54.6872, 25.2797), 12f)
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15f
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        if (locationPermissionState.status.isGranted) {
                            onLocationRequested()
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    }) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Текущее местоположение"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionState.status.isGranted),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // Markers for attractions
                attractions.forEach { attraction ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(attraction.latitude, attraction.longitude)
                        ),
                        title = attraction.name,
                        snippet = attraction.description,
                        onClick = {
                            onAttractionClick(attraction)
                            true
                        }
                    )
                }

                // Current location marker
                currentLocation?.let { location ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        title = "Вы здесь",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }

            if (!locationPermissionState.status.isGranted) {
                PermissionRationaleCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    permissionState = locationPermissionState
                )
            }
        }
    }
}

@Composable
private fun PermissionRationaleCard(
    modifier: Modifier = Modifier,
    permissionState: com.google.accompanist.permissions.PermissionState
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Требуется доступ к местоположению",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Для работы приложения необходимо предоставить доступ к геолокации, чтобы отображать достопримечательности на карте и уведомлять вас о ближайших объектах.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = { permissionState.launchPermissionRequest() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Предоставить")
            }
        }
    }
}
