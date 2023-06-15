package com.eazy.daiku.utility.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

open class MyBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val customBroadcastKey: String =
            com.eazy.daiku.BuildConfig.APPLICATION_ID + ".broadcast.activity.CUSTOM_CHT_BROADCAST"
        const val reloadPaymentSuccessKey: String = "payment_successfully"
        const val confirmBookingKey: String = "confirm_booking_key"
        const val stopMovingLocationDriverKey: String = "stop_moving_location_key"
        const val hasReloadWalletKey: String = "has_reload_wallet_key"
        const val hasProcessingTripKey: String = "has_processing_trip_key"
        const val reloadProfileUserKey: String = "reload_profile_key"
        const val Test: String = "test"
        const val reloadMainWalletWhenWithdrawSuccessKey = "reloadMainWalletWhenWithdrawSuccessKey"
        const val whenWithdrawMoneySuccessFinishScreenWithdrawMoneyKey =
            "whenWithdrawMoneySuccessFinishScreenWithdrawMoneyKey"
        const val finishWhenPaymentCompletedKey = "finishWhenPaymentCompletedKey"
        const val reloadProcessingBookingKey = "reloadProcessingBookingKey"
        const val updateLocationTrackerKey = "updateLocationTrackerKey"
        const val dataKey: String = "data_key"
        const val locationDataKey: String = "location_data_key"
        const val notificationAcceptOrder = "notificationAcceptOrder"
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
    }
}