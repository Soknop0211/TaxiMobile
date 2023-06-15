package com.eazy.daiku.utility.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.eazy.daiku.EazyTaxiApplication
import com.eazy.daiku.R
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.ui.splash_screen.SplashScreenActivity
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.other.LocaleManager
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import com.parse.ParseObject
import com.parse.ParseQuery
import dagger.android.support.DaggerAppCompatActivity
import pl.aprilapps.easyphotopicker.EasyImage
import javax.inject.Inject

open class BaseCoreActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    val activityLauncher: BetterActivityResult<Intent, ActivityResult> =
        BetterActivityResult.registerActivityForResult(this)
    val activityPermission: BetterActivityResult<String, Boolean> =
        BetterActivityResult.registerForActivityResult(
            this,
            ActivityResultContracts.RequestPermission()
        )
    val activityMultiPermission: BetterActivityResult<Array<String>, Map<String, Boolean>> =
        BetterActivityResult.registerForActivityResult(
            this,
            ActivityResultContracts.RequestMultiplePermissions()
        )
    val activityOpenMultiDocuments: BetterActivityResult<Array<String>, List<Uri>> =
        BetterActivityResult.registerForActivityResult(
            this,
            ActivityResultContracts.OpenMultipleDocuments()
        )
    val activityOpenDocument: BetterActivityResult<Array<String>, Uri> =
        BetterActivityResult.registerForActivityResult(this, ActivityResultContracts.OpenDocument())
    var easyImageCallbacks: EasyImage.Callbacks? = null

    private var globalCurrentCoreActivity: BaseCoreActivity? = null

    fun <T : BaseCoreActivity?> self(): T {
        return this as T
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(base!!))
    }

    override fun onResume() {
        UserDefaultSingleTon.newInstance?.globalBaseCoreActivity = this
        super.onResume()
    }

    fun getGlobalCurrentCoreActivity(): BaseCoreActivity? {
        return globalCurrentCoreActivity
    }

    fun globalShowError(text: String) {
        MessageUtils.showError(
            self(),
            "",
            "$text"
        )
    }

    fun globalRequestDeviceGps() {
        MessageUtils.showConfirm(
            self(),
            "",
            getString(R.string.gps_network_not_enabled),
            "Cancel",
            "Setting"
        ) { dialog ->
            //confirm click
            dialog.dismiss()
            activityLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            activityLauncher.launch(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            )
        }
    }

    fun hasInternetConnection(): Boolean {
        return if (EazyTaxiHelper.hasNotNetworkAvailable(self())) {
            globalShowError(getString(R.string.no_connection_hint))
            false
        } else {
            true
        }
    }

    fun globalShowSuccessWithDismiss(
        context: Context,
        text: String,
        onDismiss: (Boolean) -> Unit
    ) {
        MessageUtils.showSuccessDismiss(
            self(),
            "",
            text
        ) {
            it.dismiss()
            onDismiss.invoke(true)
        }
    }

    fun setNewLocale(mContext: BaseCoreActivity, @LocaleManager.LocaleDef language: String) {
        LocaleManager.setNewLocale(mContext, language)
        val intent = Intent(mContext, SplashScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        activityLauncher.launch(intent)
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    fun saveUserToSharePreference(user: User) {
        user.phone?.let {
            savePhoneNumberUser(it)
        }
        UserDefaultSingleTon.newInstance?.user = user
        val userDetailGsonStr = Gson().toJson(user)
        EazyTaxiHelper.setSharePreference(this, Constants.userDetailKey, userDetailGsonStr)
    }

    private fun savePhoneNumberUser(phoneNumber: String) {
        EazyTaxiHelper.setSharePreference(this, Constants.phoneNumberKey, phoneNumber)
    }
    fun savePassWordUser(passWord: String) {
        EazyTaxiHelper.setSharePreference(this, Constants.passWordKey, passWord)
    }



    fun saveCurrentGpsUser(lat: Double, lng: Double) {

        UserDefaultSingleTon.newInstance?.currentLatitudeUser = null
        UserDefaultSingleTon.newInstance?.currentLngtitudeUser = null

        UserDefaultSingleTon.newInstance?.currentLatitudeUser =
            "$lat"
        UserDefaultSingleTon.newInstance?.currentLngtitudeUser =
            "$lng"
    }

    fun getUserUserToSharePreference(): User? {
        val jsonStr = EazyTaxiHelper.getSharePreference(self(), Constants.userDetailKey)
        if (jsonStr.isEmpty()) {
            return null
        }
        return GsonConverterHelper.getJsonObjectToGenericClass(jsonStr)
    }

    fun getPassWordUser(): String? {
        val jsonStr = EazyTaxiHelper.getSharePreference(self(), Constants.passWordKey)
        if (jsonStr.isEmpty()){
            return null
        }
        return  jsonStr
    }

    fun hasUserInSharePreference(): String {
        return EazyTaxiHelper.getSharePreference(self(), Constants.userDetailKey)
    }

    fun saveBiometric(saveBiometric: String) {
        EazyTaxiHelper.setSharePreference(this, Constants.biometricKey, saveBiometric)
    }

    fun getBiometric(): String? {
        val data = EazyTaxiHelper.getSharePreference(this, Constants.biometricKey)
        return data.ifEmpty { null }
    }

    fun convertAsBitmap(
        imageBitmap: ImageView,
        image: String,
    ) {
        Glide
            .with(this)
            .asBitmap()
            .load(image)
            .into(imageBitmap)
    }

}