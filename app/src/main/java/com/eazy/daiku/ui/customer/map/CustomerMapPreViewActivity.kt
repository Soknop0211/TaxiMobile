package com.eazy.daiku.ui.customer.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.databinding.ActivityCustomerMapPreViewBinding
import com.eazy.daiku.ui.customer.model.*
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import com.eazy.daiku.ui.map.MapPreviewActivity
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.bottom_sheet.ChangeStyleGoogleMapBottomSheetFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class CustomerMapPreViewActivity : BaseActivity(), OnMapReadyCallback {
    private val viewModel: ProcessCustomerBookingViewModel by viewModels {
        factory
    }
    private lateinit var binding: ActivityCustomerMapPreViewBinding
    private var mMap: GoogleMap? = null
    private var qrCodeRespond: QrCodeRespond? = null
    private var currentGpsLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var needReloadData: Boolean = false
    private var marker: Marker? = null
    private var kioskMarker: Marker? = null
    private var currentLatLngKiosk: LatLng? = null
    private var isShowTraffic: Boolean = false
    private var isRotateGesture: Boolean = false
    private var isNightTheme: Boolean = false
    private var listKioskModel: ListKioskModel? = null
    private var termModel: Terms? = null
    private lateinit var address: String
    private var listMarkerOption: ArrayList<MarkerOption>? = null
    private var latLngMarkerClick: LatLng? = null
    private var mapScreenShotPath: String? = null
    private var addressIndex: Address? = null
    private val intentFilter = IntentFilter()
    private var titleFromSearch: String = ""
    private var descriptionFromSearch: String = ""


    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    companion object {
        const val listKioskModelKey = "listKioskModelKey"
        const val termModelKey = "termModelKey"
    }


    override fun onResume() {
        super.onResume()
        if (needReloadData) {
            mMap?.let {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        needReloadData = false
        unregisterReceiver(myBroadcastReceiver)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(myBroadcastReceiver, intentFilter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerMapPreViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initObserver()
        doAction()
        initGoogleMap()
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.map),
            true
        )
        listMarkerOption = ArrayList<MarkerOption>()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(self())
        if (intent != null && intent.hasExtra(listKioskModelKey)) {
            val listKioskDataJson = intent.getStringExtra(listKioskModelKey)
            listKioskModel =
                GsonConverterHelper.getJsonObjectToGenericClass<ListKioskModel>(listKioskDataJson)
        }
    }

    private fun initObserver() {
        viewModel.loadingCheckoutTaxiLiveData.observe(self()) { haseLoading ->
            binding.loadingView.root.visibility = if (haseLoading) View.VISIBLE else View.GONE
        }
        viewModel.dataCheckoutTaxiLiveData.observe(self()) { checkoutTaxiRespond ->
            if (checkoutTaxiRespond != null && checkoutTaxiRespond.success) {
                AppLOGG.d("DATACHECKOUT", "-->" + Gson().toJson(checkoutTaxiRespond.data))
            } else {
                globalShowError(checkoutTaxiRespond.message)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            mMap?.uiSettings?.isMyLocationButtonEnabled = true
        }
        fetchCurrentLocation()
        mMap?.setOnMapClickListener { latLng ->
            descriptionFromSearch = ""
            titleFromSearch = ""
            latLngMarkerClick = latLng
            drawMarker(latLng)
        }
    }


    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun doAction() {
        binding.actionGps.setOnClickListener {
            moveToCurrentGPS()
        }

        binding.actionHotel.setOnClickListener {
            latLngMarkerClick?.let { moveCameraToCurrentLocation(it) }
        }
        binding.actionMore.setOnClickListener {

        }
        binding.actionConfirmBooking.setOnClickListener {
            if (latLngMarkerClick.toString() == currentLatLngKiosk.toString()) {
                MessageUtils.showError(self(), "", getString(R.string.please_select_location))
            } else {
                if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                    if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                        mapScreenShot()
                    } else {
                        PermissionHelper.requestMultiPermission(
                            self(),
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) { hasPermission ->
                            if (hasPermission) {
                                mapScreenShot()
                            } else {
                                MessageUtils.showError(
                                    self(),
                                    null,
                                    "Permission Deny!"
                                )
                            }
                        }
                    }
                } else {
                    globalRequestDeviceGps()
                }
            }
        }

        binding.actionSearchMap.setOnClickListener {

            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                    RedirectClass.gotoSearchMapActivity(self(),
                        object : BetterActivityResult.OnActivityResult<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == RESULT_OK) {
                                    val intent = result.data
                                    if (intent != null && intent.hasExtra(SearchMapActivity.geometyModelKey)) {
                                        val geometyString =
                                            intent.getStringExtra(SearchMapActivity.geometyModelKey)
                                        val geomety: Features =
                                            GsonConverterHelper.getJsonObjectToGenericClass(
                                                geometyString)
                                        val listLatLng = geomety.geometry?.coordinates
                                        titleFromSearch = geomety.text.toString()
                                        descriptionFromSearch =
                                            geomety.placeName.toString()
                                        listLatLng?.let {
                                            if (it.size >= 2) {
                                                val latLng = LatLng(it[1], it[0])
                                                latLngMarkerClick = latLng
                                                moveCameraToCurrentLocation(latLng)
                                            }
                                        }

                                    }
                                }
                            }

                        })
                } else {
                    PermissionHelper.requestMultiPermission(
                        self(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { hasPermission ->
                        if (hasPermission) {
                            RedirectClass.gotoSearchMapActivity(self(),
                                object : BetterActivityResult.OnActivityResult<ActivityResult> {
                                    override fun onActivityResult(result: ActivityResult) {
                                        if (result.resultCode == RESULT_OK) {
                                            val intent = result.data
                                            if (intent != null && intent.hasExtra(SearchMapActivity.geometyModelKey)) {
                                                val geometyString =
                                                    intent.getStringExtra(SearchMapActivity.geometyModelKey)
                                                val geomety: Features =
                                                    GsonConverterHelper.getJsonObjectToGenericClass(
                                                        geometyString)
                                                val listLatLng = geomety.geometry?.coordinates
                                                titleFromSearch = geomety.text.toString()
                                                descriptionFromSearch =
                                                    geomety.placeName.toString()
                                                listLatLng?.let {
                                                    if (it.size >= 2) {
                                                        val latLng = LatLng(it[1], it[0])
                                                        latLngMarkerClick = latLng
                                                        moveCameraToCurrentLocation(latLng)
                                                    }
                                                }

                                            }
                                        }
                                    }

                                })
                        } else {
                            MessageUtils.showError(
                                self(),
                                null,
                                "Permission Deny!"
                            )
                        }
                    }
                }
            } else {
                globalRequestDeviceGps()
            }
        }

        binding.actionSetting.setOnClickListener {
            openMapSettings()
        }

        binding.actionKiosk.setOnClickListener {
            listKioskModel?.let {
                it.mapLat?.let { it1 -> it.mapLng?.let { it2 -> LatLng(it1, it2) } }
                    ?.let { it2 -> moveCameraToKioskLocation(it2) }
            }
        }

        binding.actionBack.setOnClickListener {
            finish()
        }
    }

    private fun mapScreenShot() {
        marker?.let setOnClickListener@{
            try {
                val geocoder = Geocoder(self(), Locale.getDefault())
                val addresses: List<Address> =
                    geocoder.getFromLocation(it.position.latitude, it.position.longitude, 1)

                if (addresses.isEmpty() || addresses.first().getAddressLine(0).isEmpty()) {
                    Toast.makeText(self(),
                        getString(R.string.invalid_location_please_try_another),
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                mMap?.snapshot { bitmap ->
                    val addressLine = addresses[0].getAddressLine(0)
                    var content = ""
                    addresses[0].let { addr ->
                        val adminArea = ("Admin Area: " + addr.adminArea)
                        val lat = ("Latitude: " + addr.latitude.toString())
                        val lng = ("Longitude: " + addr.longitude.toString())
                        content =
                            "Address Line: $addressLine<br/> $adminArea<br/>$lat<br/>$lng"
                    }
                    val file = File(cacheDir, "map.png")
                    File(file.absolutePath).writeBitmap(bitmap,
                        Bitmap.CompressFormat.PNG,
                        100)
                    mapScreenShotPath = file.absolutePath
                    addressIndex?.let { it1 ->
                        RedirectClass.gotoListTaxiDriverActivity(
                            self(),
                            GsonConverterHelper.convertGenericClassToJson(listKioskModel),
                            latLngMarkerClick?.latitude.toString(),
                            latLngMarkerClick?.longitude.toString(),
                            address,
                            mapScreenShotPath.toString(),
                            it1,
                            descriptionFromSearch,
                            titleFromSearch
                        )
                    }
                }
            } catch (t: Throwable) {
                Toast.makeText(self(),
                    getString(R.string.some_thing_went_wrong_plz_try_again),
                    Toast.LENGTH_LONG).show()
            }
            return@setOnClickListener
        }
    }

    fun submitCheckoutTaxi() {
        val body = HashMap<String, Any>()
        val bodyMap = HashMap<String, Any>()
        body["car_id"] = termModel?.id.toString()
        body["device_id"] = listKioskModel?.deviceId.toString()
        body["select_map"] = 1
        bodyMap["title"] = address
        bodyMap["latitude"] = latLngMarkerClick?.latitude.toString()
        bodyMap["longitude"] = latLngMarkerClick?.longitude.toString()
        body["map"] = bodyMap
        viewModel.submitCheckoutTaxi(body)
    }

    private fun openMapSettings() {
        ChangeStyleGoogleMapBottomSheetFragment.newInstance(
            isShowTraffic,
            isRotateGesture,
            isNightTheme,
            {
                mMap?.let { googleMap ->
                    isShowTraffic = it
                    googleMap.isTrafficEnabled = isShowTraffic
                }
            }, {
                mMap?.let { googleMap ->
                    isRotateGesture = it
                    googleMap.uiSettings.isRotateGesturesEnabled = isRotateGesture
                }
            }, {
                mMap?.let { googleMap ->
                    isNightTheme = it
                    if (isNightTheme) {
                        googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                self(),
                                R.raw.night_mode_google_map
                            )
                        )
                    } else {
                        googleMap.setMapStyle(null)
                    }

                }
            }
        ).show(supportFragmentManager, "ChangeStyleGoogleMapBottomSheetFragment")
    }

    private fun initGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun justPreviewOnly(): Boolean {
        return intent.hasExtra(MapPreviewActivity.PreviewMapOnly) && intent.getBooleanExtra(
            MapPreviewActivity.PreviewMapOnly,
            true
        )
    }

    private fun loadGpsLocationUser() {
        mMap?.let {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // getting the last known or current location
                        currentGpsLocation = location
                        if (!justPreviewOnly()) {

                            val gpsLatLng = LatLng(
                                location.latitude,
                                location.longitude
                            )
                            val destinationLatLng = LatLng(
                                qrCodeRespond?.latitude ?: 0.0,
                                qrCodeRespond?.longitude ?: 0.0
                            )
                            val group = LatLngBounds.Builder()
                                .include(gpsLatLng) // LatLgn object1
                                .include(destinationLatLng) // LatLgn object2
                                .build()

                            mMap?.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    group, 250
                                )
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this, "Failed on getting current location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun fetchCurrentLocation() {
        @SuppressLint("MissingPermission")
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location: Location ->
            val latLng = LatLng(location.latitude, location.longitude)
            listMarkerOption?.add(MarkerOption(latLng, Config.TypeMarkerOptions.userMarkerType))
            listKioskModel?.let {
                it.mapLat?.let { it1 -> it.mapLng?.let { it2 -> LatLng(it1, it2) } }
                    ?.let { it2 ->
                        latLngMarkerClick = it2
                        currentLatLngKiosk = it2
                        draMarkerStationKiosk(it2)
                        drawMarker(it2)
                    }
            }
            listKioskModel?.let {
                listMarkerOption?.add(MarkerOption(it.mapLat?.let { it1 ->
                    it.mapLng?.let { it2 ->
                        LatLng(it1,
                            it2)
                    }
                }, Config.TypeMarkerOptions.kioskMarkerType))
            }
            if (listMarkerOption != null) {
                showAllMarker()
            }
        }
    }

    private fun drawMarker(latLng: LatLng) {
        addressIndex =
            AddressHelper.getStringAddressByIndex(self(), latLng.latitude, latLng.longitude)
        address = AddressHelper.getStringAddress(self(), latLng.latitude, latLng.longitude)
        marker?.remove()
        marker = null
        val fullAddress =
            if (!TextUtils.isEmpty(descriptionFromSearch)) descriptionFromSearch else address
        marker = mMap?.addMarker(MarkerOptions().position(latLng)
            .title(fullAddress))
        marker?.showInfoWindow()
    }

    private fun draMarkerStationKiosk(latLng: LatLng) {
        val address = AddressHelper.getStringAddress(self(), latLng.latitude, latLng.longitude)
        kioskMarker =
            mMap?.addMarker(MarkerOptions().position(latLng).title(listKioskModel?.location)
                .icon(MapHelper.bitmapDescriptorFromVector(R.drawable.ic_kiosk, self())))
        kioskMarker?.showInfoWindow()
    }

    private fun moveCameraToCurrentLocation(latLng: LatLng) {
        val update = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap?.moveCamera(update)
        mMap?.animateCamera(zoom)
        drawMarker(latLng)
    }

    private fun moveCameraToKioskLocation(latLng: LatLng) {
        val update = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap?.moveCamera(update)
        mMap?.animateCamera(zoom)
        draMarkerStationKiosk(latLng)
    }

    private fun moveToCurrentGPS() {
        if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
            if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                mMap?.let {
                    currentGpsLocation?.let {
                        val currentGps = LatLng(
                            currentGpsLocation?.latitude ?: 0.0,
                            currentGpsLocation?.longitude ?: 0.0
                        )
                        val update = CameraUpdateFactory.newLatLng(currentGps)
                        val zoom = CameraUpdateFactory.zoomTo(15f)
                        mMap?.moveCamera(update)
                        mMap?.animateCamera(zoom)
                    }
                }
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        mMap?.let {
                            currentGpsLocation?.let {
                                val currentGps = LatLng(
                                    currentGpsLocation?.latitude ?: 0.0,
                                    currentGpsLocation?.longitude ?: 0.0
                                )

                                val update = CameraUpdateFactory.newLatLng(currentGps)
                                val zoom = CameraUpdateFactory.zoomTo(15f)
                                mMap?.moveCamera(update)
                                mMap?.animateCamera(zoom)
                            }
                        }
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Gps Deny!"
                        )
                    }
                }
            }
        } else {
            globalRequestDeviceGps()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }


    private fun showAllMarker() {
        val builder = LatLngBounds.Builder()
        listMarkerOption?.let {
            for (markerOption in it) {
                builder.include(markerOption.latLng)
            }
        }
        val bounds: LatLngBounds = builder.build()
        val with = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val panding = (with * 0.33).toInt()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, with, height, panding)
        mMap?.animateCamera(cu)

    }

    private var myBroadcastReceiver = object : MyBroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            super.onReceive(p0, p1)
            if (p1 != null) {
                when {
                    p1.hasExtra(finishWhenPaymentCompletedKey) -> {
                        startBroadcastData(finishWhenPaymentCompletedKey)
                        finish()
                    }
                }
            }
        }
    }
}