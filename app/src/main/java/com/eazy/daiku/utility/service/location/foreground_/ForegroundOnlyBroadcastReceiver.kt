package com.eazy.daiku.utility.service.location.foreground_

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.extension.toText
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*

/**
 * Receiver for location broadcasts from [ForegroundOnlyLocationService].
 */
class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val location = intent.getParcelableExtra<Location>(
            ForegroundOnlyLocationService.EXTRA_LOCATION
        )

        if (location != null) {

            AppLOGG.d(
                "ForegroundOnlyLocationService",
                "Foreground location: ${location.toText()}"
            )
            ParseLiveLocationHelper
                .newInstance?.let { parseServer ->
                    parseServer.submitLocationParseServer(context, location)
                }
        }
    }

    private fun uploadToParseServer(context: Context, location: Location) {
        var tripJson = ""
        var bookingCode = ""
        val qrCodeRespond = UserDefaultSingleTon.newInstance?.qrCodeRespond
        qrCodeRespond?.let {
            bookingCode = it.code ?: ""
            tripJson = GsonConverterHelper.convertGenericClassToJson(it)
        }

        val lat = location.latitude ?: 0.00
        val lng = location.longitude ?: 0.00
        val gpsHeading = location.bearing

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
                        parseObject.put("app_server", Config.UserDeviceInfo.appServer)
                        parseObject.put("app_type", Config.UserDeviceInfo.appType)
                        parseObject.put("app_info", parseServer.appInfo())
                        parseObject.put("device_info", parseServer.deviceInfo())
                        parseObject.put("device_type", Config.UserDeviceInfo.deviceType)
                        parseObject.put("gps_heading", gpsHeading)

                        //user
                        parseObject.put("user_id", userId)
                        parseObject.put("user_info", userInfoStr)
                        parseObject.put("last_updated_date", currentDate)
                        parseObject.put("last_updated_time", currentTime)

                        //location
                        parseObject.put("lat", lat)
                        parseObject.put("long", lng)

                        parseObject.put("booking_code", bookingCode)
                        parseObject.put("destination_info", tripJson)

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