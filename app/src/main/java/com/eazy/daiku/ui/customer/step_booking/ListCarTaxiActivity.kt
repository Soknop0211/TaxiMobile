package com.eazy.daiku.ui.customer.step_booking

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.eazy.daiku.R
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.ui.customer.model.ListKioskModel
import com.eazy.daiku.utility.service.MyBroadcastReceiver

class ListCarTaxiActivity : BaseActivity() {
    private var listKioskModel: ListKioskModel? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var address: String? = null
    private var pathScreenShotMap: String? = null
    private var addressByIndex: Address? = null
    private var adminArea: String? = null
    private var countryName: String? = null
    private var getAddressLine: String? = null
    private val intentFilter = IntentFilter()

    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    companion object {
        const val dataListCarTaxiJsonObjectKey = "dataListCarTaxiJsonObjectKey"
        const val latKey = "latKey"
        const val longKey = "longKey"
        const val addressKey = "addressKey"
        const val screenShotMapPathKey = "screenShotMapPathKey"
        const val addressByIndexKey = "addressByIndexKey"
        const val adminAreaKey = "adminAreaKey"
        const val countryNameKey = "countryNameKey"
        const val getAddressLineKey = "getAddressLineKey"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_car_taxi)

        initView()
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.select_vehicle),
            true
        )
        if (intent != null && intent.hasExtra(dataListCarTaxiJsonObjectKey)) {
            val dataJson = intent.getStringExtra(dataListCarTaxiJsonObjectKey)
            dataJson?.let {
                listKioskModel = GsonConverterHelper.getJsonObjectToGenericClass(dataJson)
            }
            latitude = intent.getStringExtra(latKey)
            longitude = intent.getStringExtra(longKey)
            address = intent.getStringExtra(addressKey)
            pathScreenShotMap = intent.getStringExtra(screenShotMapPathKey)
            adminArea = intent.getStringExtra(adminAreaKey)
            countryName = intent.getStringExtra(countryNameKey)
            getAddressLine = intent.getStringExtra(getAddressLineKey)
        }
        val selectCarBookingFragment =
            listKioskModel?.let {
                SelectCarBookingFragment.newInstance(it,
                    latitude.toString(),
                    longitude.toString(),
                    address.toString(),
                    pathScreenShotMap.toString(),
                    adminArea.toString(),
                    countryName.toString(),
                    getAddressLine.toString())
            }
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (selectCarBookingFragment != null) {
            fragmentTransaction.replace(R.id.container_list_car_taxi_fragment,
                selectCarBookingFragment)
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