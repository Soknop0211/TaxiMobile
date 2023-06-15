package com.eazy.daiku.utility.parse_server

import android.content.Context
import android.location.Location
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.EazyTaxiApplication
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.other.AppLOGG
import com.google.gson.JsonObject
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*
import kotlin.collections.ArrayList

class ParseLiveLocationHelper {

    companion object {
        private var ourInstance: ParseLiveLocationHelper? = null

        @JvmStatic
        val newInstance: ParseLiveLocationHelper?
            get() {
                if (ourInstance == null) {
                    ourInstance = ParseLiveLocationHelper()
                }
                return ourInstance
            }

    }

    private var appServer = Config.UserDeviceInfo.appServer
    private var appType = Config.UserDeviceInfo.appType
    private var deviceType = Config.UserDeviceInfo.deviceType
    private var currentCalendar = Calendar.getInstance()


    var lastLocation: Location? = null

    //device info
    fun deviceInfo(): String {
        val deviceInfoGson = JsonObject()

        deviceInfoGson.addProperty(
            "battery_level",
            "-168%" /*EazyTaxiHelper.getBatteryPercentage()*/
        )

        deviceInfoGson.addProperty("connection", Config.UserDeviceInfo.Connection)
        deviceInfoGson.addProperty("device_name", Config.UserDeviceInfo.deviceName)
        deviceInfoGson.addProperty("model_name", Config.UserDeviceInfo.modelName)
        deviceInfoGson.addProperty("time_zone", Calendar.getInstance().timeZone.id)
        deviceInfoGson.addProperty("version", Config.UserDeviceInfo.version)
        return GsonConverterHelper.convertGenericClassToJson(deviceInfoGson)
    }

    //get user
    fun getUser(context: Context): User? {
        val jsonStr = EazyTaxiHelper.getSharePreference(context, Constants.userDetailKey)
        if (jsonStr.isEmpty()) {
            return null
        }
        return GsonConverterHelper.getJsonObjectToGenericClass(jsonStr)
    }

