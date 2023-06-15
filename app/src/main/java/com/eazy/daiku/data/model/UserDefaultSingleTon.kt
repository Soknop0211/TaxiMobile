package com.eazy.daiku.data.model

import android.graphics.Bitmap
import android.location.Location
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.NetworkUtils
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.BaseCoreActivity
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.service.location.foreground_.ForegroundOnlyBroadcastReceiver
import kotlin.collections.HashMap

class UserDefaultSingleTon {

    var qrCodeRespond: QrCodeRespond? = null
    var startStopState: TripEnum = TripEnum.Nothing
    var user: User? = null
    var hasMainActivity: BaseCoreActivity? = null
    var globalBaseCoreActivity: BaseCoreActivity? = null
    val docKycTemporary: HashMap<String, Bitmap>? = null
    var localizeLanguage: String = ""
    var networkName: String = ""
    var bookingTaxiModel: BookingTaxiModel? = null
    var bookingTaxiTemporaryObject: BookingTaxiModel? = null
    var lastLocation: Location? = null

    var currentLatitudeUser: String? = null
    var currentLngtitudeUser: String? = null

    private var foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
    fun getForegroundOnlyBroadcastReceiverSingleTon(): ForegroundOnlyBroadcastReceiver {
        if (foregroundOnlyBroadcastReceiver == null) {
            foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
        }
        return foregroundOnlyBroadcastReceiver
    }

    companion object {
        private var ourInstance: UserDefaultSingleTon? = null

        @JvmStatic
        val newInstance: UserDefaultSingleTon?
            get() {
                if (ourInstance == null) {
                    ourInstance = UserDefaultSingleTon()
                }
                return ourInstance
            }

    }


}