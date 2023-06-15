package com.eazy.daiku.utility

import android.content.Context
import android.net.ConnectivityManager
import java.util.*
import android.os.Build
import android.telephony.TelephonyManager
import com.eazy.daiku.utility.other.AppLOGG

class NetworkUtils {

    companion object {

        // returns 102 Mbps
        fun getNetworkSpeed(context: Context): String {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nc = cm.getNetworkCapabilities(cm.activeNetwork)
                //Retrieves the downstream bandwidth for this network in Kbps.
                //This always only refers to the estimated first hop transport bandwidth.
                val uploadSpeed = (nc?.linkUpstreamBandwidthKbps)?.div(1000)
                //Retrieves the upstream bandwidth for this network in Kbps.
                //This always only refers to the estimated first hop transport bandwidth.
                val downloadSpeed = (nc?.linkDownstreamBandwidthKbps)?.div(1000)
                AppLOGG.d(Constants.pretty_gson, "upload $uploadSpeed")
                AppLOGG.d(Constants.pretty_gson, "download $downloadSpeed")
                "${uploadSpeed ?: 0} Mbps"
            } else {
                "-"
            }
        }

        // returns 2G,3G,4G,WIFI
        fun getNetworkClass(context: Context): String {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = cm.activeNetworkInfo

            if (info == null || !info.isConnected) return "-" // not connected
            if (info.type == ConnectivityManager.TYPE_WIFI) return "WIFI"
            if (info.type == ConnectivityManager.TYPE_MOBILE) {
                return when (info.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "Mobile 2G"
                    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "Mobile 3G"
                    TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "Mobile 4G"
                    else -> "?"
                }
            }
            return "?"
        }

    }
}

