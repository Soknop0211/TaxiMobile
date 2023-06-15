package com.eazy.daiku.ui.customer.step_booking

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.JsonReader
import android.view.MenuItem
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityLocationKioskBookingTaxiBinding
import com.eazy.daiku.ui.customer.model.ListKioskModel
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.other.AppLOGG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader


class LocationKioskBookingTaxiActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationKioskBookingTaxiBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private var listLocationKiosk: ArrayList<ListKioskModel>? = null
    private var marker: Marker? = null

    companion object {
        const val dataListLocationKioskKey = "dataListLocationKioskKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationKioskBookingTaxiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initGoogleMap()

    }

    fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.taxi_station),
            true
        )

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(self())
        if (intent != null && intent.hasExtra(dataListLocationKioskKey)) {
            val dataJson = intent.getStringExtra(dataListLocationKioskKey)

            val gsonConvert = Gson()
            val abaAccountType = object : TypeToken<ArrayList<ListKioskModel>>() {}.type
            listLocationKiosk = gsonConvert.fromJson(dataJson, abaAccountType)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.let {
            mMap?.uiSettings?.isMyLocationButtonEnabled = true
        }
        fetchCurrentLocation()
    }

    private fun initGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun fetchCurrentLocation() {
        @SuppressLint("MissingPermission")
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location: Location ->
            val latLng = LatLng(location.latitude, location.longitude)
            moveToCurrentLocation(latLng)
        }
    }

    private fun moveToCurrentLocation(latLng: LatLng) {
        val update = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap?.moveCamera(update)
        mMap?.animateCamera(zoom)
        listLocationKiosk?.let {
            for (listLocation in it) {
                listLocation.mapLat?.let { it1 ->
                    listLocation.mapLng?.let { it2 ->
                        LatLng(it1,
                            it2)
                    }
                }
                    ?.let { it2 -> listLocation.location?.let { it1 -> drawMarkerTaxi(it2, it1) } }
            }
        }
        showAllMarker()
    }

    private fun showAllMarker() {
        val builder = LatLngBounds.Builder()
        listLocationKiosk?.let {
            for (listKioskModel in it) {
                builder.include(listKioskModel.mapLat?.let { it1 ->
                    listKioskModel.mapLng?.let { it2 ->
                        LatLng(it1,
                            it2)
                    }
                })
            }
        }
        val bounds: LatLngBounds = builder.build()
        val with = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val panding = (with * 0.33).toInt()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, with, height, panding)
        mMap?.animateCamera(cu)

    }

    private fun drawMarkerTaxi(latLng: LatLng, location: String) {
        marker =
            mMap?.addMarker(MarkerOptions().position(latLng).title(location)
                .icon(bitmapDescriptorFromVector(R.drawable.ic_kiosk)))
        marker?.showInfoWindow()
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        val userPin = BitmapFactory.decodeResource(
            resources, vectorResId)
        val userPinMarker =
            Bitmap.createScaledBitmap(userPin, userPin.width / 3, userPin.height / 3, false)
        val userPinIcon = BitmapDescriptorFactory.fromBitmap(userPinMarker)
        return userPinIcon

    }

}