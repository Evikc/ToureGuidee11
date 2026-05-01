package com.example.toureguidee.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.toureguidee.data.database.AttractionDatabase
import com.example.toureguidee.data.model.Attraction
import com.example.toureguidee.data.repository.AttractionRepository
import com.example.toureguidee.location.LocationTracker
import com.example.toureguidee.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttractionRepository
    private val locationTracker: LocationTracker
    private val notificationHelper: NotificationHelper

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _nearbyAttractions = MutableStateFlow<List<Attraction>>(emptyList())
    val nearbyAttractions: StateFlow<List<Attraction>> = _nearbyAttractions.asStateFlow()

    private val notifiedAttractionIds = mutableSetOf<Long>()
    private var lastNotifiedTime = 0L
    private val NOTIFICATION_COOLDOWN_MS = 60000L // 1 minute cooldown

    init {
        val database = AttractionDatabase.getDatabase(application)
        repository = AttractionRepository(database.attractionDao())
        locationTracker = LocationTracker(application)
        notificationHelper = NotificationHelper(application)

        viewModelScope.launch {
            repository.allAttractions.collect { attractions ->
                _uiState.update { it.copy(attractions = attractions) }
                checkNearbyAttractions(attractions)
            }
        }
    }

    fun addAttraction(name: String, description: String, latitude: Double, longitude: Double, imageUrl: String? = null) {
        viewModelScope.launch {
            val attraction = Attraction(
                name = name,
                description = description,
                latitude = latitude,
                longitude = longitude,
                imageUrl = imageUrl,
                isUserAdded = true
            )
            repository.insertAttraction(attraction)
        }
    }

    fun deleteAttraction(attraction: Attraction) {
        viewModelScope.launch {
            repository.deleteAttraction(attraction)
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(currentLatitude = latitude, currentLongitude = longitude) }
        checkNearbyAttractions(_uiState.value.attractions)
    }

    private fun checkNearbyAttractions(attractions: List<Attraction>) {
        val currentLat = _uiState.value.currentLatitude ?: return
        val currentLon = _uiState.value.currentLongitude ?: return

        val nearby = attractions.filter { attraction ->
            val distance = locationTracker.calculateDistance(
                currentLat, currentLon,
                attraction.latitude, attraction.longitude
            )
            distance <= 500 // 500 meters radius
        }

        _nearbyAttractions.value = nearby

        // Send notifications for new nearby attractions
        val currentTime = System.currentTimeMillis()
        nearby.forEach { attraction ->
            if (attraction.id !in notifiedAttractionIds || 
                (currentTime - lastNotifiedTime) > NOTIFICATION_COOLDOWN_MS) {
                
                val distance = locationTracker.calculateDistance(
                    currentLat, currentLon,
                    attraction.latitude, attraction.longitude
                ).toInt()
                
                notificationHelper.showAttractionNotification(attraction.name, distance)
                notifiedAttractionIds.add(attraction.id)
                lastNotifiedTime = currentTime
            }
        }
    }

    fun clearNotifiedAttractions() {
        notifiedAttractionIds.clear()
    }

    data class UiState(
        val attractions: List<Attraction> = emptyList(),
        val currentLatitude: Double? = null,
        val currentLongitude: Double? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
