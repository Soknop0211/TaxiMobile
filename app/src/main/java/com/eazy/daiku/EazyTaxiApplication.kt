package com.eazy.daiku


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AndroidRuntimeException
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.di.DaggerAppComponent
import com.eazy.daiku.ui.MainActivity
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.firmareOS
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.other.LocaleManager
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.parse.Parse
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector

const val TAG = "EazyTaxiApplication"

class EazyTaxiApplication :
    DaggerApplication(),
    HasAndroidInjector,
    LifecycleObserver {

    private val applicationInjector = DaggerAppComponent.builder().application(this).build()
    private var backgroundMode: Boolean? = false
    private var globalDialog: Dialog? = null

    var mCurrentMainActivity: MainActivity? = null
    var mCurrentBaseActivity: BaseActivity? = null
    var timerHandler: Handler? = null

    @SuppressLint("HardwareIds")
    fun deviceId(): String? {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = applicationInjector

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let {
            LocaleManager.setLocale(it)
        })
        MultiDex.install(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setLocale(this)
    }

    override fun onCreate() {
        super.onCreate()
        LocaleManager.setLocale(this)

        //init config parse server key
        parseServerConfig()

        //life cycle of all activity
        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(this)

        timerHandler = Handler(Looper.getMainLooper())
    }

    private fun parseServerConfig() {
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("eazy_taxi") // if desired
                .clientKey("eazy_taxi_client")
                .server("https://app.daikou.asia/parse")/*http://178.128.96.141:1337/parse*/
                .build()
        )
    }

    fun setCurrentMainActivity(mCurrentActivity: MainActivity) {
        this.mCurrentMainActivity = mCurrentActivity
    }

    fun setCurrentBaseActivity(mCurrentActivity: BaseActivity) {
        this.mCurrentBaseActivity = mCurrentActivity
    }

    fun getBackgroundMode(): Boolean? {
        return backgroundMode
    }

    fun detectCrashApp() {
        val uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t: Thread?, e: Throwable? ->
            val errorMsg = firmareOS.errorMsg(e!!)
            EazyTaxiHelper.setSharePreference(
                applicationContext,
                Config.LOG_CRASH_MSG,
                errorMsg
            )

            if (e is AndroidRuntimeException /*&& e is a specific exception that needs treatment*/) {
                Looper.loop() // Revive main looper
            } else uncaughtExceptionHandler?.uncaughtException(t, e)
        }
    }

    fun globalLoadingView(show: Boolean) {
        mCurrentBaseActivity?.let { activity ->
            if (globalDialog == null) {
                globalDialog = Dialog(activity)
            }
            globalDialog?.setContentView(R.layout.loading_layout)
            globalDialog?.setCanceledOnTouchOutside(false)
            globalDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (show && !activity.isFinishing) {
                globalDialog?.create()
                globalDialog?.show()
            } else {
                globalDialog?.dismiss()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        AppLOGG.d("ForegroundOnlyLocationService", "APP CONTROL - START ${mCurrentMainActivity}")
        mCurrentMainActivity?.let { activity ->
            if (hasProcessingTrip() && hasNetworkGps(activity)) {
                activity.onRegisterBindServiceForegroundConnection()
            }
        }
//        onRegisterBindServiceForegroundConnection()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResumeMoveToForeground() {
        AppLOGG.d("ForegroundOnlyLocationService", "APP CONTROL - RESUME ${mCurrentMainActivity}")
        mCurrentMainActivity?.let { activity ->
            if (hasProcessingTrip() && hasNetworkGps(activity)) {
                activity.onRegisterForegroundService()
            }
        }
        backgroundMode = false
        mCurrentBaseActivity?.let { activity ->
            val hasNotTrip = !(hasProcessingTrip() && hasNetworkGps(activity))
            if (hasNotTrip) {
                activity.subscribeTaxiBooking()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPauseMoveToForeground() {
        backgroundMode = true
        AppLOGG.d("ForegroundOnlyLocationService", "APP CONTROL - PAUSE ${mCurrentMainActivity}")
        mCurrentMainActivity?.let { activity ->

            if (hasProcessingTrip() && hasNetworkGps(activity)) {
                activity.unRegisterForegroundBroadcastService()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        AppLOGG.d("ForegroundOnlyLocationService", "APP CONTROL - STOP ${mCurrentMainActivity}")
        mCurrentMainActivity?.let { activity ->
            if (hasProcessingTrip() && hasNetworkGps(activity)) {
                activity.removeTrackLocation()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onMoveToTerminateApp() {
        AppLOGG.d("ForegroundOnlyLocationService", "APP CONTROL - ON_DESTROY")
        UserDefaultSingleTon.newInstance?.globalBaseCoreActivity = null
        mCurrentMainActivity?.let { activity ->
            if (activity is BaseActivity) {
                if (hasProcessingTrip() && hasNetworkGps(activity)) {
                    activity.unSubscribeLocationForeground()
                }
            }
        }
    }

    //checking processing trip
    private fun hasProcessingTrip(): Boolean {
        return UserDefaultSingleTon.newInstance?.startStopState.toString() ==
                TripEnum.Processing.toString()
    }

    //checking gps and location permission
    private fun hasNetworkGps(activity: Activity): Boolean {
        return PermissionHelper.hasDeviceGpsAndNetwork(activity) &&
                PermissionHelper.hasCOARSEAndFINELocationPermission(activity)
    }

}