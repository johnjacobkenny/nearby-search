package me.kennyj.nearbysearch.presentation

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kennyj.nearbysearch.data.api.APIClient
import me.kennyj.nearbysearch.data.api.NearbySearchService
import me.kennyj.nearbysearch.data.entities.NearbyRestaurantResponse
import me.kennyj.nearbysearch.util.getPlacesAPIKey

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val currentLocation: Location? = null
    val _appState: MutableLiveData<AppState> = MutableLiveData<AppState>(AppState.START)
    val appState: LiveData<AppState>
        get() = _appState
    var result: NearbyRestaurantResponse? = null

    private val nearbySearchSearchService: NearbySearchService by lazy {
        APIClient.getService()
    }

    fun setState(state: AppState) {
        _appState.postValue(state)
    }

    fun getNearbySearchResponse(location: Location) {
        _appState.postValue(AppState.SEARCHING_RESTAURANTS)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val locationString = "${location.latitude}, ${location.longitude}"
                val key = getPlacesAPIKey(getApplication<Application>().packageManager)!!
                result = nearbySearchSearchService.getNearbyRestaurants(key, locationString)
            }
            _appState.postValue(AppState.VIEW_RESTAURANTS)
        }
    }
}