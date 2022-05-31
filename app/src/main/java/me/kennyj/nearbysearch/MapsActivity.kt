package me.kennyj.nearbysearch

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.kennyj.nearbysearch.databinding.ActivityMapsBinding
import me.kennyj.nearbysearch.util.isLocationPermissionGranted

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityMapsBinding
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    private var isFirstTimeLoad: Boolean = true

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
    }

    @SuppressLint("MissingPermission")
    private fun listenForLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object: LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
                if (mMap != null && isFirstTimeLoad) {
                    moveMapToCurrentLocation()
                    isFirstTimeLoad = false
                }
            }

        })
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

    private fun moveMapToCurrentLocation() {
        lifecycleScope.launchWhenStarted {
//          wait for sometime before animating the map
//          gives a better user experience
            delay(2000)
            withContext(Dispatchers.Main) {
                val currentLocationLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                mMap?.let {
                    it.addMarker(MarkerOptions().position(currentLocationLatLng))
                    it.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 16F))
                }
            }

//          wait for the map to move to current location, then show FAB
//          wanted to do a fade in animation, but didn't get time
            delay(3000)
            binding.fabSearch.visibility = View.VISIBLE
        }
    }
}