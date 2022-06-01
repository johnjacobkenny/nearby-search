package me.kennyj.nearbysearch.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.kennyj.nearbysearch.R
import me.kennyj.nearbysearch.databinding.ActivityMapsBinding
import me.kennyj.nearbysearch.util.isLocationPermissionGranted

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityMapsBinding
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    private var isFirstTimeLoad: Boolean = true
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (isLocationPermissionGranted(this)) {
            listenForLocationUpdates()
        }

        binding.apply {
            fabSearch.setOnClickListener {
                if (currentLocation != null)
                    viewModel.getNearbySearchResponse(currentLocation!!)
            }

            fabHome.setOnClickListener {
                viewModel.setState(AppState.VIEW_CURRENT_LOCATION)
            }

            fabHistory.setOnClickListener {
                viewModel.getLocationHistory()
            }
        }

        viewModel.appState.observe(this) {
            when (it) {
                AppState.START -> {
                    Toast.makeText(this@MapsActivity, "Getting Location", Toast.LENGTH_LONG).show()
                }
                AppState.VIEW_CURRENT_LOCATION -> {
                    binding.fabSearch.visibility = View.VISIBLE
                    binding.fabHome.visibility = View.INVISIBLE
                    binding.rvHistory.visibility = View.INVISIBLE
                    moveMapToCurrentLocation()
                }
                AppState.SEARCHING_RESTAURANTS -> {
                    binding.rvHistory.visibility = View.INVISIBLE
                    binding.fabSearch.visibility = View.INVISIBLE
                    Toast.makeText(
                        this@MapsActivity,
                        "Searching for restaurants",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.addLocation(currentLocation!!)
                }
                AppState.VIEW_RESTAURANTS -> {
                    binding.rvHistory.visibility = View.INVISIBLE
                    binding.fabSearch.visibility = View.INVISIBLE
                    mMap?.clear()
                    val latLngBoundsBuilder = LatLngBounds.Builder()
                    viewModel.result?.results?.forEach { result ->
                        val location = result.geometry.location
                        val latLng = LatLng(location.lat, location.lng)
                        addMarker(latLng, result.name)
                        latLngBoundsBuilder.include(latLng)
                    }
                    mMap?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            latLngBoundsBuilder.build(),
                            100
                        ), 1500, null
                    )
                    lifecycleScope.launchWhenStarted {
                        delay(2000)
                        binding.fabHome.visibility = View.VISIBLE
                    }
                }
                AppState.LOADING_HISTORY -> {

                }
                AppState.VIEW_HISTORY -> {
                    binding.rvHistory.visibility = View.VISIBLE
                    val data = viewModel.locationHistory
                    Log.d(TAG, "onCreate: $data")
                    if (data != null) {
                        val adapter = HistoryListAdapter(data)
                        binding.rvHistory.adapter = adapter
                    } else {
                        Toast.makeText(this@MapsActivity, "No entries in history", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun listenForLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            0F
        ) { location ->
            currentLocation = location
            if (mMap != null && isFirstTimeLoad) {
                lifecycleScope.launchWhenStarted {
                    //                          wait for sometime before animating the map
                    //                          gives a better user experience
                    delay(2000)
                    moveMapToCurrentLocation()
                    isFirstTimeLoad = false
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            listenForLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (isFirstTimeLoad && currentLocation != null) {
            moveMapToCurrentLocation()
            isFirstTimeLoad = false
        }
    }

    private fun addMarker(location: LatLng, name: String) {
        mMap?.addMarker(MarkerOptions().position(location).title(name))
    }

    private fun moveMapToCurrentLocation() {
        mMap?.clear()
        lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.Main) {
                val currentLocationLatLng =
                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                mMap?.let {
                    it.addMarker(MarkerOptions().position(currentLocationLatLng))
                    it.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 16F))
                }
            }

//          wait for the map to move to current location, then show FAB
//          wanted to do a fade in animation, but didn't get time
            delay(3000)
            binding.fabSearch.visibility = View.VISIBLE
            binding.fabHistory.visibility = View.VISIBLE
        }
    }
}