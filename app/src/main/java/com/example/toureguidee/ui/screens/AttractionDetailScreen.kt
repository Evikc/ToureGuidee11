package com.example.toureguidee.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.toureguidee.data.model.Attraction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionDetailScreen(
    attraction: Attraction,
    currentLatitude: Double?,
    currentLongitude: Double?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val distance = remember(attraction, currentLatitude, currentLongitude) {
        if (currentLatitude != null && currentLongitude != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                currentLatitude, currentLongitude,
                attraction.latitude, attraction.longitude,
                results
            )
            results[0].toInt()
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Информация") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    distance?.let {
                        AssistChip(
                            onClick = { },
                            label = { Text("Расстояние: $it м") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Directions,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Divider()

                    Text(
                        text = "Описание:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = attraction.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Координаты:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Широта: ${"%.6f".format(attraction.latitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Долгота: ${"%.6f".format(attraction.longitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val uri = Uri.parse(
                        "google.navigation:q=${attraction.latitude},${attraction.longitude}"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        // Fallback to browser
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Directions,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Проложить маршрут")
            }
        }
    }
}
