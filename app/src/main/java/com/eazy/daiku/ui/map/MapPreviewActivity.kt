package com.eazy.daiku.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.CustomerHistoryModel
import com.eazy.daiku.data.model.server_model.History
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.databinding.ActivityMapPreviewBinding
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.bottom_sheet.ChangeStyleGoogleMapBottomSheetFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.eazy.daiku.utility.view_model.QrCodeVm
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*


const val TAG = "EazyTaxiApplication"

class MapPreviewActivity :
    BaseActivity(),
    OnMapReadyCallback {


    private lateinit var binding: ActivityMapPreviewBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private var qrCodeRespond: QrCodeRespond? = null
    private var currentGpsLocation: Location? = null
    private val googleMapApplication by lazy { "com.google.android.apps.maps" }
    private val qrCodeVm: QrCodeVm by viewModels {
        factory
    }
    private var needReloadData: Boolean = false
    private var isShowTraffic: Boolean = false
    private var isRotateGesture: Boolean = false
    private var isNightTheme: Boolean = false


    companion object {
        const val QrCodeFieldKey = "QrCodeFieldKey"
        const val PreviewMapOnly = "PreviewMapOnly"
    }

    override fun onResume() {
        super.onResume()

        if (needReloadData) {
            mMap?.let {
                fetchGpsLocation()
            }
            updateUIGoogleMap()

        }
    }

    override fun onStop() {
        super.onStop()

        unRegisterForegroundBroadcastService()
        removeTrackLocation()

    }

    override fun onDestroy() {
        super.onDestroy()
        needReloadData = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        intiView()
        initObserved()
        initQrCodeRespond()
        doAction()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let {
            it.uiSettings.isMyLocationButtonEnabled = false
            it.uiSettings.isRotateGesturesEnabled = false
            it.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        fetchGpsLocation()
        val polylineOptions = PolylineOptions()
        polylineOptions.addAll(decodePoly("_rseAunb_Sg@eHwIf@gKr@"))
        mMap?.addPolyline(polylineOptions.width(10f).color(getColor(R.color.colorPrimary))
            .geodesic(false))
    }


    private fun decodePoly(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    private fun initObserved() {
        qrCodeVm.loadingScanQrMutableLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }


        qrCodeVm.qrCodeDataMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                needReloadData = true
            } else {
                globalShowError(respondState.message)
            }
        }

    }

    private fun initQrCodeRespond() {
        if (intent.hasExtra(QrCodeFieldKey)) {
            val gsonStr = intent.getStringExtra(QrCodeFieldKey)
            gsonStr?.let {
                if (justPreviewOnly()) {
                    var history: History? = null
                    var customerHistory: CustomerHistoryModel? = null
                    if (BuildConfig.IS_CUSTOMER) {
                        customerHistory =
                            GsonConverterHelper.getJsonObjectToGenericClass<CustomerHistoryModel>(
                                gsonStr
                            )
                    } else {
                        history = GsonConverterHelper.getJsonObjectToGenericClass<History>(
                            gsonStr
                        )
                    }
                    qrCodeRespond = QrCodeRespond()
                    if (BuildConfig.IS_CUSTOMER) {
                        var distance: String? = null
                        distance = customerHistory?.meta?.destination?.distance?.let { it1 ->
                            AmountHelper.amountFormat(
                                "",
                                it1
                            )
                        }
                        qrCodeRespond?.latitude = customerHistory?.meta?.destination?.mapLat ?: 0.00
                        qrCodeRespond?.longitude =
                            customerHistory?.meta?.destination?.mapLng ?: 0.00
                        qrCodeRespond?.distance =
                            distance?.toDouble() ?: 0.00
                        qrCodeRespond?.title =
                            if (customerHistory?.meta?.destination?.title != null) String.format("%s",
                                customerHistory.meta?.destination?.title)
                            else if (customerHistory?.meta?.destination?.toTitle != null) String.format(
                                "%s",
                                customerHistory.meta?.destination?.toTitle)
                            else if (customerHistory?.meta?.destination?.fromTitle != null) String.format(
                                "%s",
                                customerHistory.meta?.destination?.fromTitle)
                            else "---"
                        binding.containerShare.visibility =
                            if (customerHistory?.status != null && customerHistory.status == "Cancelled" && customerHistory.status == "Cus-Cancelled") View.VISIBLE
                            else View.GONE
                        qrCodeRespond?.total_amount = customerHistory?.total ?: 0.00



                    } else {
                        qrCodeRespond?.latitude = history?.destination?.mapLat ?: 0.00
                        qrCodeRespond?.longitude = history?.destination?.mapLng ?: 0.00
                        qrCodeRespond?.distance = history?.distance ?: 0.00
                        qrCodeRespond?.title =
                            if (history?.destination?.title != null) String.format("%s",
                                history.destination?.title)
                            else if (history?.destination?.toTitle != null) String.format("%s",
                                history.destination?.toTitle)
                            else if (history?.destination?.fromTitle != null) String.format("%s",
                                history.destination?.fromTitle)
                            else "---"
                        qrCodeRespond?.total_amount = history?.total ?: 0.00
                    }


                    binding.actionRouteAgainImg.visibility = View.GONE
                    binding.actionRouteAgainImg.visibility = View.GONE
                    binding.previewContainer.visibility = View.VISIBLE

                    binding.titlePreviewTv.text = qrCodeRespond?.title ?: "Unknown"
                    binding.priceKiloPreviewTv.text = String.format(
                        "%sKm", qrCodeRespond?.distance ?: "-"
                    )
                    binding.pricePreviewTv.text = String.format(
                        Locale.US,
                        "$%s", qrCodeRespond?.total_amount ?: "-"
                    )
                    binding.pricePreviewTv.setTextColor(
                        ContextCompat.getColor(
                            self(),
                            R.color.black
                        )
                    )
                    initGoogleMap()

                } else {
                    qrCodeRespond = GsonConverterHelper.getJsonObjectToGenericClass<QrCodeRespond>(
                        gsonStr
                    )
                    //set in ram
                    UserDefaultSingleTon.newInstance?.qrCodeRespond = qrCodeRespond
                    if (UserDefaultSingleTon.newInstance?.startStopState.toString() != TripEnum.Processing.toString()
                    ) {
                        UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Start
                    }
                    initUI()
                    updateUIGoogleMap()
                    initGoogleMap()

                    if (BuildConfig.IS_CUSTOMER) {
                        binding.containerInformationDriver.visibility =
                            if (qrCodeRespond != null && qrCodeRespond?.driverInfo != null) View.VISIBLE else View.GONE
                        binding.tvNameDriver.text =
                            String.format("%s", qrCodeRespond?.driverInfo?.name ?: "--")
                        binding.tvPhoneDriver.text =
                            String.format("%s", qrCodeRespond?.driverInfo?.phone ?: "--")
                        Glide.with(this).load(qrCodeRespond?.driverInfo?.avatarId ?: "")
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).error(R.drawable.profile_128px_icon)
                            .into(binding.imProfileDriver)
                    }
                }
            }
        } else {
            binding.actionRouteAgainImg.visibility = View.GONE
            binding.actionRouteAgainImg.visibility = View.GONE
            initUI()
            initGoogleMap()
        }
    }

    private fun intiView() {
        if (intent!=null&&intent.hasExtra("showQrcode")){
            binding.appBar.qrCode.visibility = View.VISIBLE
        }
        foregroundOnlyBroadcastReceiver =
            UserDefaultSingleTon.newInstance?.getForegroundOnlyBroadcastReceiverSingleTon()

        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.map),
            true
        )
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(self())

        if (BuildConfig.IS_WEGO_TAXI) {
            val user = getUserUserToSharePreference()
            val isWeGoEmployee = user?.isEmployee == Config.WeGoBusiness.Employee
            binding.pricePreviewTv.visibility = if (isWeGoEmployee) View.GONE else View.VISIBLE
        }

        binding.containerEmergency.visibility =
            if (BuildConfig.IS_CUSTOMER) View.VISIBLE else View.GONE


        binding.containerShare.visibility = if (BuildConfig.IS_CUSTOMER) View.VISIBLE else View.GONE
    }

    private fun doAction() {

        binding.actionStartAndStopTripFrameLayout.setOnClickListener {
            redirectToGoogleMap()
        }



        binding.actionGps.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                    mMap?.let {
                        currentGpsLocation?.let {
                            val currentGps = LatLng(
                                currentGpsLocation?.latitude ?: 0.0,
                                currentGpsLocation?.longitude ?: 0.0
                            )
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentGps, 16.0f))
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
                                    mMap!!.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            currentGps,
                                            16.0f
                                        )
                                    )
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

        binding.actionHotel.setOnClickListener {
            mMap?.let {
                val hotel = LatLng(
                    qrCodeRespond?.latitude ?: 0.0,
                    qrCodeRespond?.longitude ?: 0.0
                )
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(hotel, 16.0f))
            }

        }

        binding.actionRouteAgainImg.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                    currentGpsLocation?.let {
                        //update state
                        UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Processing

                        val sourceLatitude = currentGpsLocation!!.latitude
                        val sourceLongitude = currentGpsLocation!!.longitude
                        val destinationLatitude = qrCodeRespond?.latitude
                        val destinationLongitude = qrCodeRespond?.longitude

                        val uri =
                            "http://maps.google.com/maps?f=d&hl=en&saddr=$sourceLatitude,$sourceLongitude&daddr=$destinationLatitude,$destinationLongitude"
                        AppLOGG.d("googleMapTest", "#${Uri.parse(uri)}")
                        if (EazyTaxiHelper.isPackageInstalled(
                                googleMapApplication,
                                packageManager
                            )
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            intent.setPackage("com.google.android.apps.maps")
                            startActivity(intent)
                        } else {
                            RedirectClass.gotoWebViewGoogleMapActivity(
                                self(),
                                Uri.parse(uri).toString()
                            )
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
                            currentGpsLocation?.let {
                                //update state
                                UserDefaultSingleTon.newInstance?.startStopState =
                                    TripEnum.Processing

                                val sourceLatitude = currentGpsLocation!!.latitude
                                val sourceLongitude = currentGpsLocation!!.longitude
                                val destinationLatitude = qrCodeRespond?.latitude
                                val destinationLongitude = qrCodeRespond?.longitude

                                val uri =
                                    "http://maps.google.com/maps?f=d&hl=en&saddr=$sourceLatitude,$sourceLongitude&daddr=$destinationLatitude,$destinationLongitude"

                                if (EazyTaxiHelper.isPackageInstalled(
                                        googleMapApplication,
                                        packageManager
                                    )
                                ) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                    intent.setPackage("com.google.android.apps.maps")
                                    startActivity(intent)
                                } else {
                                    RedirectClass.gotoWebViewGoogleMapActivity(
                                        self(),
                                        uri
                                    )
                                }
                            }
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

        binding.actionMore.setOnClickListener {
            openMapSettings()
        }

        binding.actionEmergencyFrameLayout.setOnClickListener {
            binding.loadingView.root.visibility = View.VISIBLE
            ParseLiveLocationHelper
                .newInstance?.let { parseServer ->
                    val user = getUserUserToSharePreference()
                    val parseQuery: ParseQuery<ParseObject> =
                        parseServer.checkLiveMapUserParseServer(user?.id ?: -1)
                    parseQuery.findInBackground { objects, _ ->
                        //date time
                        val currentDateTime =
                            DateTimeHelper.dateFm(
                                Calendar.getInstance().time,
                                "E, dd MMM yyyy, hh:mm:ss a"
                            )
                        if (objects != null && objects.size > 0) {
                            //update
                            val parseObject = objects.first()
                            parseObject.put("emergency_datetime", currentDateTime)
                            parseObject.saveInBackground { e ->
                                binding.loadingView.root.visibility = View.GONE
                                if (e == null) {
                                    MessageUtils.showWarnigIcon(
                                        self(),
                                        R.drawable.emergency_icon,
                                        getString(R.string.emergency_sos),
                                        getString(R.string.stay_tuned_our_call_center_will_contact_you_soon),
                                        showCancelButton = false,
                                        canceledOnTouchOutside = true
                                    ) { its ->
                                        its.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
        }

        binding.containerEmergency.setOnClickListener {
            binding.actionEmergencyFrameLayout.performClick()
        }

        binding.actionShare.setOnClickListener {
            val uri =
                "https://www.google.com/maps/?q=" + qrCodeRespond?.latitude + "," + qrCodeRespond?.longitude
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, uri)
            startActivity(Intent.createChooser(intent, getString(R.string.share_location)))
        }
        binding.containerCall.setOnClickListener {
            EazyTaxiHelper.makeCall(self(), qrCodeRespond?.driverInfo?.phone ?: "")
        }

        binding.containerMessage.setOnClickListener {
            val uri = Uri.parse("smsto:" + qrCodeRespond?.driverInfo?.phone)
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "")
            startActivity(intent)
        }


        binding.appBar.qrCode.setOnClickListener {
            qrCodeRespond?.let { it1 ->
                MessageUtils.showQrCodeDestination(
                    self(),
                    it1.urlQrcode.toString(),
                    it1.vehicle.toString()

                )
            }
        }


    }

    private fun redirectToGoogleMap() {
        if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
            if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                startLoadNavigationMap()
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        startLoadNavigationMap()
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

    private fun startLoadNavigationMap() {
        when (UserDefaultSingleTon.newInstance?.startStopState) {
            TripEnum.Start -> {
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
                            currentGpsLocation?.let {
                                saveCurrentGpsUser(it.latitude, it.longitude)
                                qrCodeVm.requestTripToServer(
                                    TripEnum.Start,
                                    qrCodeRespond?.code ?: "",
                                ) {
                                    //update state
                                    UserDefaultSingleTon.newInstance?.startStopState =
                                        TripEnum.Processing
                                    startBroadcastData(MyBroadcastReceiver.hasProcessingTripKey)
                                    UserDefaultSingleTon.newInstance?.qrCodeRespond = qrCodeRespond
                                    //update to parse
                                    onRegisterBindServiceForegroundConnection()
                                    onRegisterForegroundService()

                                    Handler(Looper.getMainLooper()).postDelayed(
                                        {
                                            startUpdateLocationToParseServer()
                                        }, 500
                                    )

                                    val sourceLatitude = currentGpsLocation!!.latitude
                                    val sourceLongitude = currentGpsLocation!!.longitude
                                    val destinationLatitude = qrCodeRespond?.latitude
                                    val destinationLongitude = qrCodeRespond?.longitude

                                    val uri =
                                        "http://maps.google.com/maps?f=d&hl=en&saddr=$sourceLatitude,$sourceLongitude&daddr=$destinationLatitude,$destinationLongitude"
                                    Handler(Looper.getMainLooper()).postDelayed(
                                        {
                                            if (EazyTaxiHelper.isPackageInstalled(
                                                    googleMapApplication,
                                                    packageManager
                                                )
                                            ) {
                                                val intent =
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                                intent.setPackage("com.google.android.apps.maps")
                                                startActivity(intent)
                                            } else {
                                                RedirectClass.gotoWebViewGoogleMapActivity(
                                                    self(),
                                                    uri
                                                )
                                            }
                                        },
                                        1000 // value in milliseconds
                                    )
                                }
                            }
                                ?: globalShowError("current gps is null.please enable your permission gps.")
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this, "current gps is null.please enable your permission gps.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            TripEnum.Processing -> {
                MessageUtils.showConfirm(
                    self(),
                    "",
                    getString(R.string.are_you_want_to_finish_trip),
                ) { it ->
                    it.dismiss()
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
                        return@showConfirm
                    }
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location ->
                            location?.let {
                                saveCurrentGpsUser(it.latitude, it.longitude)
                                qrCodeVm.requestTripToServer(
                                    TripEnum.End,
                                    qrCodeRespond?.code ?: "",
                                ) {
                                    startBroadcastData(MyBroadcastReceiver.reloadPaymentSuccessKey)
                                    MessageUtils.showSuccessDismiss(
                                        self(),
                                        "",
                                        getString(R.string.payment_completed)
                                    ) { its ->
                                        its.dismiss()
                                        clearTripSession()
                                        finish()
                                    }
                                }
                            }
                                ?: globalShowError("current gps is null.please enable your permission gps.")
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "current gps is null.please enable your permission gps.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
            }
            else -> {

            }
        }
    }

    private fun initGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchGpsLocation()
    }

    private fun initUI() {
        binding.titleTv.text = qrCodeRespond?.title ?: "Unknown"

        if (BuildConfig.IS_WEGO_TAXI) {
            val user = getUserUserToSharePreference()
            val isWeGoEmployee = user?.isEmployee == Config.WeGoBusiness.Employee
            if (isWeGoEmployee) {
                binding.priceKiloTv.text = String.format(
                    Locale.US,
                    "%sKm", qrCodeRespond?.distance ?: "-"
                )
            } else {
                binding.priceKiloTv.text = String.format(
                    Locale.US,
                    "%sKm • $%s", qrCodeRespond?.distance ?: "-", qrCodeRespond?.total_amount ?: "-"
                )
            }
        } else {
            binding.priceKiloTv.text = String.format(
                Locale.US,
                "%sKm • $%s", qrCodeRespond?.distance ?: "-", qrCodeRespond?.total_amount ?: "-"
            )
        }
    }

    private fun updateUIGoogleMap() {
        when (UserDefaultSingleTon.newInstance?.startStopState) {
            TripEnum.Start, TripEnum.Nothing -> {
                if (BuildConfig.IS_WEGO_TAXI) {
                    binding.actionStartAndStopTripFrameLayout.setBackgroundResource(
                        R.drawable.round_circle_color_primary_drawable
                    )
                    binding.nameBtnTv.setTextColor(ContextCompat.getColor(self(), R.color.white))
                } else {
                    binding.actionStartAndStopTripFrameLayout.setBackgroundResource(
                        R.drawable.circle_black_drawable
                    )
                    binding.nameBtnTv.setTextColor(
                        ContextCompat.getColor(
                            self(),
                            R.color.colorPrimary
                        )
                    )
                }
                binding.nameBtnTv.text = getString(R.string.start)
                binding.actionRouteAgainImg.visibility = View.GONE
                binding.actionStartAndStopTripFrameLayout.visibility =
                    if (BuildConfig.IS_CUSTOMER) View.GONE else View.VISIBLE
                //open emergency button when has processing trip
                binding.actionEmergencyFrameLayout.visibility = View.GONE
            }
            TripEnum.Processing -> {
                binding.actionStartAndStopTripFrameLayout.setBackgroundResource(
                    R.drawable.round_circle_color_red_drawable
                )
                binding.nameBtnTv.text = getString(R.string.finish)
                binding.nameBtnTv.setTextColor(ContextCompat.getColor(self(), R.color.white))
                binding.actionStartAndStopTripFrameLayout.visibility = View.VISIBLE
                binding.actionRouteAgainImg.visibility = View.VISIBLE
                //open emergency button when has processing trip
                binding.actionEmergencyFrameLayout.visibility = View.VISIBLE
            }
            else -> {
                binding.actionRouteAgainImg.visibility = View.GONE
                binding.actionStartAndStopTripFrameLayout.visibility = View.GONE
                //open emergency button when has processing trip
                binding.actionEmergencyFrameLayout.visibility = View.GONE
            }
        }
    }

    private fun fetchGpsLocation() {
        mMap?.let { mMap ->
            // Add a marker in hotel and move the camera
            val hotel = LatLng(
                qrCodeRespond?.latitude ?: 0.0,
                qrCodeRespond?.longitude ?: 0.0
            )
            mMap.addMarker(MarkerOptions().position(hotel).title(qrCodeRespond?.title ?: "-"))
                .showInfoWindow()

            val hasGps = PermissionHelper.hasCOARSEAndFINELocationPermission(self())
            if (hasGps) {
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
                mMap.isMyLocationEnabled = true
                loadGpsLocationUser()
                if (justPreviewOnly()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotel, 16.0f))
                }
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotel, 16.0f))
            }
        }
    }

    private fun justPreviewOnly(): Boolean {
        return intent.hasExtra(PreviewMapOnly) && intent.getBooleanExtra(
            PreviewMapOnly,
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

                            mMap!!.animateCamera(
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

}