package com.eazy.daiku.utility.base

import android.Manifest
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.EazyTaxiApplication
import com.eazy.daiku.R
import com.eazy.daiku.data.model.BookingTaxiModel
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.CustomerInfo
import com.eazy.daiku.data.model.server_model.DestinationInfo
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.DateTimeHelper
import com.eazy.daiku.utility.EazyTaxiHelper.getSharePreference
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.custom.ConfirmBookingAlertDialog
import com.eazy.daiku.utility.enumerable.ConfirmKey
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.eazy.daiku.utility.service.location.foreground_.ForegroundOnlyBroadcastReceiver
import com.eazy.daiku.utility.service.location.foreground_.ForegroundOnlyLocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.parse.Parse
import com.parse.ParseInstallation
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import pl.aprilapps.easyphotopicker.ChooserType
import pl.aprilapps.easyphotopicker.EasyImage
import java.util.Calendar


const val TAG = "EazyTaxiApplication"

open class BaseActivity : BaseCoreActivity() {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var confirmBookingAlertDialog: ConfirmBookingAlertDialog? = null
    protected var mEazyTaxiApplication: EazyTaxiApplication? = null

    private var easyImage: EasyImage? = null
    private var shouldShow: Boolean = true

    private var lastLocation: Location? = null
    private var startLocationFlag: Boolean = true
    private var handler: Handler? = null
    private var removeHandler: Boolean = false
    private val delayTimer: Long = 60000 //1 minute
    private val locationsTemp: ArrayList<Location> = ArrayList()
    private val intentFilter = IntentFilter()
    private var mIsStateAlreadySaved = false
    private var mPendingShowDialog = false


    init {
        intentFilter.addAction(MyBroadcastReceiver.notificationAcceptOrder)
    }

    companion object {
        var bookingCode = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mEazyTaxiApplication = this.applicationContext as EazyTaxiApplication
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mEazyTaxiApplication?.setCurrentBaseActivity(this)
        initSubscribeParseServer()

        if (!TextUtils.isEmpty(bookingCode)) {
            subscribeTaxiBookingByCode(bookingCode)
            bookingCode = ""
        }

    }

