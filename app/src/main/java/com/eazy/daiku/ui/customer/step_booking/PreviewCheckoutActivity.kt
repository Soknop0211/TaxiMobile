package com.eazy.daiku.ui.customer.step_booking

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityPreviewCheckoutBinding
import com.eazy.daiku.ui.customer.model.PreviewCheckoutModel
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.service.MyBroadcastReceiver

class PreviewCheckoutActivity : BaseActivity() {
    private lateinit var binding: ActivityPreviewCheckoutBinding
    private var previewCheckoutModel: PreviewCheckoutModel? = null
    private var carId: String? = null
    private var deviceId: String? = null
    private var pathMapScreenShot: String? = null
    private var adminArea: String? = null
    private var countryName: String? = null
    private var getAddressLine: String? = null
    private var lattitude: String? = null
    private var longitude: String? = null
    private var vechicle: String? = null
    private val intentFilter = IntentFilter()

    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    companion object {
        const val dataJsonPreviewCheckoutKey = "dataJsonPreviewCheckoutKey"
        const val carIdKey = "carIdKey"
        const val deviceIdKey = "deviceIdKey"
        const val pathMapScreenShotKey = "pathMapScreenShotKey"
        const val adminAreaKey = "adminAreaKey"
        const val countryNameKey = "countryNameKey"
        const val getAddressLineKey = "getAddressLineKey"
        const val lattitudeKey = "lattitudeKey"
        const val longtitudeKey = "lattitudeKeyKey"
        const val vechicleKey = "vechicleKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.detail),
            true
        )
        previewCheckoutModel = PreviewCheckoutModel()
        if (intent != null && intent.hasExtra(dataJsonPreviewCheckoutKey)) {
            val dataJson = intent.getStringExtra(dataJsonPreviewCheckoutKey)
            previewCheckoutModel =
                GsonConverterHelper.getJsonObjectToGenericClass<PreviewCheckoutModel>(dataJson)
            carId = intent.getStringExtra(carIdKey)
            deviceId = intent.getStringExtra(deviceIdKey)
            pathMapScreenShot = intent.getStringExtra(pathMapScreenShotKey)
            adminArea = intent.getStringExtra(adminAreaKey)
            countryName = intent.getStringExtra(countryNameKey)
            getAddressLine = intent.getStringExtra(getAddressLineKey)
            lattitude = intent.getStringExtra(lattitudeKey)
            longitude = intent.getStringExtra(longtitudeKey)
            vechicle = intent.getStringExtra(vechicleKey)
        }
        val tripDetailBookingFragment =
            previewCheckoutModel?.let {
                TripDetailBookingFragment.newInstance(it,
                    carId.toString(),
                    deviceId.toString(),
                    pathMapScreenShot.toString(),
                    adminArea.toString(),
                    countryName.toString(),
                    getAddressLine.toString(),
                    lattitude.toString(),
                    longitude.toString(),
                    vechicle.toString())
            }
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (tripDetailBookingFragment != null) {
            fragmentTransaction.replace(R.id.container_preview_checkout,
                tripDetailBookingFragment)
                .commitAllowingStateLoss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(myBroadcastReceiver, intentFilter)
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