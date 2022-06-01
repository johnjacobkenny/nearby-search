package me.kennyj.nearbysearch.presentation

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kennyj.nearbysearch.data.location.LocationDatabase
import me.kennyj.nearbysearch.data.search.api.APIClient
import me.kennyj.nearbysearch.data.search.api.NearbySearchService
import me.kennyj.nearbysearch.data.search.entities.NearbyRestaurantResponse
import me.kennyj.nearbysearch.util.getPlacesAPIKey

private const val TAG = "AppViewModel"
class AppViewModel(application: Application) : AndroidViewModel(application) {
    val currentLocation: Location? = null
    private val _appState: MutableLiveData<AppState> = MutableLiveData<AppState>(AppState.START)
    private val db = Room.databaseBuilder(
        getApplication<Application>().applicationContext,
        LocationDatabase::class.java,
        "location-db"
    ).build()
    val appState: LiveData<AppState>
        get() = _appState
    var result: NearbyRestaurantResponse? = null
    var locationHistory: List<Location>? = null

    private val nearbySearchSearchService: NearbySearchService by lazy {
        APIClient.getService()
    }

    fun addLocation(location: Location) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.locationDao().insert(
                    me.kennyj.nearbysearch.data.location.Location(
                        location.latitude,
                        location.longitude
                    )
                )
            }
        }
    }

    fun getLocationHistory() {
        _appState.postValue(AppState.LOADING_HISTORY)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val locations = db.locationDao().getLastTenLocations()
                locationHistory = locations.map { loc ->
                    Location("").also {
                        it.latitude = loc.latitude
                        it.longitude = loc.longitude
                    }
                }

                _appState.postValue(AppState.VIEW_HISTORY)
            }
        }
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