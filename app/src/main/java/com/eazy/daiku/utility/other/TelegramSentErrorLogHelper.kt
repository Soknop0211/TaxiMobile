package com.eazy.daiku.utility.other

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.EazyTaxiApplication
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.remote.GeneralApi
import com.eazy.daiku.data.remote.TelegramApi
import com.eazy.daiku.utility.DateTimeHelper
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.NetworkUtils
import com.eazy.daiku.utility.base.BaseCoreActivity
import com.eazy.daiku.utility.base.MCoroutineAsyncTask
import com.github.kotlintelegrambot.entities.Message
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TelegramSentErrorLogHelper
private constructor() {

    private val networkSymbol = "\uD83D\uDCF6"
    private val locationSymbol = "\uD83D\uDCCD"
    private val robotSymbol = "\uD83E\uDD16"
    private val fullBatterySymbol = "\uD83D\uDD0B"
    private val lowBatterySymbol = "\uD83E\uDEAB"
    private var text = ""

    fun sentLogErrorToTelegram(
        titleHeader: String,
        header: String,
        bodyRequest: String,
        bodyRespond: String,
        respondTimer: String = ""
    ) {
        try {
            val publicIp = getPublicIPAddress().replace("\"", "\\\"")
            val battery =
                if (EazyTaxiHelper.getBatteryPercentage() <= 20) lowBatterySymbol else fullBatterySymbol
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    Thread {
                        try {
                            val network = String.format(
                                Locale.getDefault(),
                                "%s • %s",
                                networkType(),
                                publicIp
                            )
                            text = "$robotSymbol ${BuildConfig.APP_NAME} $titleHeader\n\n" +
                                    "👤 ${userError()}" +
                                    "\n\n$header\n" +
                                    "Request: \n" +
                                    "$bodyRequest\n\n" +

                                    "Respond in $respondTimer : \n" +
                                    "$bodyRespond\n\n" +

                                    currentDateTime() + "\n" +
                                    timeZoneUser() + "\n" +
                                    network + "\n" +
                                    appNameVersion() + "\n" +
                                    toGetDeviceName() +
                                    getAndroidApi() + "\n" +
                                    String.format(
                                        Locale.US,
                                        "%s Battery Level: %d%s",
                                        battery,
                                        EazyTaxiHelper.getBatteryPercentage(), "%"
                                    )
                            submitToTelegramChecked(text)

//                            AppLOGG.d(
//                                "CaptureScreen",
//                                "prepare sent -> ${Gson().toJson(captureScreen())}"
//                            )
//                            captureScreen()?.let { file ->
//
//                                sentCaptionAndPhoto(file, text)
//
//                            } ?: submitToTelegramChecked(text)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.start()
                },
                4000 // value in milliseconds
            )
        } catch (ignored: Exception) {
            AppLOGG.d("AppLOGGAppLOGG", ignored.localizedMessage)
        }
    }

    private fun submitToTelegramChecked(text: String) {
        try {
            val retrofit = createRetrofitService(BuildConfig.BASE_TELEGRAM)
            val telegramApi = retrofit.create(TelegramApi::class.java)
            val chatId = if (BuildConfig.IS_DEBUG) CHAT_ID_DEV else CHAT_ID_PROD
            val queryString = HashMap<String, Any>()
            queryString["chat_id"] = chatId
            queryString["text"] = text
            val call = telegramApi.submitErrorTelegramDefault(queryString)
            call.execute()
        } catch (ignored: Exception) {
            if (BuildConfig.IS_DEBUG) {
                AppLOGG.d("AppLOGGAppLOGG", "${Gson().toJson(ignored)}")
            }
        }
    }

    /**
     * current date time
     * */
    private fun currentDateTime(): String {
        val dateTimeCalendar = Calendar.getInstance()

        val dateStr = DateTimeHelper.dateFm(dateTimeCalendar.time, "EEE, dd MMM yyyy, hh:mm:ss a")
        return String.format(
            "\uD83D\uDD5B %s ",
            dateStr
        )
    }

    private fun timeZoneUser(): String {
        return "$locationSymbol" + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Calendar.getInstance().timeZone.toZoneId().id
        } else {
            Calendar.getInstance().timeZone.id
        }
    }

    private fun userError(): String {
        val user = hasActivityAlive()?.let {
            it.getUserUserToSharePreference()?.name.let { userName ->
                String.format("%s", userName ?: "N/A")
            }
        } ?: "-"

        return user
    }

    private fun appNameVersion(): String {
        return String.format(
            "⚙️ ${BuildConfig.APP_NAME} • %s(%s) • %s",
            BuildConfig.VERSION_NAME,
            BuildConfig.BUILD_VERSION,
            if (BuildConfig.BUILD_TYPE == "debug") "Dev" else "Prod"
        )
    }

    private fun toGetDeviceName(): String {
        return String.format(
            "\uD83D\uDCF1 %s",
            getDeviceName()
        )
    }

    private fun getDeviceName(): String {
        val manufacturer: String = Build.MANUFACTURER
        val model: String = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun getAndroidApi(): String {
        return String.format(" (V.%s)", Integer.valueOf(Build.VERSION.RELEASE).toString())
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        var phrase = ""
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c)
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase += c
        }
        return phrase
    }

    private fun networkType(): String? {
        var networkName: String? = null
        hasActivityAlive()?.let {
            val networkClass = NetworkUtils.getNetworkClass(it)
            val speed = NetworkUtils.getNetworkSpeed(it)
            networkName =
                "$networkSymbol $networkClass"
        }
        return networkName
    }

    private fun getPublicIPAddress(): String {
        var publicIp = "-"
        val retrofit = createRetrofitService("https://icanhazip.com")
        val generalApi = retrofit.create(GeneralApi::class.java)
        val respond = generalApi.getPublicIp().execute()
        respond.body()?.let {
            publicIp = it.asString.toString()
        }
        AppLOGG.d("AppLOGGAppLOGG", "public ip -> $publicIp")
        return publicIp
    }

    private fun captureScreen(): File? {
        var mFile: File? = null
        try {
            AppLOGG.d("CaptureScreen", "Start ->")
            hasActivityAlive()?.let {
                //ImageHelper.bitmapFromView(it, it.window.decorView.rootView)
//                it.runOnUiThread {
//
//                }
                Handler(Looper.getMainLooper()).post {
                    val rootView = it.window.decorView.rootView
                    mFile = ImageHelper.uiToFile(it.self(), rootView)
                    AppLOGG.d("CaptureScreen", "in Handler -> ${Gson().toJson(mFile)}")
                }
            }
        } catch (ignored: Exception) {
            AppLOGG.d("CaptureScreen", ignored.localizedMessage)
        }
        return mFile
    }

    private fun formatFileSize(size: Double): String {
        val hrSize: String
        val k = size / 1024.0
        val m = size / 1024.0 / 1024.0
        val g = size / 1024.0 / 1024.0 / 1024.0
        val t = size / 1024.0 / 1024.0 / 1024.0 / 1024.0
        val dec = DecimalFormat("0.00")
        hrSize = if (t > 1) {
            dec.format(t) + " "
        } else if (g > 1) {
            dec.format(g)
        } else if (m > 1) {
            dec.format(m) + " mb/s"
        } else if (k > 1) {
            dec.format(k) + " kb/s"
        } else {
            dec.format(size)
        }
        return hrSize
    }

    private fun speedInternet(context: Context): Int {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val downSpeed: Int = nc?.linkDownstreamBandwidthKbps ?: 0
        val uploadSpeed: Int = nc?.linkUpstreamBandwidthKbps ?: 0
        return uploadSpeed / 1000
    }

    private fun getWifiLevel(context: Context): Int {
        var linkSpeed = 1
        val wifiManager: WifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
        if (wifiInfo != null) {
            linkSpeed = wifiInfo.linkSpeed //measured using WifiInfo.LINK_SPEED_UNITS
        }
        return linkSpeed
    }

    private fun hasActivityAlive(): BaseCoreActivity? {
        /* EazyTaxiApplication.mCurrentActivity?.let {
             if (it is BaseActivity) {
                 return it
             }
         }*/
        /*  if (UserDefaultSingleTon.newInstance?.hasMainActivity is BaseCoreActivity) {
              return UserDefaultSingleTon.newInstance?.hasMainActivity
          }*/
        if (UserDefaultSingleTon.newInstance?.globalBaseCoreActivity is BaseCoreActivity) {
            return UserDefaultSingleTon.newInstance?.globalBaseCoreActivity
        }
        return null
    }

    private fun sentCaptionAndPhoto(file: File, caption: String) {
        try {
            val telegramApi = createMultiPartRetrofit(BuildConfig.BASE_TELEGRAM)
            val chatId = if (BuildConfig.IS_DEBUG) CHAT_ID_DEV else CHAT_ID_PROD
            val queryString = HashMap<String, Any>()
            queryString["chat_id"] = chatId
            queryString["caption"] = caption

            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart(
                "photo",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
            val req = builder.build()
            val call = telegramApi.sendPhoto(queryString, req)
            call.enqueue(object : Callback<Response<Message>> {
                override fun onResponse(
                    call: Call<Response<Message>>,
                    response: Response<Response<Message>>
                ) {
                    if (BuildConfig.IS_DEBUG) {
                        AppLOGG.d("CaptureScreen", "after text -> ${Gson().toJson(response)}")
                    }
                }

                override fun onFailure(call: Call<Response<Message>>, t: Throwable) {
                    if (BuildConfig.IS_DEBUG) {
                        AppLOGG.d("CaptureScreen", "after text -> ${t.localizedMessage}")
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createMultiPartRetrofit(baseUrl: String): TelegramApi {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getMultiPathOkHttpClient())
            .build()
        return retrofit.create(TelegramApi::class.java)
    }

    private fun getMultiPathOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        val interceptor = MultiPathInterceptor()
        httpClient.addInterceptor(interceptor)
        httpClient.connectTimeout(40, TimeUnit.SECONDS)
        httpClient.writeTimeout(60, TimeUnit.SECONDS)
        httpClient.readTimeout(60, TimeUnit.SECONDS)
        return httpClient.build()
    }

    class MultiPathInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val original = chain.request()
            val builder = original.newBuilder()
            builder.addHeader("Content-Type", "multipart/form-data")
            val request = builder.build()
            return chain.proceed(request)
        }
    }

    private fun createRetrofitService(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        //  .addConverterFactory(GsonConverterFactory.create())
    }

    companion object {
        private var ourInstance: TelegramSentErrorLogHelper? = null

        @JvmStatic
        val instance: TelegramSentErrorLogHelper?
            get() {
                if (ourInstance == null) {
                    ourInstance = TelegramSentErrorLogHelper()
                }
                return ourInstance
            }

        private const val CHAT_ID_DEV = -1001580343972 //-1001575354411 old group
        private const val CHAT_ID_PROD = -1001767489584 //-1001405720385  old group
    }


}