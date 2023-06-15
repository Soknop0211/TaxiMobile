package com.eazy.daiku.ui.customer.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.BookingPreviewDoCheckoutModel
import com.eazy.daiku.data.model.server_model.Drivers
import com.eazy.daiku.data.model.server_model.Vehicle
import com.eazy.daiku.data.remote.EazyTaxiApi
import com.eazy.daiku.databinding.ActivityPickUpLocationBinding
import com.eazy.daiku.ui.customer.model.*
import com.eazy.daiku.ui.customer.model.sear_map.Results
import com.eazy.daiku.ui.customer.step_booking.SelectCarBookingFragment
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.bottom_sheet.ChangeStyleGoogleMapBottomSheetFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import okhttp3.OkHttpClient
import okhttp3.Route
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class PickUpLocationActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityPickUpLocationBinding
    private val viewModel: ProcessCustomerBookingViewModel by viewModels {
        factory
    }
    private var mMap: GoogleMap? = null
    private var needReloadData: Boolean = false
    private var currentLatLng: LatLng? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongtitude: Double? = null
    private var taxiLatitude: Double? = null
    private var taxiLongtitude: Double? = null
    private var marker: Marker? = null
    private var locationKioskNearBy: ListKioskModel? = null
    private var listMarkerOption: ArrayList<MarkerOption>? = null
    private val intentFilter = IntentFilter()
    private lateinit var selectCarBookingAdapter: SelectCarBookingFragment.SelectCarBookingAdapter
    private var addressIndex: Address? = null
    private var mapScreenShotPath: String? = null
    private var address: String? = null
    private var latLngMarkerClick: LatLng? = null
    private var carBookingModel: Vehicle? = null
    private var titleFromSearch: String = ""
    private var descriptionFromSearch: String = ""
    private var previewCheckoutModel: PreviewCheckoutModel? = null
    private var bookingPreviewDoCheckoutModel: BookingPreviewDoCheckoutModel =
        BookingPreviewDoCheckoutModel()
    private var markerA: Marker? = null
    private var markerB: Marker? = null
    private var hasPointOnMapA: Boolean = false
    private var hasPointOnMapB: Boolean = false
    private var latLngA: LatLng = LatLng(0.0, 0.0)
    private var latLngB: LatLng = LatLng(0.0, 0.0)
    private var currentLatLngA: LatLng = LatLng(0.0, 0.0)
    private var currentLatLngB: LatLng = LatLng(0.0, 0.0)
    private var hasAddressA: Boolean = false
    private var hasAddressB: Boolean = false
    private var addressPointA: String? = null
    private var addressPointB: String? = null
    private var startX = 0f
    private var startY = 0f
    private val CLICK_ACTION_THRESHOLD = 200
    private val markerHasMap = HashMap<String, Marker>()
    private var fromTitle: String = ""
    private var toTitle: String = ""
    private var outTradeNo: String? = null
    private var isCanNotBack = false
    private lateinit var timer: CountDownTimer

    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    private var isShowTraffic: Boolean = false
    private var isRotateGesture: Boolean = false
    private var isNightTheme: Boolean = false

    companion object {
        const val searchLocationKioskNearByKey = "searchLocationKioskNearByKey"
    }

    override fun onResume() {
        super.onResume()
        if (needReloadData) {
            mMap?.let {


                if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                    if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                        fetchCurrentLocation(false)
                    } else {
                        PermissionHelper.requestMultiPermission(
                            self(),
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) { _ ->
                            fetchCurrentLocation(false)
                        }
                    }
                } else {
                    globalRequestDeviceGps()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
            if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                fetchCurrentLocation(false)
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) { _ ->
                    fetchCurrentLocation(false)
                }
            }
        } else {
            globalRequestDeviceGps()
        }
        needReloadData = false
        unregisterReceiver(myBroadcastReceiver)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(myBroadcastReceiver, intentFilter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickUpLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initObserver()
        initAction()
        initGoogleMap()
        initRecyclerview()
    }


    private fun initView() {
        selectCarBookingAdapter = SelectCarBookingFragment.SelectCarBookingAdapter()

        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            "Taxi Station",
            true
        )
        listMarkerOption = ArrayList()
        if (intent != null && intent.hasExtra(searchLocationKioskNearByKey)) {
            val dataJson = intent.getStringExtra(searchLocationKioskNearByKey)
            dataJson?.let {
                locationKioskNearBy =
                    GsonConverterHelper.getJsonObjectToGenericClass<ListKioskModel>(
                        dataJson
                    )
            }
        }
        if (locationKioskNearBy != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.containerSearchLocation.visibility = View.GONE
            }, 3000)
            binding.tvSearching.text = getString(R.string.searching_taxi_station)
            binding.actionParkingTaxi.visibility = View.GONE
            binding.containerSearchNotFound.visibility = View.GONE
        } else {
            binding.tvSearching.text = getString(R.string.not_found_parking)
            binding.actionParkingTaxi.visibility = View.VISIBLE
            binding.containerSearchNotFound.visibility = View.VISIBLE
            binding.containerLottie.visibility = View.GONE
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(self())
    }

    private fun initObserver() {
        viewModel.loadingSearchLocationKioskLiveData.observe(self()) { hasLoading ->
            Handler(Looper.getMainLooper()).postDelayed({
                binding.containerSearchLocation.visibility =
                    if (hasLoading) View.VISIBLE else View.GONE
            }, 5000)

        }
        viewModel.dataSearchLocationKioskLiveData.observe(self()) { respond ->
            if (respond != null && respond.success) {
                if (respond.data != null) {
                    locationKioskNearBy = respond.data
                }
            } else {
                globalShowError(respond.message)
            }
        }


        viewModel.loadingGetTaxiDriverLiveData.observe(self()) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        viewModel.dataGetTaxiDriverLiveData.observe(self()) { respondData ->
            if (respondData != null && respondData.success) {
                AppLOGG.d("DATALISTTAXI", "-->" + Gson().toJson(respondData.data))
            } else {
                globalShowError(respondData.message)
            }
        }

        viewModel.loadingListAllLocationKioskLiveData.observe(self()) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.dataListAllLocationKioskLiveData.observe(self()) { dataRespond ->
            if (dataRespond != null && dataRespond.success) {
                val datajson = Gson().toJson(dataRespond.data)
                RedirectClass.gotoLocationKioskBookingTaxi(self(), datajson)
            } else {
                globalShowError(dataRespond.message)
            }
        }

        viewModel.loadingCheckoutTaxiLiveData.observe(self()) {
            // binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
            binding.actionBookNow.isEnabled = !it
        }
        viewModel.dataCheckoutTaxiLiveData.observe(self()) {
            if (it != null && it.success) {
                previewCheckoutModel = it.data
                val distance: Double? =
                    if (it.data != null && !TextUtils.isEmpty(it.data.kilometre.toString()))
                        it.data.kilometre else 0.0
                val distanceFormat = String.format("%sKm",
                    distance?.let { it1 -> AmountHelper.amountFormat("", it1) })
                val totalAmount: Double? =
                    if (it.data != null && !TextUtils.isEmpty(it.data.totalPrice.toString()))
                        it.data.totalPrice else 0.0
                val totalAmountFormat = String.format("%s",
                    totalAmount?.let { it1 -> AmountHelper.amountFormat("$", it1) })

                binding.actionBookNow.text =
                    String.format("%s = %s | %s", distanceFormat, totalAmountFormat, "Book Now")


                val dataJson = Gson()
                val previewCheckout = dataJson.toJson(it.data)


                /* if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                     if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                         mapScreenShot(previewCheckout)
                     } else {
                         PermissionHelper.requestMultiPermission(
                             self(),
                             arrayOf(
                                 Manifest.permission.ACCESS_FINE_LOCATION,
                                 Manifest.permission.ACCESS_COARSE_LOCATION
                             )
                         ) { hasPermission ->
                             if (hasPermission) {
                                 mapScreenShot(previewCheckout)
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
                 }*/
            } else {
                MessageUtils.showError(self(), "", it.message)
            }
        }


        viewModel.loadingCheckoutLiveData.observe(self()) {
            binding.loadingLookingDriver.visibility = if (it) View.VISIBLE else View.GONE
            hasPointOnMapA = it
            hasPointOnMapB = it
            binding.tvAddressNameA.setTextColor(
                if (it) getColor(R.color.light_gray) else getColor(
                    R.color.black
                )
            )
            binding.tvAddressNameB.setTextColor(
                if (it) getColor(R.color.light_gray) else getColor(
                    R.color.black
                )
            )
        }
        viewModel.dataCheckoutLiveData.observe(self()) {
            if (it != null && it.success && it.data != null && it.data.outTradeNo != null) {
                outTradeNo = it.data.outTradeNo
                binding.actionBookNow.visibility = View.GONE
                binding.actionCancelBooking.visibility = View.VISIBLE
                val dataJson = Gson().toJson(it.data)
                binding.loadingLookingDriver.visibility = View.VISIBLE
                hasPointOnMapA = true
                hasPointOnMapB = true
                binding.actionBookNow.isEnabled = false
                binding.tvAddressNameA.setTextColor(getColor(R.color.light_gray))
                binding.tvAddressNameB.setTextColor(getColor(R.color.light_gray))



                ParseLiveLocationHelper
                    .newInstance?.let { parseServer ->
                        val parseQuery: ParseQuery<ParseObject> =
                            parseServer.checkStatusAssignTaxi(it.data.outTradeNo ?: "")


                        parseServer.checkStatusAssignTaxi(it.data.outTradeNo ?: "")
                            .findInBackground { parseObjects, e ->
                                var timerRetry: Long = 150 - 10
                                timer = object : CountDownTimer(150000, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        binding.tvTimer.visibility = View.VISIBLE
                                        val time = String.format(
                                            Locale.getDefault(), "%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                                        )
                                        binding.tvTimer.text = String.format("%s", time)
                                        if (millisUntilFinished / 1000 == timerRetry) {

                                            if (parseObjects != null && parseObjects.size > 0) {
                                                val parseObj = parseObjects.first()
                                                parseObj.put(
                                                    "app_server",
                                                    Config.UserDeviceInfo.appServer
                                                )
                                                parseObj.put("status", "Retry")
                                                parseObj.saveInBackground()
                                            }
                                            timerRetry = (millisUntilFinished / 1000) - 10

                                        }

                                    }

                                    override fun onFinish() {
                                        timer.cancel()
                                        isCanNotBack = false
                                        viewModel.submitCancelBooingTaxi(outTradeNo ?: "")
                                    }

                                }
                                timer.start()
                            }

                        ParseLiveQueryClient.Factory.getClient().subscribe(parseQuery)
                            .handleEvent(SubscriptionHandling.Event.UPDATE) { _: ParseQuery<ParseObject?>?, parseObject: ParseObject? ->
                                if (parseObject != null) {
                                    val status = parseObject.get("status").toString()
                                    if (status == "Accepted") {
                                        startBroadcastData(MyBroadcastReceiver.reloadProcessingBookingKey)
                                        runOnUiThread {
                                            RedirectClass.gotoWebPayActivity(self(),
                                                dataJson,
                                                object :
                                                    BetterActivityResult.OnActivityResult<ActivityResult> {
                                                    override fun onActivityResult(result: ActivityResult) {

                                                    }

                                                })
                                            binding.loadingLookingDriver.visibility =
                                                View.GONE
                                            hasPointOnMapA = false
                                            hasPointOnMapB = false
                                            isCanNotBack = false
                                            binding.actionBookNow.isEnabled = true
                                            binding.tvAddressNameA.setTextColor(getColor(R.color.black))
                                            binding.tvAddressNameB.setTextColor(getColor(R.color.black))
                                            binding.actionBookNow.visibility = View.VISIBLE
                                            binding.actionCancelBooking.visibility =
                                                View.GONE
                                            binding.recyclerView.visibility = View.VISIBLE
                                            timer.cancel()
                                        }
                                    } else if (status == "Rejected") {
                                        runOnUiThread {
                                            parseObject.put(
                                                "app_server",
                                                Config.UserDeviceInfo.appServer
                                            )
                                            parseObject.put("status", "Retry")
                                            parseObject.saveInBackground()
                                        }

                                    }
                                }


                            }
                        /*parseQuery.getFirstInBackground { objects, exception ->
                            if (exception != null) {
                                AppLOGG.d("LIVEUSERDATA",
                                    "ex--> " + Gson().toJson(exception) + "id-->" + driver.driverId)
                            } else if (objects != null) {
                                val latLng = LatLng(objects.get("lat").toString().toDouble(),
                                    objects.get("long").toString().toDouble())
                                val gpsHeading = objects.get("gps_heading").toString().toFloat()
                                driver.driverId?.let { drawMarkerTaxi(latLng, gpsHeading, it) }
                                driver.driverId?.let {
                                    hasMap["myId"] = it
                                }
                            }


                        }*/

                    }
            } else {
                isCanNotBack = false
                MessageUtils.showError(self(), "", it.message)
                binding.recyclerView.visibility = View.VISIBLE
            }
        }


        viewModel.loadingBookingPreviewDoCheckoutLiveData.observe(self()) {
            binding.recyclerView.visibility = if (it) View.GONE else View.VISIBLE
            binding.loadingShimmer.visibility = if (it) View.VISIBLE else View.GONE
            binding.containerNotFoundTaxi.visibility = View.GONE
            binding.actionBookNow.isEnabled = false
        }

        viewModel.dataBookingPreviewDoCheckoutLiveData.observe(self()) {
            if (it != null && it.success && it.data != null) {
                this.bookingPreviewDoCheckoutModel = it.data
                enableBottonBookNow()
                binding.recyclerView.visibility = View.VISIBLE
                selectCarBookingAdapter.add(it.data.vehicle)
                binding.recyclerView.apply {
                    layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = selectCarBookingAdapter
                }

                val driver: ArrayList<Drivers> = it.data.drivers
                drawMarkerTaxiWithLatLng(driver)
            } else {
                binding.actionBookNow.isEnabled = false
                binding.containerNotFoundTaxi.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
        }


        viewModel.loadingCancelBookingLiveData.observe(self()) {
            // binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE

        }
        viewModel.dataCancelBookingLiveData.observe(self()) {
            if (it != null && it.success) {
                timer.cancel()
                binding.actionCancelBooking.visibility = View.GONE
                binding.actionBookNow.visibility = View.VISIBLE
                binding.actionBookNow.isEnabled = true
                hasPointOnMapB = false
                hasPointOnMapA = false
                binding.tvAddressNameA.setTextColor(getColor(R.color.black))
                binding.tvAddressNameB.setTextColor(getColor(R.color.black))
                binding.loadingLookingDriver.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE

            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAction() {
        binding.actionNextStep.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                    RedirectClass.gotoCustomerMapPreView(
                        self(),
                        GsonConverterHelper.convertGenericClassToJson(locationKioskNearBy)
                    )
                } else {
                    PermissionHelper.requestMultiPermission(
                        self(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { hasPermission ->
                        if (hasPermission) {
                            RedirectClass.gotoCustomerMapPreView(
                                self(),
                                GsonConverterHelper.convertGenericClassToJson(locationKioskNearBy)
                            )
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

        binding.actionLocationUser.setOnClickListener {
            val latLng = currentLatitude?.let { it1 ->
                currentLongtitude?.let { it2 ->
                    LatLng(
                        it1,
                        it2
                    )
                }
            }
            if (latLng != null) {
                moveLocation(latLng)
            }

        }

        binding.actionLoactionTaxiDriver.setOnClickListener {
            val latLng = taxiLatitude?.let { it1 ->
                taxiLongtitude?.let { it2 ->
                    LatLng(
                        it1,
                        it2
                    )
                }
            }
            if (latLng != null) {
                moveLocation(latLng)
            }
        }
        binding.actionParkingTaxi.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                    viewModel.submitListAllLocationKiosk(true)
                } else {
                    PermissionHelper.requestMultiPermission(
                        self(),
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { hasPermission ->
                        if (hasPermission) {
                            viewModel.submitListAllLocationKiosk(true)
                        } else {
                            MessageUtils.showError(
                                self(),
                                null,
                                "Permission Camera, Storage and location are deny"
                            )
                        }
                    }
                }
            } else {
                globalRequestDeviceGps()
            }
        }

        binding.actionBack.setOnClickListener {
            if (!hasPointOnMapA && !hasPointOnMapB && !isCanNotBack) {
                finish()
            } else if (!isCanNotBack) {
                binding.recyclerView.visibility = View.GONE
                binding.tvAddressNameA.setTextColor(getColor(R.color.black))
                binding.tvAddressNameB.setTextColor(getColor(R.color.black))
                binding.containerSetPointLocation.visibility = View.GONE
                if (hasPointOnMapA) {
                    drawMarker("", currentLatLngA, Config.TypeMapMarker.markerA)
                } else if (hasPointOnMapB) {
                    drawMarker("", currentLatLngB, Config.TypeMapMarker.markerB)
                }
                binding.actionBookNow.text = String.format("%s", getString(R.string.book_now))
                hasPointOnMapA = false
                hasPointOnMapB = false
            }
        }

        binding.actionSetting.setOnClickListener {
            openMapSettings()
        }

        binding.actionScan.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                    RedirectClass.gotoScanQRCodeActivity(self())
                } else {
                    PermissionHelper.requestMultiPermission(
                        self(),
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { hasPermission ->
                        if (hasPermission) {
                            RedirectClass.gotoScanQRCodeActivity(self())
                        } else {
                            MessageUtils.showError(
                                self(),
                                null,
                                "Permission Camera, Storage and location are deny"
                            )
                        }
                    }
                }
            } else {
                globalRequestDeviceGps()
            }

        }

        selectCarBookingAdapter.onSelectRow = { carBookingModel ->
            this.carBookingModel = carBookingModel
            enableBottonBookNow()
            // submitCheckoutTaxi(carBookingModel)
        }

        val shortAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)

        binding.actionBookNow.setOnClickListener {
            binding.tvAddressNameA.setTextColor(getColor(R.color.black))
            binding.tvAddressNameB.setTextColor(getColor(R.color.black))
            if (hasPointOnMapA) {
                hasPointOnMapA = false
                binding.containerSetPointLocation.visibility = View.GONE
                binding.tvAddressNameA.text = String.format("%s", addressPointA)
                binding.actionBookNow.text =
                    String.format("%s", getString(R.string.confirm_booking))
                addressPointA?.let { it1 -> drawMarker(it1, latLngA, Config.TypeMapMarker.markerA) }
                currentLatLngA = latLngA
                showAllMarker()
            } else if (hasPointOnMapB) {
                hasPointOnMapB = false
                binding.containerSetPointLocation.visibility = View.GONE
                binding.tvAddressNameB.text = String.format("%s", addressPointB)
                binding.actionBookNow.text =
                    String.format("%s", getString(R.string.confirm_booking))
                addressPointB?.let { it1 -> drawMarker(it1, latLngB, Config.TypeMapMarker.markerB) }
                currentLatLngB = latLngB
                showAllMarker()
            } else if (bookingPreviewDoCheckoutModel.fromLocation == null) {
                MessageUtils.showError(self(), "", "Please select location A")
            } else if (bookingPreviewDoCheckoutModel.toLocation == null) {
                MessageUtils.showError(self(), "", "Please select location B")
            } else if (carBookingModel?.id == null) {
                MessageUtils.showError(self(), "", "Please select location Vehicle")
            } else {
                isCanNotBack = true
                binding.loadingLookingDriver.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.loadingShimmer.visibility = View.GONE
                binding.actionBookNow.isEnabled = false
                submitCheckout()
            }
            /*  if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                  if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                      if (carBookingModel == null) {
                          MessageUtils.showError(self(),
                              "",
                              getString(R.string.please_select_vehicle))
                          return@setOnClickListener
                      }
                      carBookingModel?.let { it1 -> submitCheckoutTaxi(it1) }
                  } else {
                      PermissionHelper.requestMultiPermission(
                          self(),
                          arrayOf(
                              Manifest.permission.ACCESS_FINE_LOCATION,
                              Manifest.permission.ACCESS_COARSE_LOCATION
                          )
                      ) { hasPermission ->
                          if (hasPermission) {
                              carBookingModel?.let { it1 -> submitCheckoutTaxi(it1) }
                          } else {
                              MessageUtils.showError(
                                  self(),
                                  "",
                                  "Permission Deny!"
                              )
                          }
                      }
                  }
              } else {
                  if (applicationContext is BaseCoreActivity) {
                      (self() as BaseCoreActivity).globalRequestDeviceGps()
                  }
              }*/
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
                                                geometyString
                                            )
                                        val listLatLng = geomety.geometry?.coordinates
                                        titleFromSearch = geomety.text.toString()
                                        descriptionFromSearch =
                                            geomety.placeName.toString()
                                        listLatLng?.let {
                                            if (it.size >= 2) {
                                                val latLng = LatLng(it[1], it[0])
                                                latLngMarkerClick = latLng
                                                //moveCameraWhenSearch(latLng)
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
                                                        geometyString
                                                    )
                                                val listLatLng = geomety.geometry?.coordinates
                                                titleFromSearch = geomety.text.toString()
                                                descriptionFromSearch =
                                                    geomety.placeName.toString()
                                                listLatLng?.let {
                                                    if (it.size >= 2) {
                                                        val latLng = LatLng(it[1], it[0])
                                                        latLngMarkerClick = latLng
                                                        //moveCameraWhenSearch(latLng)
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

        binding.actionPickUpAddres.setOnClickListener {
            if (!hasPointOnMapA && !hasPointOnMapB) {
                SearchMapActivity.keySearch = Config.KeySearch.pointA
                RedirectClass.gotoSearchMapActivity(self(),
                    object : BetterActivityResult.OnActivityResult<ActivityResult> {
                        override fun onActivityResult(result: ActivityResult) {
                            if (result.resultCode == RESULT_OK) {
                                binding.actionBookNow.isEnabled = true
                                hasAddressA = true
                                hasAddressB = false
                                val intent = result.data
                                if (intent != null && intent.hasExtra(Config.KeySearch.pointA) && intent.hasExtra(
                                        Config.KeySearch.currentGPS
                                    )
                                ) {
                                    fetchCurrentLocation(false)
                                    binding.containerSetPointLocation.visibility = View.GONE
                                    binding.tvAddressNameA.text =
                                        String.format(
                                            "%s",
                                            getString(R.string.your_curren_location)
                                        )
                                } else if (intent != null && intent.hasExtra(Config.KeySearch.pointA) && intent.hasExtra(
                                        Config.KeySearch.setPointOnMapA
                                    )
                                ) {
                                    fetchCurrentLocation(true)
                                    binding.containerSetPointLocation.visibility = View.VISIBLE
                                    markerA?.remove()
                                    hasPointOnMapA = true
                                    binding.tvAddressNameA.setTextColor(getColor(R.color.light_gray))
                                    binding.tvAddressNameB.setTextColor(getColor(R.color.light_gray))
                                    binding.actionBookNow.text =
                                        String.format("%s", "Confirm Drop-off address")
                                    binding.recyclerView.visibility = View.GONE
                                } else if (intent != null && intent.hasExtra(SearchMapActivity.geometyModelKey)) {
                                    val resultString =
                                        intent.getStringExtra(SearchMapActivity.geometyModelKey)
                                    val results: Results =
                                        GsonConverterHelper.getJsonObjectToGenericClass(
                                            resultString
                                        )
                                    titleFromSearch = results.name.toString()

                                    val latLng =
                                        results.geometry?.viewport?.southwest?.lat?.let { it1 ->
                                            results.geometry?.viewport?.southwest?.lng?.let { it2 ->
                                                LatLng(
                                                    it1,
                                                    it2
                                                )
                                            }
                                        }
                                    latLngMarkerClick = latLng
                                    listMarkerOption?.add(MarkerOption(latLng))
                                    if (latLng != null) {
                                        results.formattedAddress?.let { it1 ->
                                            moveCameraWhenSearch(
                                                it1,
                                                latLng,
                                                Config.TypeMapMarker.markerA
                                            )
                                        }
                                    }
                                    binding.tvAddressNameA.text = String.format("%s", results.name)
                                    binding.containerSetPointLocation.visibility = View.GONE
                                    hasPointOnMapA = false

                                }
                            }
                        }

                    })
            }

        }
        binding.actionDropOffAddress.setOnClickListener {
            if (!hasPointOnMapA && !hasPointOnMapB) {
                SearchMapActivity.keySearch = Config.KeySearch.pointB
                RedirectClass.gotoSearchMapActivity(self(),
                    object : BetterActivityResult.OnActivityResult<ActivityResult> {
                        override fun onActivityResult(result: ActivityResult) {
                            if (result.resultCode == RESULT_OK) {
                                binding.actionBookNow.isEnabled = true
                                hasAddressA = false
                                hasAddressB = true
                                val intent = result.data
                                if (intent != null && intent.hasExtra(Config.KeySearch.pointB) && intent.hasExtra(
                                        Config.KeySearch.setPointOnMapB
                                    )
                                ) {
                                    hasPointOnMapB = true
                                    binding.tvAddressNameA.setTextColor(getColor(R.color.light_gray))
                                    binding.tvAddressNameB.setTextColor(getColor(R.color.light_gray))
                                    fetchCurrentLocation(true)
                                    binding.containerSetPointLocation.visibility = View.VISIBLE
                                    markerB?.remove()
                                    binding.actionBookNow.text =
                                        String.format("%s", "Confirm Drop-off address")
                                    binding.recyclerView.visibility = View.GONE
                                } else if (intent != null && intent.hasExtra(SearchMapActivity.geometyModelKey)) {
                                    val resultString =
                                        intent.getStringExtra(SearchMapActivity.geometyModelKey)
                                    val results: Results =
                                        GsonConverterHelper.getJsonObjectToGenericClass(
                                            resultString
                                        )
                                    titleFromSearch = results.name.toString()
                                    /* descriptionFromSearch =
                                         geomety.placeName.toString()*/

                                    val latLng =
                                        results.geometry?.viewport?.southwest?.lat?.let { it1 ->
                                            results.geometry?.viewport?.southwest?.lng?.let { it2 ->
                                                LatLng(
                                                    it1,
                                                    it2
                                                )
                                            }
                                        }
                                    listMarkerOption?.add(MarkerOption(latLng))
                                    latLngMarkerClick = latLng
                                    if (latLng != null) {
                                        results.formattedAddress?.let { it1 ->
                                            moveCameraWhenSearch(
                                                it1,
                                                latLng,
                                                Config.TypeMapMarker.markerB
                                            )
                                        }
                                    }
                                    binding.tvAddressNameB.text = String.format("%s", results.name)

                                }
                            }
                        }

                    })
            }

        }

        binding.actionGps.setOnClickListener {
            binding.tvAddressNameA.setTextColor(getColor(R.color.black))
            binding.tvAddressNameB.setTextColor(getColor(R.color.black))
            hasPointOnMapA = false
            hasPointOnMapB = false
            fetchCurrentLocation(true)
        }


        binding.loadingShimmer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    if (isClickShimmer(startX, endX, startY, endY)) {
                        AppLOGG.d("TopSellingProduct", "has click")
                    }
                }
            }
            v.parent.requestDisallowInterceptTouchEvent(true) //specific to my project
            false //specific to my project
        }


        binding.loadingLookingDriver.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    if (isClickShimmer(startX, endX, startY, endY)) {
                        AppLOGG.d("TopSellingProduct", "has click")
                    }
                }
            }
            v.parent.requestDisallowInterceptTouchEvent(true) //specific to my project
            false //specific to my project
        }




        binding.actionCancelBooking.setOnClickListener {
            isCanNotBack = false
            viewModel.submitCancelBooingTaxi(outTradeNo ?: "")
        }
    }


    private fun drawMarkerTaxiWithLatLng(driver: ArrayList<Drivers>) {
        var hasMap = HashMap<String, Int>()
        for (driver: Drivers in driver) {
            ParseLiveLocationHelper
                .newInstance?.let { parseServer ->
                    val parseQuery: ParseQuery<ParseObject> =
                        parseServer.checkLiveUserCustomerParseServer(driver.driverId ?: -1)
                    ParseLiveQueryClient.Factory.getClient().subscribe(parseQuery)
                        .handleEvent(SubscriptionHandling.Event.UPDATE) { _: ParseQuery<ParseObject?>?, parseObject: ParseObject? ->
                            if (parseObject != null) {
                                val latLng = LatLng(
                                    parseObject.get("lat").toString().toDouble(),
                                    parseObject.get("lng").toString().toDouble()
                                )
                                val markerMap = markerHasMap[parseObject.get("user_id")]
                                markerMap?.position = latLng
                            }

                        }
                    parseQuery.getFirstInBackground { objects, exception ->
                        if (exception != null) {
                            AppLOGG.d(
                                "LIVEUSERDATA",
                                "ex--> " + Gson().toJson(exception) + "id-->" + driver.driverId
                            )
                        } else if (objects != null) {
                            val latLng = LatLng(
                                objects.get("lat").toString().toDouble(),
                                objects.get("long").toString().toDouble()
                            )
                            val gpsHeading = objects.get("gps_heading").toString().toFloat()
                            driver.driverId?.let { drawMarkerTaxi(latLng, gpsHeading, it) }
                            driver.driverId?.let {
                                hasMap["myId"] = it
                            }
                        }


                    }

                }
        }
    }

    private fun isClickShimmer(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = Math.abs(startX - endX)
        val differenceY = Math.abs(startY - endY)
        return !(differenceX > CLICK_ACTION_THRESHOLD /* =5 */ || differenceY > CLICK_ACTION_THRESHOLD)
    }

    private fun mapScreenShot(previewCheckout: String) {
        marker?.let setOnClickListener@{
            try {
                val geocoder = Geocoder(self(), Locale.getDefault())
                val addresses: List<Address> =
                    geocoder.getFromLocation(it.position.latitude, it.position.longitude, 1)

                if (addresses.isEmpty() || addresses.first().getAddressLine(0).isEmpty()) {
                    Toast.makeText(
                        self(),
                        getString(R.string.invalid_location_please_try_another),
                        Toast.LENGTH_LONG
                    ).show()
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
                    File(file.absolutePath).writeBitmap(
                        bitmap,
                        Bitmap.CompressFormat.PNG,
                        100
                    )
                    mapScreenShotPath = file.absolutePath
                    val adminArea: String =
                        if (!TextUtils.isEmpty(titleFromSearch)) titleFromSearch else addressIndex?.adminArea.toString()
                    val countryName: String =
                        if (!TextUtils.isEmpty(titleFromSearch)) "" else addressIndex?.countryName.toString()
                    val addressLines: String =
                        if (!TextUtils.isEmpty(descriptionFromSearch)) descriptionFromSearch else addressIndex?.getAddressLine(
                            0
                        ).toString()
                    RedirectClass.gotoPreviewCheckoutActivity(
                        self(),
                        previewCheckout,
                        carBookingModel?.id.toString(),
                        locationKioskNearBy?.deviceId.toString(),
                        mapScreenShotPath.toString(),
                        adminArea,
                        countryName,
                        addressLines,
                        latLngMarkerClick?.latitude.toString(),
                        latLngMarkerClick?.longitude.toString(),
                        " carBookingModel?.title.toString()"
                    )
                }
            } catch (t: Throwable) {
                Toast.makeText(
                    self(),
                    getString(R.string.some_thing_went_wrong_plz_try_again),
                    Toast.LENGTH_LONG
                ).show()
            }
            return@setOnClickListener
        }
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun submitCheckoutTaxi(terms: Terms) {
        val body = HashMap<String, Any>()
        val bodyMap = HashMap<String, Any>()
        body["car_id"] = terms.id.toString()
        body["select_map"] = 1
        body["device_id"] = locationKioskNearBy?.deviceId.toString()
        bodyMap["title"] = address.toString()
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.pick_up -> {
                if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                    if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                        RedirectClass.gotoScanQRCodeActivity(self())
                    } else {
                        PermissionHelper.requestMultiPermission(
                            self(),
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) { hasPermission ->
                            if (hasPermission) {
                                RedirectClass.gotoScanQRCodeActivity(self())
                            } else {
                                MessageUtils.showError(
                                    self(),
                                    null,
                                    "Permission Camera, Storage and location are deny"
                                )
                            }
                        }
                    }
                } else {
                    globalRequestDeviceGps()
                }
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.pick_up_location_menu, menu)
        return true
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.let {
            mMap?.uiSettings?.isMyLocationButtonEnabled = true
        }

        if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
            if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                fetchCurrentLocation(false)
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) { _ ->
                    fetchCurrentLocation(false)
                }
            }
        } else {
            globalRequestDeviceGps()
        }
        mMap?.setOnCameraMoveStartedListener {
            binding.lottieMaker.setMinAndMaxFrame(0, 15)
            binding.lottieMaker.pauseAnimation()
        }

        mMap?.setOnCameraIdleListener {
            binding.lottieMaker.setMinAndMaxFrame(55, 70)
            binding.lottieMaker.playAnimation()
            addressIndex =
                AddressHelper.getStringAddressByIndex(
                    self(),
                    mMap?.cameraPosition?.target?.latitude,
                    mMap?.cameraPosition?.target?.longitude
                )
            address = AddressHelper.getStringAddress(
                self(),
                mMap?.cameraPosition?.target?.latitude,
                mMap?.cameraPosition?.target?.longitude
            )
            latLngMarkerClick = mMap?.cameraPosition?.target?.latitude?.let {
                mMap?.cameraPosition?.target?.longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1
                    )
                }
            }
            val lat: Double? = mMap?.cameraPosition?.target?.latitude
            val lng: Double? = mMap?.cameraPosition?.target?.longitude

            if (lat != null && lng != null) {
                if (hasPointOnMapA) {
                    latLngA = LatLng(lat, lng)
                    listMarkerOption?.add(MarkerOption(latLngA))
                } else if (hasPointOnMapB) {
                    latLngB = LatLng(lat, lng)
                    listMarkerOption?.add(MarkerOption(latLngB))
                }

            }
            val finalAddress =
                if (!TextUtils.isEmpty(descriptionFromSearch)) descriptionFromSearch else address
            addressPointA = finalAddress
            addressPointB = finalAddress

            binding.containerAddress.visibility =
                if (!TextUtils.isEmpty(finalAddress)) View.VISIBLE else View.GONE
            binding.showAddress.text = String.format("%s", finalAddress)
            carBookingModel?.let { /*submitCheckoutTaxi(it)*/ }
        }
        MapsInitializer.initialize(this)

    }




    private fun initGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.pick_up_location_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }



    private fun initRecyclerview() {
        // locationKioskNearBy?.let { selectCarBookingAdapter.add(it.terms) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = selectCarBookingAdapter
        }
    }

    private fun fetchCurrentLocation(isSetPointOnMap: Boolean) {
        @SuppressLint("MissingPermission")
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location: Location ->
            val latLng = LatLng(location.latitude, location.longitude)
            currentLatLng = latLng
            currentLatitude = location.latitude
            currentLongtitude = location.longitude
            listMarkerOption?.add(MarkerOption(latLng, Config.TypeMarkerOptions.userMarkerType))
            listMarkerOption?.add(MarkerOption(locationKioskNearBy?.mapLat?.let {
                locationKioskNearBy?.mapLng?.let { it1 ->
                    LatLng(
                        it,
                        it1
                    )
                }
            }, Config.TypeMarkerOptions.kioskMarkerType))
            moveToCurrentLocation(latLng, isSetPointOnMap)
            currentLatLngA = latLng
        }
    }

    private fun moveToCurrentLocation(latLng: LatLng, isSetPointOnMap: Boolean) {
        val update = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap?.moveCamera(update)
        mMap?.animateCamera(zoom)
        taxiLatitude = locationKioskNearBy?.mapLat
        taxiLongtitude = locationKioskNearBy?.mapLng
        // TODO: drawMarker taxi
        val latLngTaxi = locationKioskNearBy?.mapLat?.let {
            locationKioskNearBy?.mapLng?.let { it1 ->
                LatLng(
                    it,
                    it1
                )
            }
        }
        // TODO: drawMarker user
        if (!isSetPointOnMap) {
            drawMarker(
                getString(R.string.your_curren_location),
                latLng,
                Config.TypeMapMarker.markerA
            )
        }
        showAllMarker()
    }

    private fun moveCameraWhenSearch(titleMarker: String, latLng: LatLng, title: String) {
        val update = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap?.moveCamera(update)
        mMap?.animateCamera(zoom)
        mMap?.clear()
        drawMarker(titleMarker, latLng, title)
    }

    private fun drawMarker(titleMarker: String, latLng: LatLng, title: String) {
        if (title == Config.TypeMapMarker.markerA) {
            mMap?.moveCamera(CameraUpdateFactory.zoomTo(10f))
            markerA?.remove()
            markerA = mMap?.addMarker(
                MarkerOptions().position(latLng)
                    .title(titleMarker)
                    .icon(
                        MapHelper.bitmapDescriptorFromVector(
                            self(),
                            R.drawable.round_corner_maker_point_a
                        )
                    )
            )

            markerA?.showInfoWindow()
            latLngA = latLng
            fromTitle = titleMarker
            submitBookingPreviewDoCheckout(latLngA, latLngB, fromTitle, toTitle)
        } else if (title == Config.TypeMapMarker.markerB) {
            markerB?.remove()
            markerB = mMap?.addMarker(MarkerOptions().position(latLng)
                .title(titleMarker)
                .icon(MapHelper.bitmapDescriptorFromVector(self(),
                    R.drawable.round_corner_maker_point_b)))

            val polylineOptions = PolylineOptions()
            polylineOptions.addAll(decodePoly("_rseAunb_Sg@eHwIf@gKr@"))
            mMap?.addPolyline(polylineOptions.width(20f).color(getColor(R.color.blue_300)).geodesic(true))
            getApiMap()
            markerB?.showInfoWindow()
            latLngB = latLng
            toTitle = titleMarker
            submitBookingPreviewDoCheckout(latLngA, latLngB, fromTitle, toTitle)

        }
        showAllMarker()
    }

    private fun getApiMap() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://graphhopper.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EazyTaxiApi::class.java)
        val mMapApi = retrofit.polylineApi(
            latLngA.latitude.toString() + "," + latLngA.longitude,
            latLngB.latitude.toString() + "," + latLngB.longitude,
            "car",
            true,
            "4b61d087-c38f-47f6-ba3e-3ba120584177"
        )
        mMapApi.enqueue(object : Callback<JsonElement> {
            @SuppressLint("LogNotTimber")
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                val points = response.body()?.asJsonObject?.get("paths")?.asJsonArray?.get(0)?.asJsonObject?.get("points")?.asString
                val polylineOptions = PolylineOptions()
                polylineOptions.addAll(decodePoly(points.toString()))
                mMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))
                mMap?.addPolyline(
                    polylineOptions.width(10f).color(getColor(R.color.colorPrimary))
                        .geodesic(false)
                )
                Log.d("points", "message$points")
                Log.d("success", "success" + Gson().toJson("success message"))

            }


            @SuppressLint("LogNotTimber")
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d("error", "onFailure" + Gson().toJson("error message"))

            }
        })
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
            val latLng = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(latLng)
        }
        for (lat in poly) {
            Log.d("asdsadsad", "lat :  " + lat.latitude + " lng :" + lat.longitude)
        }
        return poly
    }

    private fun drawMarkerTaxi(latLng: LatLng, gpsHeading: Float, driverId: Int) {
        marker =
            mMap?.addMarker(
                MarkerOptions().position(latLng).title(locationKioskNearBy?.location)
                    .icon(MapHelper.bitmapDescriptorFromVector(R.drawable.ic_live_taxi, self()))
                    .rotation(gpsHeading)
            )
        marker?.setAnchor(0.5f, 0.5f)
        marker?.isFlat = true
        marker?.showInfoWindow()
        marker?.let {
            markerHasMap[driverId.toString()] = it
        }

    }

    private fun moveLocation(latLng: LatLng) {
        val update = CameraUpdateFactory.newLatLng(latLng)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap?.moveCamera(update)
        mMap?.animateCamera(zoom)
    }

    private fun showAllMarker() {
        val builder = LatLngBounds.Builder()
        listMarkerOption?.let {
            for (markerOption in it) {
                markerOption.latLng?.let { latLng ->
                    builder.include(latLng)
                }
            }
        }
        val bounds: LatLngBounds = builder.build()
        val with = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val panding = (with * 0.33).toInt()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, with, height, panding)
        mMap?.animateCamera(cu)

    }

    private fun submitCheckout() {
        val fromLat: Double? = bookingPreviewDoCheckoutModel.fromLocation?.get(0)
        val fromLong: Double? = bookingPreviewDoCheckoutModel.fromLocation?.get(1)
        val fromTitle = bookingPreviewDoCheckoutModel.fromTitle
        val toLat: Double? = bookingPreviewDoCheckoutModel.toLocation?.get(0)
        val toLong: Double? = bookingPreviewDoCheckoutModel.toLocation?.get(1)
        val toTitle = bookingPreviewDoCheckoutModel.toTitle
        val body = HashMap<String, Any>()
        val bodyMap = HashMap<String, Any>()
        body["car_id"] = carBookingModel?.id.toString()
        fromLat?.let { bodyMap["from_lat"] = it }
        fromLong?.let { bodyMap["from_long"] = it }
        bodyMap["from_title"] = fromTitle.toString()
        toLat?.let { bodyMap["to_lat"] = it }
        toLong?.let { bodyMap["to_long"] = it }
        bodyMap["to_title"] = toTitle.toString()
        body["map"] = bodyMap
        viewModel.submitCheckout(body)
    }

    private fun submitBookingPreviewDoCheckout(
        latLngA: LatLng,
        latLngB: LatLng,
        titleA: String,
        titleB: String,
    ) {
        val deviceId =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val body = HashMap<String, Any>()
        val jsonObject = JsonObject()
        jsonObject.addProperty("from_lat", latLngA.latitude)
        jsonObject.addProperty("from_long", latLngA.longitude)
        jsonObject.addProperty("from_title", titleA)
        if (latLngB.latitude != 0.0 && latLngB.longitude != 0.0) {
            jsonObject.addProperty("to_lat", latLngB.latitude)
            jsonObject.addProperty("to_long", latLngB.longitude)
            jsonObject.addProperty("to_title", titleB)
        }
        body["map"] = jsonObject
        viewModel.submitBookingPreviewDoCheckout(body)

    }

    private fun enableBottonBookNow() {
        binding.actionBookNow.isEnabled =
            if (bookingPreviewDoCheckoutModel != null &&
                bookingPreviewDoCheckoutModel.fromLocation != null &&
                bookingPreviewDoCheckoutModel.toLocation != null &&
                carBookingModel != null &&
                carBookingModel?.id != null
            ) true else false
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