    //user Info
    fun userInfo(user: User): String {
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
    fun appInfo(): String {
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

    fun submitLocationParseServer(context: Context, location: Location?) {
        val lat = location?.latitude ?: 0.00
        val lng = location?.longitude ?: 0.00
        val gpsHeading: Float = location?.bearing ?: 0f
        var tripJson = ""
        var bookingCode = ""

        if (location != null) {
            lastLocation = location
            if (context is EazyTaxiApplication) {
                if (context.mCurrentBaseActivity is BaseActivity) {
                    val baseActivity = context.mCurrentBaseActivity as BaseActivity
                    baseActivity.checkingStopMovingDriver(lastLocation)
                }
            }

        }

        val qrCodeRespond = UserDefaultSingleTon.newInstance?.qrCodeRespond
        qrCodeRespond?.let {
            bookingCode = it.code ?: ""
            tripJson = GsonConverterHelper.convertGenericClassToJson(it)
        }

        val user: User? = getUser(context)
        val userId: Int = user?.id ?: -1
        var userInfoStr = ""
        user?.let {
            userInfoStr = userInfo(it)
        }

        val parseQuery: ParseQuery<ParseObject> =
            checkLiveMapUserParseServer(userId)
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
                parseObject.put("device_info", deviceInfo())
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

                parseObject.saveInBackground()

            } else {
                //add new
                val liveMapUserParseServer =
                    liveMapUserParseServer(
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

    /**
     * live attendance
     * */
    fun addAttendanceUser(
        appInfo: String,

        userId: Int,
        userInfo: String,
        isEmployee: Boolean,
        loginDate: String,
        loginTime: String,

        gpsHeading: Double,
        latitude: Double,
        longtitude: Double,
    ): ParseObject {
        val parseObject = ParseObject(Config.ParseServerKey.LiveAttendanceKey)
        parseObject.put("app_server", appServer)
        parseObject.put("app_type", appType)
        parseObject.put("app_info", appInfo)
        parseObject.put("device_info", deviceInfo())
        parseObject.put("device_type", deviceType)

        parseObject.put("user_id", userId)
        parseObject.put("user_info", userInfo)
        parseObject.put("is_employee", isEmployee)
        parseObject.put("login_date", loginDate)
        parseObject.put("login_time", loginTime)

        parseObject.put("gps_heading", gpsHeading)
        parseObject.put("lat", latitude)
        parseObject.put("long", longtitude)

        return parseObject
    }

    fun checkAttendanceUser(
        userId: Int,
    ): ParseQuery<ParseObject> {
        val currentDate = DateTimeHelper.dateFm(currentCalendar.time, "E, dd MMM yyyy")
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.LiveAttendanceKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("app_type", appType)
            .whereEqualTo("user_id", userId)
            .whereEqualTo("login_date", currentDate)
        return parseQuery
    }

    /**
     * live user to parse server
     * */

    //check live user
    fun checkLiveUserParseServer(
        userId: Int,
    ): ParseQuery<ParseObject> {
        val currentDate = DateTimeHelper.dateFm(currentCalendar.time, "E, dd MMM yyyy")
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.LiveUserKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("app_type", appType)
            .whereEqualTo("user_id", userId)
        return parseQuery
    }

    //get live user eazy customer
    fun checkLiveUserCustomerParseServer(
        userId: Int,
    ): ParseQuery<ParseObject> {
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.LiveUserKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("user_id", userId)
        return parseQuery
    }

    //object live user
    fun liveUserParseServer(
        appInfo: String,

        userId: Int,
        userInfo: String,
        isEmployee: Boolean,
        lastUpdatedDate: String,
        lastUpdatedTime: String,

        gpsHeading: Float,
        latitude: Double,
        longtitude: Double,

        displayOnMap: Boolean,

        ): ParseObject {
        val parseObject = ParseObject(Config.ParseServerKey.LiveUserKey)
        parseObject.put("app_server", appServer)
        parseObject.put("app_type", appType)
        parseObject.put("app_info", appInfo)
        parseObject.put("device_info", deviceInfo())
        parseObject.put("device_type", deviceType)

        parseObject.put("user_id", userId)
        parseObject.put("user_info", userInfo)
        parseObject.put("is_employee", isEmployee)
        parseObject.put("last_updated_date", lastUpdatedDate)
        parseObject.put("last_updated_time", lastUpdatedTime)

        parseObject.put("gps_heading", gpsHeading)
        parseObject.put("lat", latitude)
        parseObject.put("long", longtitude)

        parseObject.put("display_on_map", displayOnMap)

        return parseObject
    }

    fun liveUserParseServer(
        parseObject: ParseObject,
        appInfo: String,

        userId: Int,
        userInfo: String,
        isEmployee: Boolean,
        lastUpdatedDate: String,
        lastUpdatedTime: String,

        gpsHeading: Float,
        latitude: Double,
        longtitude: Double,

        displayOnMap: Boolean,

        ): ParseObject {
        parseObject.put("app_server", appServer)
        parseObject.put("app_type", appType)
        parseObject.put("app_info", appInfo)
        parseObject.put("device_info", deviceInfo())
        parseObject.put("device_type", deviceType)

        parseObject.put("user_id", userId)
        parseObject.put("user_info", userInfo)
        parseObject.put("is_employee", isEmployee)
        parseObject.put("last_updated_date", lastUpdatedDate)
        parseObject.put("last_updated_time", lastUpdatedTime)

        parseObject.put("gps_heading", gpsHeading)
        parseObject.put("lat", latitude)
        parseObject.put("long", longtitude)

        parseObject.put("display_on_map", displayOnMap)

        return parseObject
    }

    fun liveUserUpdateBookingCode(context: Context, bookingCode: String) {
        val user: User? = getUser(context)
        val userId: Int = user?.id ?: -1
        val parseQuery: ParseQuery<ParseObject> =
            checkLiveMapUserParseServer(userId)
        parseQuery.findInBackground { objects, exception ->
            val parseObject = objects.first()
            parseObject.put("booking_code", bookingCode)
            parseObject.saveInBackground()

        }
    }

    /**
     * live map to parse server
     * */

    //check live user
    fun checkLiveMapUserParseServer(
        userId: Int,
    ): ParseQuery<ParseObject> {
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.LiveMapKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("app_type", appType)
            .whereEqualTo("user_id", userId)
        return parseQuery
    }


    //object liveMap user
    fun liveMapUserParseServer(
        appInfo: String,

        userId: Int,
        userInfo: String,
        lastUpdatedDate: String,
        lastUpdatedTime: String,

        gpsHeading: Float,
        latitude: Double,
        longtitude: Double,

        bookingCode: String,
        startTripDate: String,
        startTripTime: String,
        destinationInfo: String,

        ): ParseObject {
        //change
        val parseObject = ParseObject(Config.ParseServerKey.LiveMapKey)

        //device
        parseObject.put("app_server", appServer)
        parseObject.put("app_type", appType)
        parseObject.put("app_info", appInfo)
        parseObject.put("device_info", deviceInfo())
        parseObject.put("device_type", deviceType)
        parseObject.put("gps_heading", gpsHeading)

        //user
        parseObject.put("user_id", userId)
        parseObject.put("user_info", userInfo)
        parseObject.put("last_updated_date", lastUpdatedDate)
        parseObject.put("last_updated_time", lastUpdatedTime)

        //location
        parseObject.put("lat", latitude)
        parseObject.put("long", longtitude)

        //trip info
        parseObject.put("booking_code", bookingCode)
        parseObject.put("start_trip_date", startTripDate)
        parseObject.put("start_trip_time", startTripTime)
        parseObject.put("destination_info", destinationInfo)

        return parseObject
    }

    /**
     * booking Taxi parse server
     * */

    //check live user
    fun checkAssignTaxiIsSelf(
        userId: Int,
        status: String,
    ): ParseQuery<ParseObject> {
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.BookingTaxiKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("app_type", appType)
            .whereEqualTo("assign_to", userId)
            .whereEqualTo("status", status)
        return parseQuery
    }

    fun checkCodeTaxiIsSelf(
        userId: Int,
        code: String
    ): ParseQuery<ParseObject> {
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.BookingTaxiKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("app_type", appType)
            .whereEqualTo("code", code)
        return parseQuery
    }


    fun checkAssignTaxiIsSelf(
    ): ParseQuery<ParseObject> {
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.BookingTaxiKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("app_type", appType)
        return parseQuery
    }

    fun checkStatusAssignTaxi(outTradeNo: String): ParseQuery<ParseObject> {
        val parseQuery: ParseQuery<ParseObject> =
            ParseQuery.getQuery(Config.ParseServerKey.BookingTaxiKey)
        parseQuery
            .whereEqualTo("app_server", appServer)
            .whereEqualTo("code", outTradeNo)
        return parseQuery
    }

}