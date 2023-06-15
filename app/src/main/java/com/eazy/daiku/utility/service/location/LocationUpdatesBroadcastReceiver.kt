package com.eazy.daiku.utility.service.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.widget.Toast
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.TAG
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.parse_server.ParseLiveLocationHelper
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.google.gson.JsonObject
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*

open class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PROCESS_UPDATES: String =
            BuildConfig.APPLICATION_ID + ".action.PROCESS_UPDATES"
    }

    private var appServer = Config.UserDeviceInfo.appServer
    private var appType = Config.UserDeviceInfo.appType
    private var deviceType = Config.UserDeviceInfo.deviceType

    override fun onReceive(context: Context, intent: Intent?) {
        AppLOGG.d(TAG, "out BaseActivity ")
        if (intent != null) {
            if (intent.action == ACTION_PROCESS_UPDATES) {
                // LocationAvailability.isLocationAvailable returns true if the device location is
                // known and reasonably up to date within the hints requested by the active LocationRequests.
                // Failure to determine location may result from a number of causes including disabled
                // location settings or an inability to retrieve sensor data in the device's environment.
                LocationAvailability.extractLocationAvailability(intent).let {

                }
                LocationResult.extractResult(intent).let { locationResult ->
                    locationResult?.let {
                        it.lastLocation?.let {
                            val location = locationResult.lastLocation
                            uploadToParseServer(context, location)
                            AppLOGG.d(TAG, "lat: ${location.latitude}")
                            AppLOGG.d(TAG, "lng: ${location.longitude}")
                            AppLOGG.d(TAG, "===============$==============")

                        }
                    }
                }
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
                val user: User? = getUser(context)
                val userId: Int = user?.id ?: -1

                var userInfoStr = ""
                user?.let {
                    userInfoStr = userInfo(it)
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
                        parseObject.put("app_server", appServer)
                        parseObject.put("app_type", appType)
                        parseObject.put("app_info", appInfo())
                        parseObject.put(
                            "device_info",
                            ParseLiveLocationHelper.newInstance?.deviceInfo() ?: ""
                        )
                        parseObject.put("device_type", deviceType)
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
                                appInfo(),
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

    private fun getUser(context: Context): User? {
        val jsonStr = EazyTaxiHelper.getSharePreference(context, Constants.userDetailKey)
        if (jsonStr.isEmpty()) {
            return null
        }
        return GsonConverterHelper.getJsonObjectToGenericClass(jsonStr)
    }

    //user Info
    private fun userInfo(user: User): String {
        val userGson = JsonObject()
        userGson.addProperty("avatar_url", user.avatarUrl)
        userGson.addProperty("gender", if (user.gender == 0) "Male" else "Female")
        userGson.addProperty("is_available", user.isAvailable)
        userGson.addProperty("is_employee", user.isEmployee == 1)
        userGson.addProperty("name", user.name)
        userGson.addProperty("phone", user.phone)
        userGson.addProperty("plate_number", user.kycDocument?.plateNumber)
        userGson.addProperty("termID", user.kycDocument?.termId)
        userGson.addProperty("termName", user.kycDocument?.termName)
        return GsonConverterHelper.convertGenericClassToJson(userGson)
    }

    //app Info
    private fun appInfo(): String {
        val appInfoGson = JsonObject()
        appInfoGson.addProperty("app_name", BuildConfig.APP_NAME)
        appInfoGson.addProperty(
            "version", String.format(
                "%s (%s)",
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_VERSION,
            )
        )
        return GsonConverterHelper.convertGenericClassToJson(appInfoGson)
    }

    //device info
    private fun deviceInfo(): String {
        val deviceInfoGson = JsonObject()
        deviceInfoGson.addProperty(
            "battery_level",
            ParseLiveLocationHelper.newInstance?.deviceInfo() ?: ""
        )
        deviceInfoGson.addProperty("connection", Config.UserDeviceInfo.Connection)
        deviceInfoGson.addProperty("device_name", Config.UserDeviceInfo.deviceName)
        deviceInfoGson.addProperty("model_name", Config.UserDeviceInfo.modelName)
        deviceInfoGson.addProperty("time_zone", Calendar.getInstance().timeZone.id)
        deviceInfoGson.addProperty("version", Config.UserDeviceInfo.version)
        return GsonConverterHelper.convertGenericClassToJson(deviceInfoGson)
    }
}