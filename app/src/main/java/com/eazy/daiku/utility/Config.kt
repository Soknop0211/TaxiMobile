package com.eazy.daiku.utility

import android.os.Build
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.data.model.UserDefaultSingleTon

object Config {
    const val BASE_URL = BuildConfig.BASE_URL
    const val EAZYTAXI_PREF_NAME: String = "EAZYTAXI_PREF_NAME"
    const val LOG_CRASH_MSG: String = "LOG_CRASH_MSG"
    const val FCM_TOKEN: String = "fcm_token"

    object TypeMarkerOptions {
        const val userMarkerType = "userMarkerType"
        const val kioskMarkerType = "kioskMarkerType"
    }

    object TypeDeepLink {
        const val kessChatDeepLink = "io.kessinnovation.kesschat"
        const val acledaDeepLink = "com.domain.acledabankqr"
        const val sathapanaDeepLink = "kh.com.sathapana.consumer"
        const val abaDeepLink = "com.paygo24.ibank"
    }

    object TypeScheme {
        const val abaScheme = "abamobilebank"
        const val acledaScheme = "market"
        const val kessChatScheme = "kesspay.io"
        const val spnScheme = "spnb"
    }

    object KeyServer {
        const val Slug = "car-type"
    }

    object QrStatus {
        const val processingTrip = "processing_trip"
        const val startTrip = "start_trip"
    }

    object KycDocStatus {
        const val Pending = "PENDING"
        const val Verified = "VERIFIED"
        const val Refused = "REFUSED"
    }

    object HistoryStatus {
        const val Paid = "paid"
        const val Failed = "failed"
        const val Blocked = "blocked"
        const val Received = "received"
    }

    object BankBic {
        const val bicABA = "ABAAKHPP"
        const val bicACL = "ACLBKHPP"
    }

    object InternetCon {
        const val LocalizeException = "LocalizeException"
        const val UnknownHostException = "UnknownHostException"
        const val ConnectException = "ConnectException"
        const val SSLHandshakeException = "SSLHandshakeException"
        const val SSLProtocolException = "SSLProtocolException"
    }

    object HttpStatusCode {
        const val UNAUTHORIZED = 401
        const val BAD_GATEWAY = 502
        const val GATE_WAY_TIMEOUT = 504
        const val SERVICE_UNAVAILABLE = 503
        const val SERVER_DOWN = 521
        const val INTERNAL_SERVER_ERROR = 500
        const val SUCCESSED = 200

    }

    object KycDocKey {
        const val selfie = "selfie"
        const val idCard = "idCard"
        const val vehiclePicture = "vehiclePicture"
        const val plateNumberImg = "plateNumberImg"
        const val plateNumberText = "plateNumberText"
        const val organizationCode = "organizationCode"
    }

    object WeGoBusiness {
        const val Employee = 1
        const val Freelance = 0
    }

    object ParseServerKey {
        const val LiveMapKey = "LiveUser" //LiveMap
        const val LiveAttendanceKey = "LiveAttendance"
        const val LiveUserKey = "LiveUser"
        const val BookingTaxiKey = "BookingTaxi"
    }

    object TaxiAppType {
        const val EazyTaxiDriver = "eazy_taxi_driver"
        const val EazyTaxiCustomer = "eazy_taxi_customer"
        const val WegoTaxiDriver = "wego_taxi_driver"
    }

    object UserDeviceInfo {
        var appServer = if (BuildConfig.IS_DEBUG) "development" else "production"
        val appType = if (BuildConfig.IS_TAXI) {
            TaxiAppType.EazyTaxiDriver
        } else if (BuildConfig.IS_CUSTOMER) {
            TaxiAppType.EazyTaxiCustomer
        } else if (BuildConfig.IS_WEGO_TAXI) {
            TaxiAppType.WegoTaxiDriver
        } else {
            ""
        }
        const val deviceType = "android"


        val device = "Android"

        //val BatteryLevel = EazyTaxiHelper.getBatteryPercentage()
        val Connection = UserDefaultSingleTon.newInstance?.networkName
        val modelName = "${Build.BRAND}"
        val deviceName = "${Build.MODEL}"
        val version = "${Build.VERSION.RELEASE}"
    }

    object StatusAssignKey {
        const val Accepted = "Accepted"
        const val Rejected = "Rejected"
        const val Cancelled = "Cancelled"
        const val NoResponse = "No-Response"
        const val New = "New"
        const val Assigned = "Assigned"
    }

    object TypeMapMarker {
        const val markerA="markerA"
        const val markerB="markerB"
    }

    object  KeySearch{
        const val pointA="pointA"
        const val pointB="pointB"
        const val currentGPS="currentGPS"
        const val setPointOnMapA="setPointOnMapA"
        const val setPointOnMapB="setPointOnMapB"
    }

}