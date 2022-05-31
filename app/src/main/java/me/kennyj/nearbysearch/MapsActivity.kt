package me.kennyj.nearbysearch

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (isFirstTimeLoad && currentLocation != null) {
            moveMapToCurrentLocation()
            isFirstTimeLoad = false
        }
    }

    private fun moveMapToCurrentLocation() {
        lifecycleScope.launchWhenStarted {
            delay(2000)
            withContext(Dispatchers.Main) {
                val currentLocationLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                mMap?.let {
                    it.addMarker(MarkerOptions().position(currentLocationLatLng))
                    it.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 16F))
                }
            }
        }
    }
}