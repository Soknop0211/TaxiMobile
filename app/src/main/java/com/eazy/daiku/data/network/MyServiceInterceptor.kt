package com.eazy.daiku.data.network

import android.content.Context
import android.util.Log
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.EazyTaxiApplication
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.other.DeviceConfig
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.*
import javax.inject.Inject


class MyServiceInterceptor @Inject constructor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val sessionToken: String =
            EazyTaxiHelper.getSharePreference(context, Constants.Token.API_TOKEN)
        val request = chain.request()
        val builder: Request.Builder = request.newBuilder()


        /* FirebaseMessaging.getInstance().token
             .addOnCompleteListener(OnCompleteListener { task ->
                 if (!task.isSuccessful) {
                     Log.d("TAGTOKEN", "Fetching FCM registration token failed", task.exception)
                     return@OnCompleteListener
                 }

                 // Get new FCM registration token
                 //deviceToken = task.result

                 EazyTaxiHelper.setSharePreference(
                     context,
                     Config.FCM_TOKEN,
                     task.result
                 )
                 AppLOGG.d("DEVICETOKEN", "--> " + task.result)
             })

 */

        /* builder.addHeader(
            "device-token",
            "eNdBDZP3Qoi0BtmkWDg77m:APA91bHY7oVW7tBd0P8w6sCR6YnPEUOMpdO0l6k0JwNYE22V69C3Qjz9ovrh98Mh01TjCXq44kp4TgCpGjVcKcpaLnbzflIOZQpw7gE33ky2T_LqRlXdJzQM2IgujMkqlSg-eTFjBLYu"
        )*/
        val deviceToken: String = EazyTaxiHelper.getDeviceTokenPreference(context, Config.FCM_TOKEN)
        builder.addHeader("device-token", deviceToken)
        builder.addHeader("Accept", "*/*")
        builder.addHeader("x-device-type", "mobile")
        builder.addHeader("Content-Type", "application/json")
        builder.addHeader("Accept-Encoding", "Accept-Encoding")
        builder.addHeader("Content-Encoding", "gzip")

        builder.addHeader("language_code", UserDefaultSingleTon.newInstance?.localizeLanguage ?: "")


        if (BuildConfig.IS_TAXI) {
            builder.addHeader("app-type", Config.TaxiAppType.EazyTaxiDriver)
        } else if (BuildConfig.IS_CUSTOMER) {
            builder.addHeader("app-type", Config.TaxiAppType.EazyTaxiCustomer)
        } else if (BuildConfig.IS_WEGO_TAXI) {
            builder.addHeader("app-type", Config.TaxiAppType.WegoTaxiDriver)
        } else {
            builder.addHeader("app-type", "")
        }


        UserDefaultSingleTon.newInstance?.currentLatitudeUser?.let { lat ->
            builder.addHeader("current-latitude", lat)
            if (!BuildConfig.IS_CUSTOMER) {
                UserDefaultSingleTon.newInstance?.currentLatitudeUser = null
            }
        }

        UserDefaultSingleTon.newInstance?.currentLngtitudeUser?.let { lng ->
            builder.addHeader("current-longitude", lng)
            if (!BuildConfig.IS_CUSTOMER) {
                UserDefaultSingleTon.newInstance?.currentLngtitudeUser = null
            }
        }

        val deviceId: String? = (context as EazyTaxiApplication).deviceId()
        val serialNumber: String? = DeviceConfig.getSerialNumber()
        when {
            deviceId != null -> {
                builder.addHeader("device-id", deviceId)
            }

            serialNumber != null -> {
                builder.addHeader("device-sn", serialNumber)
            }

            else -> {
                builder.addHeader("device-id", String.format("%s", Date().time))
            }
        }
        if (sessionToken.isNotEmpty()) {
            builder.addHeader("Authorization", "Bearer $sessionToken")
        }

        val requestBuilder = builder.build()
        return chain.proceed(requestBuilder)
    }


}