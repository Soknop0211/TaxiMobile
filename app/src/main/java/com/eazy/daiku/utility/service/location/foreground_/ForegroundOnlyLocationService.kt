package com.eazy.daiku.utility.service.location.foreground_

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.ui.MainActivity
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.SharedPreferenceUtil
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.google.android.gms.location.*
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*


/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
class ForegroundOnlyLocationService : Service() {

    /*
      * Checks whether the bound activity has really gone away (foreground service with notification
      * created) or simply orientation change (no-op).
      */
    private var configurationChange = false
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()

    private var appServer = Config.UserDeviceInfo.appServer
    private var appType = Config.UserDeviceInfo.appType
    private var deviceType = Config.UserDeviceInfo.deviceType

    private lateinit var notificationManager: NotificationManager

    // TODO: Step 1.1, Review variables (no changes).
    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate()")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // TODO: Step 1.2, Review the FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // TODO: Step 1.3, Create a LocationRequest.
        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = 10000//TimeUnit.SECONDS.toMillis(60)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            //fastestInterval = TimeUnit.SECONDS.toMillis(30)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            //maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // TODO: Step 1.4, Initialize the LocationCallback.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                currentLocation = locationResult.lastLocation

                // Notify our Activity that a new location was added. Again, if this was a
                // production app, the Activity would be listening for changes to a database
                // with new locations, but we are simplifying things a bit to focus on just
                // learning the location side of things.
                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                // Updates notification content if this service is running as a foreground
                // service.
                if (serviceRunningInForeground) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(currentLocation)
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        Log.d(TAG, "onStartCommand() #${cancelLocationTrackingFromNotification}")
        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d(TAG, "Start foreground service")
            val notification = generateNotification(currentLocation)
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates() ")

        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))

        try {
            // TODO: Step 1.5, Subscribe to location changes.
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")
        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateNotification(location: Location?): Notification {
        AppLOGG.d(
            "ForegroundOnlyLocationService",
            "generateNotification() ${location?.latitude}  ${location?.longitude} ## ${applicationContext}"
        )

        applicationContext?.let { context ->
            ParseLiveLocationHelper
                .newInstance?.let { parseServer ->
                    parseServer.submitLocationParseServer(context, location)
                }
        }

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data

        val title =
            UserDefaultSingleTon.newInstance?.qrCodeRespond?.title ?: "-"
        val totalAmount = UserDefaultSingleTon.newInstance?.qrCodeRespond?.total_amount ?: 0.00
        val distant =
            UserDefaultSingleTon.newInstance?.qrCodeRespond?.distance ?: 0

        //location?.toText()?: getString(com.eazy.daiku.R.string.no_location_text)
        val mainNotificationText =
            String.format(
                "%sKm • $%s",
                distant,
                totalAmount
            )
        val titleText = title

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Trip_Notification", NotificationManager.IMPORTANCE_LOW
            )

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
//        val launchActivityIntent = Intent(this, MainActivity::class.java)
//
//        val cancelIntent = Intent(this, ForegroundOnlyLocationService::class.java)
//        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)
//
//        val servicePendingIntent = PendingIntent.getService(
//            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        val activityPendingIntent = PendingIntent.getActivity(
//            this, 0, launchActivityIntent, 0
//        )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.drawable.ic_navigation_24)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true) // force cancel in notification bar
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(LongArray(1) { 0L })
            .build()
//            .addAction(
//                R.drawable.ic_navigation_24, "Launch activity",
//                activityPendingIntent
//            )

    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService
    }

    companion object {
        private const val TAG = "ForegroundOnlyLocationService"

        private const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
    }

    private fun uploadToParseServer(context: Context, location: Location?) {
        val lat = location?.latitude ?: 0.00
        val lng = location?.longitude ?: 0.00
        val gpsHeading: Float = location?.bearing ?: 0f
        var tripJson = ""
        var bookingCode = ""
        val qrCodeRespond = UserDefaultSingleTon.newInstance?.qrCodeRespond
        qrCodeRespond?.let {
            bookingCode = it.code ?: ""
            tripJson = GsonConverterHelper.convertGenericClassToJson(it)
        }

        ParseLiveLocationHelper
            .newInstance?.let { parseServer ->
                val user: User? = parseServer.getUser(context)
                val userId: Int = user?.id ?: -1

                var userInfoStr = ""
                user?.let {
                    userInfoStr = parseServer.userInfo(it)
                }

                val parseQuery: ParseQuery<ParseObject> =
                    parseServer.checkLiveMapUserParseServer(user?.id ?: -1)
                parseQuery.findInBackground { objects, exception ->

                    //date
                    val currentDate =
                        DateTimeHelper.dateFm(
                            Calendar.getInstance().time,
                            "E, dd MMM yyyy"
                        )
                    //time
                    val currentTime =
                        DateTimeHelper.dateFm(
                            Calendar.getInstance().time,
                            "hh:mm:ss a"
                        )

                    //device info


                    //to delete put only one
                    if (objects != null && objects.size > 0) {
                        var index = 0
                        for (liveUser in objects) {
                            if (index == 0) {
                                index++
                                continue
                            }
                            liveUser.deleteInBackground()
                        }

                        //update
                        val parseObject = objects.first()
                        parseObject.put("user_info", userInfoStr)
                        parseObject.put("last_updated_date", currentDate)
                        parseObject.put("last_updated_time", currentTime)
                        parseObject.put("lat", lat)
                        parseObject.put("long", lng)

                        //device
                        parseObject.put("app_server", appServer)
                        parseObject.put("app_type", appType)
                        parseObject.put("app_info", parseServer.appInfo())
                        parseObject.put("device_info", parseServer.deviceInfo())
                        parseObject.put("device_type", deviceType)
                        parseObject.put("gps_heading", gpsHeading)

                        //user
                        parseObject.put("user_id", userId)
                        parseObject.put("user_info", userInfoStr)
                        parseObject.put("last_updated_date", currentDate)
                        parseObject.put("last_updated_time", currentTime)

                        //location
                        parseObject.put("lat", lat)
                        parseObject.put("long", lng)

                        parseObject.saveInBackground()

                    } else {
                        //add new
                        val liveMapUserParseServer =
                            parseServer.liveMapUserParseServer(
                                parseServer.appInfo(),
                                userId,
                                userInfoStr,
                                currentDate,
                                currentTime,
                                gpsHeading,
                                lat,
                                lng,
                                bookingCode,
                                currentDate,
                                currentTime,
                                tripJson
                            )
                        liveMapUserParseServer.saveInBackground {

                        }
                    }
                }

            }

    }

}