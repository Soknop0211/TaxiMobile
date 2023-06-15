package com.eazy.daiku.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.HomeScreenModel
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.data.remote.GeneralApi
import com.eazy.daiku.databinding.ActivityMainBinding
import com.eazy.daiku.databinding.ActivityMapPreviewBinding
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import com.eazy.daiku.ui.map.MapPreviewActivity
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.adapter.HomeScreenAdapter
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.bottom_sheet.ConfirmBookingBottomSheet
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.custom.StaticAirportTaxiAlertDialog
import com.eazy.daiku.utility.enumerable.HomeScreenActionEnum
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.service.ExitAppService
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.eazy.daiku.utility.view_model.QrCodeVm
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


const val TAG = "EazyTaxiApplication"

class MainActivity : BaseActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var binding : ActivityMapPreviewBinding

    private val qrCodeVm: QrCodeVm by viewModels {
        factory
    }
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }
    private val viewModel: ProcessCustomerBookingViewModel by viewModels {
        factory
    }

    private val intentFilter = IntentFilter()
    private var homeScreenAdapter: HomeScreenAdapter? = null
    private var bookingTaxiObj: ParseObject? = null
    private var isBooking: Boolean = false


    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mEazyTaxiApplication?.setCurrentMainActivity(this)

        intiView()
        initObserved()
        doAction()
        intiData()


    }


    private fun checkStatusProcessingBooking(code: String) {
        ParseLiveLocationHelper
            .newInstance?.let { parseServer ->
                val parseQuery: ParseQuery<ParseObject> =
                    parseServer.checkStatusAssignTaxi(code)
                ParseLiveQueryClient.Factory.getClient().subscribe(parseQuery)
                    .handleEvent(SubscriptionHandling.Event.UPDATE) { _: ParseQuery<ParseObject?>?, parseObject: ParseObject? ->
                        if (parseObject != null) {
                            val status = parseObject.get("status").toString()
                            if (status == "Finished") {
                                runOnUiThread {
                                    mainBinding.tripProcessingRelativeLayout.visibility = View.GONE
                                    isBooking = false
                                }
                            } else if (status == "Cus-Cancelled" || status == "Cancelled") {
                                runOnUiThread {
                                    mainBinding.tripProcessingRelativeLayout.visibility = View.GONE
                                    isBooking = false
                                }
                            }
                        }
                    }
            }
    }

    override fun onStart() {
        super.onStart()
        //if mCurrentActivity in application is null need to register in heer
        onRegisterBindServiceForegroundConnection()

        registerReceiver(myBroadcastReceiver, intentFilter)
        UserDefaultSingleTon.newInstance?.hasMainActivity = this

    }

    override fun onResume() {
        super.onResume()
        //if mCurrentActivity in application is null need to register in heer
        onRegisterForegroundService()

        homeScreenAdapter?.let {
            if (BuildConfig.IS_WEGO_TAXI) {
                val user = getUserUserToSharePreference()
                val isWeGoEmployee = user?.isEmployee == Config.WeGoBusiness.Employee
                if (isWeGoEmployee) {
                    it.removeView(HomeScreenActionEnum.Wallet)
                } else {
                    it.removeView(HomeScreenActionEnum.Wallet)
                    it.addHomeScreen(
                        HomeScreenModel(
                            id = 1,
                            name = getString(R.string.wallet),
                            icon = if (BuildConfig.IS_WEGO_TAXI) R.drawable.wallet_icon_wego else R.drawable.wallet_icon_1,
                            actionEnum = HomeScreenActionEnum.Wallet
                        )
                    )
                }
            }
        }

    }

    override fun onStop() {

        val screenIsDie = !this.window.decorView.rootView.isShown
        AppLOGG.d(TAG, "OnStop screen state# ${screenIsDie}")
        Handler(Looper.getMainLooper()).postDelayed(
            {
                // This method will be executed once the timer is over
                if (screenIsDie) {
                    disableDisplayOnMapLiveUser(/*newUpdate*/true)
                }
            }, 2000 // value in milliseconds
        )
        super.onStop()

    }

    override fun onDestroy() {

        unregisterReceiver(myBroadcastReceiver)
        UserDefaultSingleTon.newInstance?.hasMainActivity = null

        unRegisterLocationLiveUser()
        unSubscribeLocationForeground()

        super.onDestroy()

    }

    private fun intiData() {
        val notCustomerApp = !BuildConfig.IS_CUSTOMER
        if (notCustomerApp) {
            //fetch processing
            qrCodeVm.fetchTripProcessing()
        } else {
            viewModel.submitBookingProcessing()
        }
    }

    private fun initObserved() {
        qrCodeVm.loadingScanQrMutableLiveData.observe(this) { hasLoading ->
            mainBinding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }

        qrCodeVm.fetchProcessingMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                if (respondState.data != null) {
                    val processing = respondState.data
                    mainBinding.tripProcessingRelativeLayout.visibility = View.VISIBLE
                    homeScreenAdapter?.updateView(HomeScreenActionEnum.ScanQR, false)
                    UserDefaultSingleTon.newInstance?.qrCodeRespond = processing
                    UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Processing
                    val title: String = processing.title ?: ""
                    val fromTitle: String = processing.from_title ?: ""
                    val toTitle: String = processing.to_title ?: ""

                    mainBinding.hotelNameTv.text =
                        String.format("%s %s %s", title, fromTitle, toTitle)
                    mainBinding.priceTv.text = String.format(
                        "%sKm • $%s",
                        processing.distance ?: "-",
                        processing.total_amount ?: "-"
                    )
                    startUpdateLocationToParseServer()
                } else {
                    mainBinding.tripProcessingRelativeLayout.visibility = View.GONE
                    homeScreenAdapter?.updateView(HomeScreenActionEnum.ScanQR, true)
                    UserDefaultSingleTon.newInstance?.qrCodeRespond = null
                    UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Nothing

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            disableDisplayOnMapLiveUser(true)
                            needToCheckPermissionLocation()
                        }, 1000 // value in milliseconds
                    )
                }
            } else {
                globalShowError(respondState.message)
            }
        }

        userInfoVM.loadingUserLiveData.observe(this) { hasLoading ->
            mainBinding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }

        userInfoVM.dataUserLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let {
                    saveUserToSharePreference(it)
                    RedirectClass.gotoProfileActivity(self())
                } ?: globalShowError("Object user is null")
            } else {
                globalShowError(respondState.message)
            }
        }

        // TODO: Start observer search location taxi near by
        viewModel.loadingSearchLocationKioskLiveData.observe(self()) { hasLoading ->
            mainBinding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        viewModel.dataSearchLocationKioskLiveData.observe(self()) { respond ->
            if (respond != null && respond.success) {
                if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                    if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                        if (respond.data != null) {
                            RedirectClass.gotoPickUpLocation(self(),
                                GsonConverterHelper.convertGenericClassToJson(respond.data),
                                object : BetterActivityResult.OnActivityResult<ActivityResult> {
                                    override fun onActivityResult(result: ActivityResult) {

                                    }

                                })
                        } else {
                            MessageUtils.showError(self(), "", respond.message)
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
                                if (respond.data != null) {
                                    RedirectClass.gotoPickUpLocation(self(),
                                        GsonConverterHelper.convertGenericClassToJson(respond.data),
                                        object :
                                            BetterActivityResult.OnActivityResult<ActivityResult> {
                                            override fun onActivityResult(result: ActivityResult) {

                                            }

                                        })
                                } else {
                                    RedirectClass.gotoPickUpLocation(self(),
                                        null,
                                        object :
                                            BetterActivityResult.OnActivityResult<ActivityResult> {
                                            override fun onActivityResult(result: ActivityResult) {

                                            }

                                        })
                                }
                            } else {
                                MessageUtils.showError(self(), null, "Permission Deny!")
                            }
                        }
                    }
                } else {
                    globalRequestDeviceGps()
                }

            } else {
                globalShowError(respond.message)
            }
        }
        // TODO: End observer search location taxi near by


        viewModel.loadingBookingProcessingLiveData.observe(self()) {
            mainBinding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.dataBookingProcessingLiveData.observe(self()) {
            if (it != null && it.success && it.data != null) {
                val qrCodeRespond = QrCodeRespond()
                qrCodeRespond.title = it.data.booking?.destinationInfo?.toTitle
                qrCodeRespond.total_amount = it.data.booking?.destinationInfo?.total
                qrCodeRespond.driverInfo = it.data.booking?.driverInfo
                qrCodeRespond.latitude = it.data.booking?.toLat
                qrCodeRespond.longitude = it.data.booking?.toLong
                qrCodeRespond.distance = it.data.booking?.destinationInfo?.distance
                qrCodeRespond.urlQrcode = it.data.booking?.qrCodeUrl
                qrCodeRespond.vehicle=it.data.booking?.destinationInfo?.vehicle
                UserDefaultSingleTon.newInstance?.qrCodeRespond = qrCodeRespond


                if (it.data.booking?.status != null && it.data.booking?.status != TripEnum.Finished.toString() &&
                    it.data.booking?.status != null && it.data.booking?.status != "Cus-Cancelled" &&
                    it.data.booking?.status != null && it.data.booking?.status != "Cancelled"
                ) {
                    isBooking = true
                    mainBinding.tripProcessingRelativeLayout.visibility = View.VISIBLE

                } else
                    mainBinding.tripProcessingRelativeLayout.visibility = View.GONE
                mainBinding.hotelNameTv.text =
                    String.format("%s", it.data.booking?.destinationInfo?.toTitle ?: "---")
                mainBinding.priceTv.text = String.format(
                    "%sKm . $%s",
                    it.data.booking?.destinationInfo?.distance ?: "---",
                    it.data.booking?.destinationInfo?.total ?: "---"
                )
                mainBinding.hotelNameTv.setTextColor(getColor(R.color.white))
                mainBinding.priceTv.setTextColor(getColor(R.color.white))
                mainBinding.locationSymbolImg.setColorFilter(getColor(R.color.white))
                mainBinding.moreImg.setColorFilter(getColor(R.color.white))

                checkStatusProcessingBooking(it.data.booking?.code.toString())
            }


        }
    }

    private fun intiView() {

        //foreground request initialize 09-08-2022
        foregroundOnlyBroadcastReceiver =
            UserDefaultSingleTon.newInstance?.getForegroundOnlyBroadcastReceiverSingleTon()
//        sharedPreferences =
//            getSharedPreferences(SharedPreferenceUtil.preference_file_key, Context.MODE_PRIVATE)

        //start exit app service
        startService(Intent(this, ExitAppService::class.java))
        UserDefaultSingleTon.newInstance?.networkName = NetworkUtils.getNetworkClass(self())

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        /*
        * w = 1000
        * h = 664
        * aspatio = h/w (1000/664) = 0.66
        *
        * */
        val dpHeightReload = ImageHelper.getWidthScreen(self()) * 0.66
        mainBinding.bannerImg.layoutParams =
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                dpHeightReload.toInt()
            )
        mainBinding.logoAppImg.scaleType =
            if (BuildConfig.IS_CUSTOMER) ImageView.ScaleType.FIT_CENTER else ImageView.ScaleType.CENTER_CROP

        var version = String.format(
            "%s %s(%s) %s",
            getString(R.string.version),
            BuildConfig.VERSION_NAME,
            BuildConfig.BUILD_VERSION,
            if (BuildConfig.IS_DEBUG) "• Dev" else ""
        )
        if (BuildConfig.IS_WEGO_TAXI) {
            mainBinding.logoAppImg.setImageResource(R.drawable.wego_logo_icon)
            version = "Powered by EAZY Partner " + if (BuildConfig.IS_DEBUG) "• Dev" else ""

            val whiteColor = ContextCompat.getColorStateList(self(), R.color.white)
            mainBinding.hotelNameTv.setTextColor(whiteColor)
            mainBinding.priceTv.setTextColor(whiteColor)
            mainBinding.locationSymbolImg.imageTintList = whiteColor
            mainBinding.moreImg.imageTintList = whiteColor

        } else if (BuildConfig.IS_CUSTOMER) {
            mainBinding.logoAppImg.setImageResource(R.drawable.eazy_logo_blue)
        } else {
            mainBinding.logoAppImg.setImageResource(R.drawable.eazy_black_logo)
        }
        mainBinding.versionTv.text = version

        setUpHomeScreenLayout()

    }

    private fun doAction() {

        homeScreenAdapter?.selectedRow = { homeScreenModel ->
            when (homeScreenModel.actionEnum) {
                HomeScreenActionEnum.Wallet -> {
                    if (BuildConfig.IS_CUSTOMER && !isBooking) {
                        // TODO: submit search location kiosk near by
                        //  viewModel.submitSearchLocationKioskNearBy()
                        if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                            if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                                RedirectClass.gotoPickUpLocation(self(),
                                    null,
                                    object : BetterActivityResult.OnActivityResult<ActivityResult> {
                                        override fun onActivityResult(result: ActivityResult) {

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
                                        RedirectClass.gotoPickUpLocation(self(),
                                            null,
                                            object :
                                                BetterActivityResult.OnActivityResult<ActivityResult> {
                                                override fun onActivityResult(result: ActivityResult) {

                                                }

                                            })
                                    } else {
                                        MessageUtils.showError(self(), null, "Permission Deny!")
                                    }
                                }
                            }
                        } else {
                            globalRequestDeviceGps()
                        }
                    } else if (!isBooking) {
                        RedirectClass.gotoMainWalletActivity(self())
                    }
                }
                HomeScreenActionEnum.Profile -> {
                    userInfoVM.fetchUserInfo()
                }
                HomeScreenActionEnum.ScanQR -> {
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
                HomeScreenActionEnum.History -> {
                    if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                        RedirectClass.gotoHistoryTripActivity(self())
                    } else {
                        PermissionHelper.requestMultiPermission(
                            self(),
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) { hasPermission ->
                            if (hasPermission) {
                                RedirectClass.gotoHistoryTripActivity(self())
                            } else {
                                MessageUtils.showError(self(), null, "Permission Deny!")
                            }
                        }
                    }

                }
                else -> {
                }
            }
        }

        mainBinding.tripProcessingRelativeLayout.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(self())) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
                    RedirectClass.gotoMapPreviewActivity(
                        self(),
                        GsonConverterHelper.convertGenericClassToJson(UserDefaultSingleTon.newInstance?.qrCodeRespond)
                    )
                } else {
                    PermissionHelper.requestMultiPermission(
                        self(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { _ ->
                        RedirectClass.gotoMapPreviewActivity(
                            self(),
                            GsonConverterHelper.convertGenericClassToJson(UserDefaultSingleTon.newInstance?.qrCodeRespond)
                        )
                    }
                }
            } else {
                globalRequestDeviceGps()
            }
        }

        mainBinding.tripProcessingRelativeLayout.setOnClickListener {
            intent = Intent(this@MainActivity,MapPreviewActivity :: class.java)
            intent.putExtra("showQrcode","Qrcode")
            startActivity(intent)
        }
        mainBinding.logoAppImg.setOnClickListener {

        }

        mainBinding.confirmAcceptBookingMtc.setOnClickListener {
            ConfirmBookingBottomSheet.newInstance()
                .show(supportFragmentManager, "ConfirmBookingBottomSheet")
        }

        mainBinding.actionCloseImg.setOnClickListener {
            MessageUtils.showConfirm(
                self(),
                getString(R.string.cancel_customer_booking),
                getString(R.string.are_you_sure_to_cancel_this_customer_booking)
            ) { dialog ->  //confirm
                dialog.dismiss()
                globalLoadingViewEnable(true)
                bookingTaxiObj?.let { pObject ->
                    confirmBooking(Config.StatusAssignKey.Cancelled, pObject) { successKey ->
                        globalLoadingViewEnable(false)
                        if (successKey) {
                            mainBinding.confirmAcceptBookingMtc.visibility = View.GONE
                            UserDefaultSingleTon.newInstance?.bookingTaxiModel = null
                        }
                    }
                }
            }
        }
    }

    private fun setUpHomeScreenLayout() {

        val homeScreens = ArrayList<HomeScreenModel>()
        var profileIcon: Int = -1
        var walletIcon: Int = -1
        var historyIcon: Int = -1
        var scanIcon: Int = -1

        if (BuildConfig.IS_WEGO_TAXI) {
            profileIcon = R.drawable.profile_icon_wego
            walletIcon = R.drawable.wallet_icon_wego
            historyIcon = R.drawable.history_icon_wego
            scanIcon = R.drawable.scan_qr_icon_wego
        } else if (BuildConfig.IS_CUSTOMER) {
            profileIcon = R.drawable.profile_icon
            walletIcon = R.drawable.ic_taxi_booking
            historyIcon = R.drawable.history_icon
            scanIcon = R.drawable.scan_qr_icon
        } else if (BuildConfig.IS_TAXI) {
            profileIcon = R.drawable.profile_icon_1
            walletIcon = R.drawable.wallet_icon_1
            historyIcon = R.drawable.history_icon_1
            scanIcon = R.drawable.scan_qr_icon_1
        }
        if (BuildConfig.IS_WEGO_TAXI) {
            val user = getUserUserToSharePreference()
            val isWeGoEmployee = user?.isEmployee == Config.WeGoBusiness.Employee
            if (!isWeGoEmployee) {
                homeScreens.add(
                    HomeScreenModel(
                        id = 1,
                        name = getString(R.string.wallet),
                        icon = walletIcon,
                        actionEnum = HomeScreenActionEnum.Wallet
                    )
                )
            }
        } else {
            homeScreens.add(
                HomeScreenModel(
                    id = 1,
                    name = if (BuildConfig.IS_CUSTOMER) getString(R.string.booking) else getString(R.string.wallet),
                    icon = walletIcon,
                    actionEnum = HomeScreenActionEnum.Wallet
                )
            )
        }

        homeScreens.add(
            HomeScreenModel(
                id = 2,
                name = getString(R.string.profile),
                icon = profileIcon,
                actionEnum = HomeScreenActionEnum.Profile
            )
        )
        if (!BuildConfig.IS_CUSTOMER) {
            homeScreens.add(
                HomeScreenModel(
                    id = 3,
                    name = getString(R.string.scan_qr),
                    icon = scanIcon,
                    actionEnum = HomeScreenActionEnum.ScanQR
                )
            )
        }
        homeScreens.add(
            HomeScreenModel(
                id = 4,
                name = if (BuildConfig.IS_CUSTOMER) getString(R.string.activity) else getString(R.string.history),
                icon = historyIcon,
                actionEnum = HomeScreenActionEnum.History
            )
        )

        homeScreenAdapter = HomeScreenAdapter()
        mainBinding.homeScreenRecyclerView.apply {
            layoutManager = GridLayoutManager(self(), 2)
            adapter = homeScreenAdapter
        }
        homeScreenAdapter?.addHomeScreen(homeScreens)
    }

    private val myBroadcastReceiver = object : MyBroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {
            super.onReceive(p0, p1)
            if (p1 != null) {
                when {
                    p1.hasExtra(reloadProcessingBookingKey) -> {
                        intiData()
                    }

                    p1.hasExtra(finishWhenPaymentCompletedKey) -> {
                        intiData()
                    }
                    p1.hasExtra(reloadPaymentSuccessKey) -> {
                        clearTripSession()
                        mainBinding.tripProcessingRelativeLayout.visibility = View.GONE
                        homeScreenAdapter?.updateView(HomeScreenActionEnum.ScanQR, true)

                    }
                    p1.hasExtra(hasProcessingTripKey) -> {
                        val processing = UserDefaultSingleTon.newInstance?.qrCodeRespond
                        mainBinding.tripProcessingRelativeLayout.visibility = View.VISIBLE
                        homeScreenAdapter?.updateView(HomeScreenActionEnum.ScanQR, false)
                        UserDefaultSingleTon.newInstance?.qrCodeRespond = processing
                        UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Processing
                        mainBinding.hotelNameTv.text = processing?.title ?: "Unknown"
                        mainBinding.priceTv.text = String.format(
                            "%sKm • $%s",
                            processing?.distance ?: "-",
                            processing?.total_amount ?: "-"
                        )

                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                disableDisplayOnMapLiveUser(/*false*/true)
                                unRegisterLocationLiveUser()
                            }, 1000 // value in milliseconds
                        )

                    }
                    p1.hasExtra(confirmBookingKey) -> {
                        if (p1.hasExtra(dataKey)) {
                            val jsonConfirmKeyData: String? = p1.getStringExtra(dataKey)
                            if (jsonConfirmKeyData.equals(Config.StatusAssignKey.Accepted)) {
                                val bookingTaxiModel =
                                    UserDefaultSingleTon.newInstance?.bookingTaxiModel
                                bookingTaxiObj = bookingTaxiModel?.bookingParseObj

                                mainBinding.confirmAcceptBookingMtc.visibility = View.VISIBLE
                                mainBinding.destinationTv.text =
                                    bookingTaxiModel?.destinationInfo?.title
                                        ?: bookingTaxiModel?.destinationInfo?.toTitle ?: "---"
                                mainBinding.bookingCodeTv.text =
                                    bookingTaxiModel?.bookingCode ?: "---"

                            } else if (jsonConfirmKeyData.equals(Config.StatusAssignKey.Rejected)) {
                                mainBinding.confirmAcceptBookingMtc.visibility = View.GONE
                                bookingTaxiObj = null
                            }

                        }
                    }
                }
            }
        }

    }


}