    override fun onStart() {
        super.onStart()
        registerReceiver(myBroadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // EasyImage request codes are set to be between 374961 and 374965.
        // Toast.makeText(this, "onActivityResult in BaseActivity", Toast.LENGTH_SHORT).show();
        easyImage?.let { easyImage ->
            easyImageCallbacks?.let {
                easyImage.handleActivityResult(requestCode, resultCode, data, this, it)
            }
        }
    }

    open fun chooseGalleryImage(easyImageCallbacks: EasyImage.Callbacks) {
        if (easyImage == null) buildEasyImage(ChooserType.CAMERA_AND_GALLERY)
        easyImage?.let {
            it.openGallery(this)
            this.easyImageCallbacks = easyImageCallbacks
        }
    }

    open fun buildEasyImage(chooserType: ChooserType) {
        buildEasyImage(chooserType, false)
    }

    open fun buildEasyImage(chooserType: ChooserType, isMultiple: Boolean) {
        easyImage = EasyImage.Builder(this) // Chooser only
            // Will appear as a system chooser title, PERSONAL empty string
            //                .setChooserTitle("Pick media")
            // Will tell chooser that it should show documents or gallery apps
            //                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
            //you can use this or the one below
            // Setting to true will cause taken pictures to show up in the device gallery, PERSONAL false
            .setCopyImagesToPublicGalleryFolder(true)
            .setChooserType(chooserType) //                .setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)
            //                .setFolderName("KESS chat")
            //                 Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
            .setFolderName("EazyTaxi") // Allow multiple picking
            .allowMultiple(isMultiple).build()
    }

    fun startBroadcastData(key: String, jsonModel: String = "") {
        Handler(mainLooper).postDelayed({
            val intent = Intent(MyBroadcastReceiver.customBroadcastKey)
            intent.putExtra(key, true)
            intent.putExtra(MyBroadcastReceiver.dataKey, jsonModel)
            sendBroadcast(intent)
        }, 500)
    }

    fun startBroadcastLocationData(key: String, location: Location?) {
        Handler(mainLooper).postDelayed({
            val intent = Intent(MyBroadcastReceiver.customBroadcastKey)
            intent.putExtra(key, true)
            intent.putExtra(MyBroadcastReceiver.locationDataKey, location)
            sendBroadcast(intent)
        }, 500)
    }

    private fun nextStepAfterHasLocation() {

        val user: User? = getUserUserToSharePreference()
        user?.let {
            //add attendance every single day
            checkingAttendanceUserInParseServer(user)
        }

        //update live user
        updateLiveUserParseServer()

        //save fcm token
        initFcmToken()

        //call if has processing trip
        if (UserDefaultSingleTon.newInstance?.startStopState.toString() == TripEnum.Processing.toString()) {
            startUpdateLocationToParseServer()
        }
    }

    private fun initFcmToken() {
        Parse.checkInit()
        val fcmToken: String = getSharePreference(this, Config.FCM_TOKEN)
        ParseInstallation.getCurrentInstallation().apply {
            deviceToken = fcmToken
        }.saveInBackground()
    }

    protected fun needToCheckPermissionLocation() {

        if (!PermissionHelper.hasDeviceGpsAndNetwork(self())) {
            val messageLocationAlertDialog = Dialog(self())
            messageLocationAlertDialog.setCanceledOnTouchOutside(false)
            messageLocationAlertDialog.setContentView(R.layout.message_location_alert_dialog_layout)
            messageLocationAlertDialog.findViewById<MaterialButton>(R.id.action_setting)
                .setOnClickListener {
                    activityLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        object : BetterActivityResult.OnActivityResult<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult) {
                                messageLocationAlertDialog.dismiss()
                            }
                        })
                }
            messageLocationAlertDialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
                override fun onKey(
                    dialog: DialogInterface?,
                    keyCode: Int,
                    event: KeyEvent?,
                ): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        // Show your Alert Box here
                        return true
                    }
                    return false
                }
            })
            messageLocationAlertDialog.show()
            return
        }

        if (!PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
            PermissionHelper.requestMultiPermission(
                this, arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) { hasPermission ->
                if (hasPermission) {
                    nextStepAfterHasLocation()
                } else {
                    shouldShow = false
                    val messageLocationAlertDialog = Dialog(self())
                    messageLocationAlertDialog.setCanceledOnTouchOutside(false)
                    messageLocationAlertDialog.setContentView(R.layout.message_location_alert_dialog_layout)
                    messageLocationAlertDialog.findViewById<TextView>(R.id.action_setting).text =
                        getString(R.string.open)
                    messageLocationAlertDialog.findViewById<MaterialButton>(R.id.action_setting)
                        .setOnClickListener {
                            shouldShow = true
                            activityLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:$packageName")
                            }, object : BetterActivityResult.OnActivityResult<ActivityResult> {
                                override fun onActivityResult(result: ActivityResult) {
                                    messageLocationAlertDialog.dismiss()
                                }
                            })
                        }
                    messageLocationAlertDialog.setOnKeyListener(object :
                        DialogInterface.OnKeyListener {
                        override fun onKey(
                            dialog: DialogInterface?,
                            keyCode: Int,
                            event: KeyEvent?,
                        ): Boolean {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                // Show your Alert Box here
                                return true
                            }
                            return false
                        }
                    })
                    messageLocationAlertDialog.show()
                }
            }
            return
        }

        nextStepAfterHasLocation()
    }

    fun clearTripSession() {
        UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Nothing
        UserDefaultSingleTon.newInstance?.qrCodeRespond = null
        UserDefaultSingleTon.newInstance?.docKycTemporary?.clear()

        removeLiveDriverInParseServer()
        unSubscribeLocationForeground()
        needToCheckPermissionLocation()

    }

    //user Info
    fun userInfo(user: User): String {
        val userGson = JsonObject()
        userGson.addProperty("avatar_url", user.avatarUrl)
        userGson.addProperty("gender", if (user.gender == 0) "Male" else "Female")
        userGson.addProperty("is_available", user.isAvailable)
        userGson.addProperty("is_employee", user.isEmployee == 1)
        userGson.addProperty("name", user.name)
        userGson.addProperty("phone", user.phone)
        userGson.addProperty("plate_number", user.kycDocument?.plateNumber)
        userGson.addProperty("termID", user.kycDocument?.termId)
        userGson.addProperty("termName", user.kycDocument?.termName)
        return GsonConverterHelper.convertGenericClassToJson(userGson)
    }

    //app Info
    fun appInfo(): String {
        val appInfoGson = JsonObject()
        appInfoGson.addProperty("app_name", BuildConfig.APP_NAME)
        appInfoGson.addProperty(
            "version", String.format(
                "%s (%s)",
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_VERSION,
            )
        )
        return GsonConverterHelper.convertGenericClassToJson(appInfoGson)
    }

    //checking attendance
    private fun checkingAttendanceUserInParseServer(user: User) {
        ParseLiveLocationHelper.newInstance?.let { parseServer ->
            val parseQuery: ParseQuery<ParseObject> = parseServer.checkAttendanceUser(user.id ?: -1)
            parseQuery.findInBackground { objects, exception ->
                if (objects != null) {
                    AppLOGG.d(TAG, "attendance User ${objects.size}")
                    AppLOGG.d(TAG, "exception ${Gson().toJson(exception)}")
                    if (objects.size == 0) {
                        //user
                        val isEmployee = user.isEmployee == 1

                        //date
                        val currentDate =
                            DateTimeHelper.dateFm(Calendar.getInstance().time, "E, dd MMM yyyy")

                        //time
                        val currentTime =
                            DateTimeHelper.dateFm(Calendar.getInstance().time, "hh:mm:ss a")

                        //location
                        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                            if (location != null) {
                                // getting the last known or current location
                                AppLOGG.d(
                                    TAG, "${location.latitude}  ${location.longitude}"
                                )
                                //add attendance user to parse server
                                val attendanceUserParseServer = parseServer.addAttendanceUser(
                                    appInfo(),
                                    user.id ?: -1,
                                    userInfo(user),
                                    isEmployee,
                                    currentDate,
                                    currentTime,
                                    0.0,
                                    location.latitude,
                                    location.longitude
                                )
                                attendanceUserParseServer.saveInBackground {
                                    AppLOGG.d(TAG, "save User ${Gson().toJson(it)}")
                                }
                            }
                        }?.addOnFailureListener {

                        }

                    }
                }
            }
        }
    }

    //update live user
    private fun updateLiveUserParseServer() {

        //fused google map
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10_000 // 10 seconds
        }
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(self())
        fusedLocationProviderClient.requestLocationUpdates(
            request, locationCallback, Looper.getMainLooper()
        )

    }

    //update disable live user in parse server
    fun disableDisplayOnMapLiveUser(displayOnMap: Boolean = false) {
        AppLOGG.d(TAG, "call disable display on map")
        val user: User? = getUserUserToSharePreference()
        ParseLiveLocationHelper.newInstance?.let { parseServer ->
            val parseQuery: ParseQuery<ParseObject> =
                parseServer.checkLiveUserParseServer(user?.id ?: -1)
            parseQuery.findInBackground { objects, exception ->
                if (objects != null && objects.size > 0) {
                    val parseObject = objects.first()
                    parseObject.put("display_on_map", displayOnMap)
                    parseObject.saveInBackground()
                }
            }
        }

    }

    protected fun startUpdateLocationToParseServer() {
        //old code
        /* val locationRequest: LocationRequest = LocationRequest.create().apply {
             interval = TimeUnit.SECONDS.toMillis(10)
             fastestInterval = TimeUnit.SECONDS.toMillis(5)
             priority = LocationRequest.PRIORITY_HIGH_ACCURACY
         }
         try {
             fusedBackgroundLocationProviderClient?.requestLocationUpdates(
                 locationRequest,
                 locationBackgroundUpdatePendingIntent
             )

             Handler(Looper.getMainLooper()).postDelayed(
                 {
                     disableDisplayOnMapLiveUser()
                     unRegisterLocationLiveUser()
                 },
                 1500 // value in milliseconds
             )

         } catch (permissionRevoked: SecurityException) {
             // Exception only occurs if the user revokes the FINE location permission before
             // requestLocationUpdates() is finished executing (very rare).
             Log.d(
                 com.eazy.daiku.ui.map.TAG,
                 "Location permissions revoked; details: $permissionRevoked"
             )
             throw permissionRevoked
         }*/

//        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
//        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
//        this.bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
//

        //new
        foregroundOnlyLocationService?.subscribeToLocationUpdates()
        Handler(Looper.getMainLooper()).postDelayed(
            {
                disableDisplayOnMapLiveUser(/*newUpdate*/true)
                unRegisterLocationLiveUser()
            }, 1500 // value in milliseconds
        )

        //old
//        val enabled = sharedPreferences.getBoolean(
//            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
//        )
//        Log.d("ForegroundOnlyLocationService", "Enable #${enabled}")
//
//        if (enabled) {
//            foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
//        } else {
//            // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
//            foregroundOnlyLocationService?.subscribeToLocationUpdates()
//                ?: Log.d("ForegroundOnlyLocationService", "Service Not Bound")
//            /* if (foregroundOnlyLocationService != null) {
//
//                 Handler(Looper.getMainLooper()).postDelayed(
//                     {
//                         disableDisplayOnMapLiveUser()
//                         unRegisterLocationLiveUser()
//                     },
//                     1500 // value in milliseconds
//                 )
//
//             } else { }*/
//            // foregroundOnlyLocationService?.subscribeToLocationUpdates()
//
//        }

    }

    fun unSubscribeLocationForeground() {
//        fusedBackgroundLocationProviderClient?.removeLocationUpdates(
//            locationBackgroundUpdatePendingIntent
//        )
        foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
        AppLOGG.d(TAG, "unRegisterLocationBackground")
    }

    fun unRegisterLocationLiveUser() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        AppLOGG.d(TAG, "unRegisterLocationLiveUser")
    }

    private fun removeLiveDriverInParseServer() {
        AppLOGG.d(TAG, "removeLiveDriverInParseServer")
        ParseLiveLocationHelper.newInstance?.let { parseServer ->
            val user: User? = getUserUserToSharePreference()
            val parseQuery: ParseQuery<ParseObject> =
                parseServer.checkLiveMapUserParseServer(user?.id ?: -1)
            parseQuery.findInBackground { objects, exception ->
                if (objects != null) {
                    for (liveUser in objects) {
                        liveUser.deleteInBackground()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldShow) {
            needToCheckPermissionLocation()
        }

    }

    // Monitors connection to the while-in-use service.
    protected val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true

        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    /*Foreground service location update with show notification*/
    protected var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    protected var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    protected var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver? = null
    // protected lateinit var sharedPreferences: SharedPreferences

    fun onRegisterBindServiceForegroundConnection() {
        // sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        this.bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)

    }

    fun onRegisterForegroundService() {
        foregroundOnlyBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it, IntentFilter(
                    ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
                )
            )
        }
    }

    fun unRegisterForegroundBroadcastService() {
        foregroundOnlyBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(
                it
            )
        }
    }

    fun removeTrackLocation() {
        if (foregroundOnlyLocationServiceBound) {
            this.unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        // sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

    }

    private fun initSubscribeParseServer() {
        ParseLiveLocationHelper.newInstance?.let { parseServer ->

            val user: User? = getUserUserToSharePreference()
            val mUserId = user?.id ?: -1

            //fetch data when show label when have been accepted before
            parseServer.checkAssignTaxiIsSelf(mUserId, Config.StatusAssignKey.Accepted)
                .findInBackground { objects, exception ->
                    if (objects != null && objects.size > 0) {
                        val bookingInfoParseObj = objects.first()

                        val bookingCode: String = bookingInfoParseObj.get("code").toString()
                        val destinationInfoStr =
                            bookingInfoParseObj.get("destination_info").toString()
                        val destinationInfo =
                            GsonConverterHelper.getJsonObjectToGenericClassValidate<DestinationInfo>(
                                destinationInfoStr
                            )
                        val customerInfoStr = bookingInfoParseObj.get("customer_info").toString()
                        var customerInfo: CustomerInfo? = null
                        try {
                            customerInfo =
                                GsonConverterHelper.getJsonObjectToGenericClassValidate<CustomerInfo>(
                                    customerInfoStr
                                )
                        } catch (ignored: JsonSyntaxException) {
                        }

                        //2. save booking model singleTon class
                        UserDefaultSingleTon.newInstance?.bookingTaxiModel = BookingTaxiModel(
                            bookingCode, customerInfo, destinationInfo, bookingInfoParseObj
                        )
                        //3. update ui in mainScreen
                        startBroadcastData(
                            MyBroadcastReceiver.confirmBookingKey, Config.StatusAssignKey.Accepted
                        )
                    } else {
                        startBroadcastData(
                            MyBroadcastReceiver.confirmBookingKey, Config.StatusAssignKey.Rejected
                        )
                    }
                }

            //checking status assigned first time in home screen to alert show user
            parseServer.checkAssignTaxiIsSelf(mUserId, Config.StatusAssignKey.Assigned)
                .findInBackground { parseObjects, exception ->
                    if (parseObjects != null && parseObjects.size > 0) {

                        val parseObj = parseObjects.first()
                        val bookingCode: String = parseObj.getString("code") ?: ""
                        val customerInfoStr = parseObj.get("customer_info").toString()
                        val customerInfo =
                            GsonConverterHelper.getJsonObjectToGenericClassValidate<CustomerInfo>(
                                customerInfoStr
                            )
                        val destinationInfoStr = parseObj.get("destination_info").toString()
                        val destinationInfo =
                            GsonConverterHelper.getJsonObjectToGenericClassValidate<DestinationInfo>(
                                destinationInfoStr
                            )
                        if (confirmBookingAlertDialog?.dialog?.isShowing == true) {
                            confirmBookingAlertDialog?.dialog?.dismiss()
                        }
                        confirmBookingAlertDialog = ConfirmBookingAlertDialog.newInstance(
                            BookingTaxiModel(
                                bookingCode, customerInfo, destinationInfo
                            )
                        ) { confirmKey, dialog, bookCode, destination ->
                            when (confirmKey) {
                                ConfirmKey.Accepted -> {
                                    //1. update status to parse server
                                    globalLoadingViewEnable(true)
                                    confirmBooking(
                                        Config.StatusAssignKey.Accepted, parseObj
                                    ) { successKey ->
                                        globalLoadingViewEnable(false)
                                        dialog?.dismiss()
                                        if (successKey) {
                                            //2. save booking model singleTon class
                                            UserDefaultSingleTon.newInstance?.bookingTaxiModel =
                                                BookingTaxiModel(
                                                    bookingCode, customerInfo, destination, parseObj
                                                )
                                            //3. update ui in mainScreen
                                            startBroadcastData(
                                                MyBroadcastReceiver.confirmBookingKey,
                                                Config.StatusAssignKey.Accepted
                                            )
                                        }
                                    }

                                }

                                ConfirmKey.Rejected -> {
                                    globalLoadingViewEnable(true)
                                    confirmBooking(
                                        Config.StatusAssignKey.Rejected, parseObj
                                    ) { successKey ->
                                        globalLoadingViewEnable(false)
                                        dialog?.dismiss()
                                        if (successKey) {
                                            startBroadcastData(
                                                MyBroadcastReceiver.confirmBookingKey,
                                                Config.StatusAssignKey.Rejected
                                            )
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                        confirmBookingAlertDialog?.show(
                            this.supportFragmentManager, "ConfirmBookingAlertDialog"
                        )
                    }
                }

            //observer when parse has update live data
            assignedFilterBookingTaxi(parseServer)

            // assignedFilterGlobalBookingTaxi(parseServer)

            //subscribe live map when user stop moving location
            assignedStopMovingClass(parseServer)

        }
    }

    private fun assignedStopMovingClass(parseServer: ParseLiveLocationHelper) {
        val user: User? = getUserUserToSharePreference()
        val mUserId = user?.id ?: -1
        val subscribeLiveMapHandling = ParseLiveQueryClient.Factory.getClient().subscribe(
            parseServer.checkLiveMapUserParseServer(mUserId)
        )
        subscribeLiveMapHandling.handleSubscribe {
            //update
            subscribeLiveMapHandling.handleEvent(
                SubscriptionHandling.Event.UPDATE
            ) { _: ParseQuery<ParseObject?>?, parseObject: ParseObject? ->
                parseObject?.let { parseObj ->
                    val stopMoving = parseObj.getBoolean("stop_moving")
                    AppLOGG.d("subscriptionHandling", "call here $stopMoving")
                    if (!stopMoving) {
                        removeHandler = false
                        handler?.postDelayed(runnableCode, delayTimer)//60000milli = 1minute
                    }
                }
            }
        }
    }

    private fun assignedFilterBookingTaxi(parseServer: ParseLiveLocationHelper) {
        val user: User? = getUserUserToSharePreference()
        val mUserId = user?.id ?: -1
        val subscribe = ParseLiveQueryClient.Factory.getClient().subscribe(
            parseServer.checkAssignTaxiIsSelf()
        )
        subscribe.handleSubscribe {
            //update
            subscribe.handleEvent(
                SubscriptionHandling.Event.UPDATE
            ) { _: ParseQuery<ParseObject?>?, parseObject: ParseObject? ->
                AppLOGG.d(
                    "subscriptionHandling",
                    "UPDATE out -> ${parseObject} id-> ${mUserId} -> ${parseObject?.get("status") ?: ""}"
                )
                parseObject?.let { parseObj ->
                    if (parseObj.getInt("assign_to") == mUserId) {
                        when (parseObj.get("status") ?: "") {
                            Config.StatusAssignKey.Assigned -> {
                                val bookingCode: String = parseObj.getString("code") ?: ""
                                val customerInfoStr = parseObj.get("customer_info").toString()
                                val customerInfo =
                                    GsonConverterHelper.getJsonObjectToGenericClassValidate<CustomerInfo>(
                                        customerInfoStr
                                    )
                                val destinationInfoStr = parseObj.get("destination_info").toString()
                                val destinationInfo =
                                    GsonConverterHelper.getJsonObjectToGenericClassValidate<DestinationInfo>(
                                        destinationInfoStr
                                    )

                                val backgroundMode =
                                    mEazyTaxiApplication?.getBackgroundMode() ?: false
                                if (backgroundMode) {
                                    UserDefaultSingleTon.newInstance?.bookingTaxiTemporaryObject =
                                        BookingTaxiModel(
                                            bookingCode, customerInfo, destinationInfo, parseObj
                                        )
                                } else {
                                    UserDefaultSingleTon.newInstance?.bookingTaxiTemporaryObject =
                                        null
                                    if (confirmBookingAlertDialog?.dialog?.isShowing == true) {
                                        confirmBookingAlertDialog?.dialog?.dismiss()
                                    }
                                    confirmBookingAlertDialog =
                                        ConfirmBookingAlertDialog.newInstance(
                                            BookingTaxiModel(
                                                bookingCode, customerInfo, destinationInfo, parseObj
                                            )
                                        ) { confirmKey, dialog, bookCode, destination ->
                                            when (confirmKey) {
                                                ConfirmKey.Accepted -> {
                                                    //1. update status to parse server
                                                    globalLoadingViewEnable(true)
                                                    confirmBooking(
                                                        Config.StatusAssignKey.Accepted, parseObj
                                                    ) { successKey ->
                                                        globalLoadingViewEnable(false)
                                                        dialog?.dismiss()
                                                        if (successKey) {
                                                            //2. save booking model singleTon class
                                                            UserDefaultSingleTon.newInstance?.bookingTaxiModel =
                                                                BookingTaxiModel(
                                                                    bookingCode,
                                                                    customerInfo,
                                                                    destination,
                                                                    parseObj
                                                                )
                                                            //3. update ui in mainScreen
                                                            startBroadcastData(
                                                                MyBroadcastReceiver.confirmBookingKey,
                                                                Config.StatusAssignKey.Accepted
                                                            )
                                                        }
                                                    }
                                                }

                                                ConfirmKey.Rejected -> {
                                                    globalLoadingViewEnable(true)
                                                    confirmBooking(
                                                        Config.StatusAssignKey.Rejected, parseObj
                                                    ) { successKey ->
                                                        globalLoadingViewEnable(false)
                                                        if (successKey) {
                                                            dialog?.dismiss()
                                                            startBroadcastData(
                                                                MyBroadcastReceiver.confirmBookingKey,
                                                                Config.StatusAssignKey.Rejected
                                                            )
                                                        }
                                                    }

                                                }

                                                else -> {}
                                            }
                                        }
                                    confirmBookingAlertDialog?.show(
                                        this.supportFragmentManager, "ConfirmBookingAlertDialog"
                                    )
                                }
                            }

                            Config.StatusAssignKey.NoResponse -> {
                                UserDefaultSingleTon.newInstance?.bookingTaxiTemporaryObject = null
                                UserDefaultSingleTon.newInstance?.bookingTaxiModel = null
                                if (confirmBookingAlertDialog?.dialog?.isShowing == true) {
                                    confirmBookingAlertDialog?.dialog?.dismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun subscribeTaxiBooking() {
        if (confirmBookingAlertDialog?.dialog?.isShowing == true) {
            confirmBookingAlertDialog?.dialog?.dismiss()
        }
        UserDefaultSingleTon.newInstance?.bookingTaxiTemporaryObject?.let { bookingTaxi ->
            confirmBookingAlertDialog = ConfirmBookingAlertDialog.newInstance(
                bookingTaxi
            ) { confirmKey, dialog, bookCode, destination ->
                when (confirmKey) {
                    ConfirmKey.Accepted -> {
                        //1. update status to parse server
                        globalLoadingViewEnable(true)
                        confirmBooking(
                            Config.StatusAssignKey.Accepted, bookingTaxi.bookingParseObj
                        ) { successKey ->
                            globalLoadingViewEnable(false)
                            dialog?.dismiss()
                            if (successKey) {
                                //2. save booking model singleTon class
                                UserDefaultSingleTon.newInstance?.bookingTaxiModel = bookingTaxi
                                //3. update ui in mainScreen
                                startBroadcastData(
                                    MyBroadcastReceiver.confirmBookingKey,
                                    Config.StatusAssignKey.Accepted
                                )
                            }
                        }

                    }

                    ConfirmKey.Rejected -> {
                        globalLoadingViewEnable(true)
                        confirmBooking(
                            Config.StatusAssignKey.Rejected, bookingTaxi.bookingParseObj
                        ) { successKey ->
                            globalLoadingViewEnable(false)
                            dialog?.dismiss()
                            if (successKey) {
                                startBroadcastData(
                                    MyBroadcastReceiver.confirmBookingKey,
                                    Config.StatusAssignKey.Rejected
                                )
                            }
                        }

                    }

                    else -> {}
                }
            }
            confirmBookingAlertDialog?.show(
                this.supportFragmentManager, "ConfirmBookingAlertDialog"
            )

            UserDefaultSingleTon.newInstance?.bookingTaxiTemporaryObject = null
        }
    }


    fun subscribeTaxiBookingByCode(code: String) {
        // MessageUtils.showError(this, "", "Test")


        ParseLiveLocationHelper.newInstance?.let { parseServer ->
            val user: User? = getUserUserToSharePreference()
            val mUserId = user?.id ?: -1

            parseServer.checkCodeTaxiIsSelf(
                mUserId, code
            ).findInBackground { parseObjects, exception ->
                if (parseObjects != null && parseObjects.size > 0) {

                    val parseObj = parseObjects.first()
                    val bookingCode: String = parseObj.getString("code") ?: ""
                    val customerInfoStr = parseObj.get("customer_info").toString()
                    val customerInfo =
                        GsonConverterHelper.getJsonObjectToGenericClassValidate<CustomerInfo>(
                            customerInfoStr
                        )
                    val destinationInfoStr = parseObj.get("destination_info").toString()
                    val destinationInfo =
                        GsonConverterHelper.getJsonObjectToGenericClassValidate<DestinationInfo>(
                            destinationInfoStr
                        )
                    if (confirmBookingAlertDialog?.dialog?.isShowing == true) {
                        confirmBookingAlertDialog?.dialog?.dismiss()
                    }
                    AppLOGG.d("confirmBookingAlertDialogShow", "isShow")
                    confirmBookingAlertDialog = ConfirmBookingAlertDialog.newInstance(
                        BookingTaxiModel(
                            bookingCode, customerInfo, destinationInfo
                        )
                    ) { confirmKey, dialog, bookCode, destination ->
                        when (confirmKey) {
                            ConfirmKey.Accepted -> {
                                //1. update status to parse server
                                globalLoadingViewEnable(true)
                                confirmBooking(
                                    Config.StatusAssignKey.Accepted, parseObj
                                ) { successKey ->
                                    globalLoadingViewEnable(false)
                                    dialog?.dismiss()
                                    if (successKey) {
                                        ParseLiveLocationHelper.newInstance?.liveUserUpdateBookingCode(
                                            this,
                                            bookingCode
                                        )
                                        //2. save booking model singleTon class
                                        UserDefaultSingleTon.newInstance?.bookingTaxiModel =
                                            BookingTaxiModel(
                                                bookingCode, customerInfo, destination, parseObj
                                            )
                                        //3. update ui in mainScreen
                                        startBroadcastData(
                                            MyBroadcastReceiver.confirmBookingKey,
                                            Config.StatusAssignKey.Accepted
                                        )
                                    }
                                }

                            }

                            ConfirmKey.Rejected -> {
                                globalLoadingViewEnable(true)
                                confirmBooking(
                                    Config.StatusAssignKey.Rejected, parseObj
                                ) { successKey ->
                                    globalLoadingViewEnable(false)
                                    dialog?.dismiss()
                                    if (successKey) {
                                        startBroadcastData(
                                            MyBroadcastReceiver.confirmBookingKey,
                                            Config.StatusAssignKey.Rejected
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    showBookingDialog()
                    /*confirmBookingAlertDialog?.show(
                        this.supportFragmentManager, "ConfirmBookingAlertDialog"
                    )*/
                }
            }

        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        mIsStateAlreadySaved = false
        if (mPendingShowDialog) {
            mPendingShowDialog = false
            // showBookingDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        mIsStateAlreadySaved = true
    }


    private fun showBookingDialog() {
        if (mIsStateAlreadySaved) {
            mPendingShowDialog = true
        } else {
            confirmBookingAlertDialog?.show(
                this.supportFragmentManager,
                "ConfirmBookingAlertDialog"
            )
        }
    }


    protected fun confirmBooking(
        status: String, parseObject: ParseObject?, completedUpdated: (Boolean) -> Unit
    ) {
        parseObject?.put("status", status)
        parseObject?.saveInBackground { e ->
            AppLOGG.d("submitLocationParseServer", "work outside parseObject ${e == null}")
            if (e == null) {
                completedUpdated.invoke(true)
            }
        }

    }

    /*protected fun confirmBooking(
        status: String,
        completedUpdated: (Boolean) -> Unit
    ) {
         ParseLiveLocationHelper
             .newInstance?.let { parseServer ->
                 val user: User? = getUserUserToSharePreference()
                 val mUserId = user?.id ?: -1
                 val parseQuery: ParseQuery<ParseObject> =
                     parseServer.checkAssignTaxiIsSelf(mUserId)
                 parseQuery.findInBackground { objects, exception ->
                     if (objects != null && objects.size > 0) {
                         var index = 0
                         for (assigned in objects) {
                             if (index == 0) {
                                 index++
                                 continue
                             }
                             assigned.put("status", Config.StatusAssignKey.Cancelled)
                             assigned.saveInBackground()
                         }
                         val parseObject = objects.first()
                         parseObject.put("status", status)
                         parseObject.saveInBackground { e ->
                             AppLOGG.d("submitLocationParseServer", "work outside ${e == null}")
                             if (e == null) {
                                 completedUpdated.invoke(true)
                             }
                         }

                     }
                 }
             }
    }*/

    protected fun stopMovingLocationParseServer(status: Boolean) {
        ParseLiveLocationHelper.newInstance?.let { parseServer ->
            val user: User? = getUserUserToSharePreference()
            val parseQuery: ParseQuery<ParseObject> =
                parseServer.checkLiveMapUserParseServer(user?.id ?: -1)
            parseQuery.findInBackground { parseObject, exception ->
                parseObject?.forEach {
                    it.put("stop_moving", status)
                    it.saveInBackground()
                }
            }
        }
    }

    fun checkingStopMovingDriver(locationData: Location?) {
        handler = mEazyTaxiApplication?.timerHandler
        lastLocation = locationData
        if (startLocationFlag) {
            AppLOGG.d(
                "submitLocationParseServer_", "call "
            )
            lastLocation?.let { location ->
                AppLOGG.d(
                    "submitLocationParseServer_",
                    "${location.latitude} - ${location.longitude} - flag ${startLocationFlag}"
                )
            }
        }

        lastLocation?.let { location ->
            if (location.latitude > 0 || location.longitude > 0) {
                if (startLocationFlag) {
                    handler?.postDelayed(runnableCode, delayTimer)
                    startLocationFlag = false
                }
            }
        }

//        startBroadcastLocationData(
//            MyBroadcastReceiver.stopMovingLocationDriverKey,
//            location
//        )
    }

    fun globalLoadingViewEnable(showLoading: Boolean) {
        mEazyTaxiApplication?.globalLoadingView(showLoading)
    }

    private fun removeLiveTripHandler() {
        AppLOGG.d(
            "submitLocationParseServer_", "handler $handler"
        )
        handler?.removeCallbacks(runnableCode)
        removeHandler = true
        locationsTemp.clear()
    }

    private val runnableCode = object : Runnable {
        override fun run() {
            val distanceMeter: Int = 50
            val fiveMinute = 5
            AppLOGG.d(
                "submitLocationParseServer_", "timer working ${locationsTemp.size}"
            )
            if (locationsTemp.size > fiveMinute) {
                val lastLoc = locationsTemp[locationsTemp.size - fiveMinute]
                val distance = lastLoc.distanceTo(lastLocation)
                AppLOGG.d(
                    "submitLocationParseServer_",
                    "lat: ${lastLoc.latitude} # lng: ${lastLoc.longitude} -> distance ${distance}m -> ${distance < distanceMeter}"
                )
                if (distance <= distanceMeter) /*50meter*/ {
                    stopMovingLocationParseServer(true)
                    removeLiveTripHandler()
                    AppLOGG.d(
                        "submitLocationParseServer_", "Remove success"
                    )
                }
            }
            lastLocation?.let { location ->
                if (location.latitude > 0 || location.longitude > 0) {
                    AppLOGG.d(
                        "submitLocationParseServer_",
                        "add location in Lists lat: ${location.latitude} # lng: ${location.longitude}"
                    )
                    locationsTemp.add(location)
                }
            }
            if (!removeHandler) {
                handler?.postDelayed(this, delayTimer)//60000milli = 1minute
            }
        }
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            val lat = p0.lastLocation.latitude
            val lng = p0.lastLocation.longitude
            val gpsHeading = p0.lastLocation.bearing

            //for wego driver has trip
            val notProcessingTrip =
                UserDefaultSingleTon.newInstance?.startStopState.toString() != TripEnum.Processing.toString()
            if (notProcessingTrip) {
                disableDisplayOnMapLiveUser(true)
                ParseLiveLocationHelper.newInstance?.let { parseServer ->

                    val user: User? = getUserUserToSharePreference()
                    var userInfoStr = ""
                    user?.let {
                        userInfoStr = userInfo(it)
                    }

                    val parseQuery: ParseQuery<ParseObject> =
                        parseServer.checkLiveUserParseServer(user?.id ?: -1)
                    parseQuery.findInBackground { objects, exception ->

                        //user
                        val isEmployee = user?.isEmployee == 1

                        //date
                        val currentDate = DateTimeHelper.dateFm(
                            Calendar.getInstance().time, "E, dd MMM yyyy"
                        )

                        //time
                        val currentTime = DateTimeHelper.dateFm(
                            Calendar.getInstance().time, "hh:mm:ss a"
                        )

                        //update
                        if (objects != null && objects.size > 0) {
                            //to delete put only one
                            var index = 0
                            for (liveUser in objects) {
                                if (index == 0) {
                                    index++
                                    continue
                                }
                                liveUser.deleteInBackground()
                            }

                            val parseObject = objects.first()
                            parseServer.liveUserParseServer(
                                parseObject,
                                appInfo(),
                                user?.id ?: -1,
                                userInfoStr,
                                isEmployee,
                                currentDate,
                                currentTime,
                                gpsHeading,
                                lat,
                                lng,
                                true
                            ).saveInBackground()

                        } else {
                            //add liveUser user to parse server
                            val liveUserUserParseServer = parseServer.liveUserParseServer(
                                appInfo(),
                                user?.id ?: -1,
                                userInfoStr,
                                isEmployee,
                                currentDate,
                                currentTime,
                                gpsHeading,
                                lat,
                                lng,
                                true
                            )
                            liveUserUserParseServer.saveInBackground {
                                AppLOGG.d(TAG, "save live User ${Gson().toJson(it)}")
                            }
                        }
                    }
                }
            } else {
                disableDisplayOnMapLiveUser(/*false*/true)
            }

            //for global lat and long location
            saveCurrentGpsUser(lat, lng)

            AppLOGG.d(
                TAG,
                "live user lastLocation  ${p0.lastLocation.latitude} # " + "${p0.lastLocation.longitude} -> $notProcessingTrip"
            )

        }
    }
    private val myBroadcastReceiver = object : MyBroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            super.onReceive(p0, p1)
            if (p1 != null) {
                when {
                    p1.hasExtra(notificationAcceptOrder) -> {
                        val code: String? = p1.getStringExtra(notificationAcceptOrder)
                        AppLOGG.d("TAXIBOOKINGCODE", "--> " + code)
                        if (code != null) {
                            subscribeTaxiBookingByCode(code)
                        }
                    }
                }
            }
        }

    }
}

