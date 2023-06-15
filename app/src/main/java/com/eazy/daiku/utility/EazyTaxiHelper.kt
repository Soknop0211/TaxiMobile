package com.eazy.daiku.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ApplicationErrorReport
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.paging.PagedList
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.SaveBankAccount
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.ui.customer.model.SearchMapHistoryModel
import com.eazy.daiku.utility.base.BaseCoreActivity
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.edit_text.EazyLengthFilter
import com.eazy.daiku.utility.edit_text.MoneyValueFilter
import com.eazy.daiku.utility.enumerable.TripEnum
import com.eazy.daiku.utility.other.AppLOGG
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.*
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.*
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object EazyTaxiHelper {

    //set header title app
    fun setUpTitleAppBar(
        context: BaseCoreActivity,
        appBarTitle: String,
        showSymbolBack: Boolean = false,
    ) {
        val toolbar = context.findViewById<Toolbar>(R.id.toolbar)
        context.setSupportActionBar(toolbar)
        val title = context.findViewById<TextView>(R.id.title)
        title.text = appBarTitle
        if (context.supportActionBar != null) {
            context.supportActionBar!!.setDisplayShowTitleEnabled(false)
            context.supportActionBar!!.setDisplayHomeAsUpEnabled(showSymbolBack)
        }
    }

    //validate field editText
    fun validateField(
        fieldLayout: TextInputLayout,
        data: CharSequence,
        msgError: String,
    ) {

        fieldLayout.error = if (data.isEmpty()) msgError else null

    }

    fun makeCall(context: Context, number: String) {
        if (!number.isEmpty()) {
            val callIntent = Intent(Intent.ACTION_VIEW)
            callIntent.data = Uri.parse("tel:$number")
            context.startActivity(callIntent)
        }
    }

    fun configurePagedListConfig(): PagedList.Config {
        return PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(50)
            .setPageSize(50)
            .build()
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun phoneNumberValidate(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }


    // TODO: save list bank account
    fun setSharePreference(context: Context, PREF_NAME: String, VALUE: String) {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(PREF_NAME, VALUE)
        editor.apply()
    }

    fun setDeviceTokenPreference(context: Context, PREF_NAME: String, VALUE: String) {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(PREF_NAME, VALUE)
        AppLOGG.d("MYTOKENFIR", "myToken--> " + VALUE)
        editor.apply()
    }

    fun getDeviceTokenPreference(context: Context, PREF_NAME: String): String {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_NAME, "token") ?: "test"
    }

    // TODO: get list bank account
    fun getSharePreference(context: Context, PREF_NAME: String): String {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_NAME, "") ?: ""
    }

    // TODO: remove list bank account
    fun removeListSharePreference(
        context: Context,
        bankList: ArrayList<SaveBankAccount>,
        bankAccount: SaveBankAccount,
        userId: Int,
    ) {
        val listBank = ArrayList<SaveBankAccount>()
        var saveBankAccount: SaveBankAccount? = null
        var isRemove = false
        for (i in bankList) {
            if (i.uuid == bankAccount.uuid) {
                removeFirstBankAccount(context)
                isRemove = true
                continue
            }
            saveBankAccount = SaveBankAccount(i.userId, i.uuid, i.bic, i.name, i.number, i.logo)
            listBank.add(saveBankAccount)
        }
        if (isRemove && saveBankAccount != null) {
            val myBankAccountJson = getFirstBankAccount(context)
            val myListBankAccount: ArrayList<SaveBankAccount> =
                GsonConverterHelper.getListMyBankAccount(myBankAccountJson)
            if (myListBankAccount.size > 0) {
                for (myBankAccount in myListBankAccount) {
                    if (userId == myBankAccount.userId) {
                        myListBankAccount.remove(myBankAccount)
                    }
                }
            }
            myListBankAccount.add(saveBankAccount)
            GsonConverterHelper.saveListMyBankAccount(context, myListBankAccount)
        }
        GsonConverterHelper.saveBankAccountJson(context, listBank)
    }

    //todo save list withdraw bank account
    fun setFirstBankAccountSharePreference(context: Context, PREF_NAME: String, VALUE: String) {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(PREF_NAME, VALUE)
        editor.apply()
    }

    // TODO: get list withdraw bank account

    fun getFirstBankAccountSharePreference(context: Context, PREF_NAME: String): String {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_NAME, "") ?: ""
    }

    // TODO: remove list withdraw bank account
    fun removeFirstBankAccount(context: Context) {
        val prefs = context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(Constants.saveFirstBankAccount)
        editor.apply()
    }

    // TODO: save list history search map
    fun setListSearchHistoryMapSharePreference(context: Context, PREF_NAME: String, VALUE: String) {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(PREF_NAME, VALUE)
        editor.apply()
    }

    // TODO: get list search history map
    fun getSearchHistoryMapSharePreference(context: Context, PREF_NAME: String): String {
        val prefs =
            context.getSharedPreferences(Constants.EAZYTAXI_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_NAME, "") ?: ""
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return true
                }
            }
        }
        return false
    }

    fun hasNotNetworkAvailable(context: Context): Boolean {
        return !isNetworkAvailable(context)
    }

    fun clearSession(context: Context) {
        UserDefaultSingleTon.newInstance?.startStopState = TripEnum.Nothing
        UserDefaultSingleTon.newInstance?.qrCodeRespond = null
        UserDefaultSingleTon.newInstance?.docKycTemporary?.clear()
        UserDefaultSingleTon.newInstance?.bookingTaxiTemporaryObject = null

        setSharePreference(context, Constants.userDetailKey, "")
        setSharePreference(context, Constants.Token.API_TOKEN, "")
        setSharePreference(context, Constants.biometricKey, "")
        setSharePreference(context, Config.FCM_TOKEN, "")


    }

    fun formatDate(date: Date?, pattern: String?): String? {
        val format = SimpleDateFormat(pattern, Locale.US)
        return try {
            format.format(date)
        } catch (dte: Exception) {
            ""
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun KessLogDataGson(): com.eazy.daiku.data.network.CustomHttpLogging {
        return com.eazy.daiku.utility.other.LogDemoJson.KessLogDataGson()
    }

    // TODO: save list bank account
    fun saveListBankAccountNumber(context: Context, data: String) {
        setSharePreference(context, Constants.saveListAccountNumberBank, data)
    }

    // TODO: get list bank account
    fun getSaveListAccountNumber(context: Context): String {
        val data: String = getSharePreference(context, Constants.saveListAccountNumberBank)
        return data/*.ifEmpty { null }*/
    }

    // TODO: remove list bank account
    fun removeListBankAccount(
        context: Context,
        listBankAccount: ArrayList<SaveBankAccount>,
        saveBankAccount: SaveBankAccount,
        userId: Int,
    ) {
        removeListSharePreference(context, listBankAccount, saveBankAccount, userId)
    }

    // TODO: save list withdraw bank account
    fun saveFirstBankAccount(context: Context, data: String) {
        setFirstBankAccountSharePreference(context, Constants.saveFirstBankAccount, data)
    }

    // TODO: get list withdraw bank account
    fun getFirstBankAccount(context: Context): String {
        val data: String =
            getFirstBankAccountSharePreference(context, Constants.saveFirstBankAccount)
        return data
    }

    // TODO: save list search history map
    fun saveListSearchHistoryMap(context: Context, data: String) {
        setListSearchHistoryMapSharePreference(context, Constants.saveListSearchHistoryMap, data)
    }

    // TODO: get list search history map
    fun getSearchHistoryMap(context: Context): String {
        val data: String =
            getSearchHistoryMapSharePreference(context, Constants.saveListSearchHistoryMap)
        return data
    }


    // TODO: remove list search history map
    fun removeListSearchHistoryMap(
        context: Context,
        listSearchHistoryMap: ArrayList<SearchMapHistoryModel>,
        searchMapHistoryModel: SearchMapHistoryModel,
    ) {

    }

    fun hideKeyboard(view: View) {
        val inputManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getBatteryPercentage(): Int {
        var battery: Int? = 0
        try {
            UserDefaultSingleTon.newInstance?.globalBaseCoreActivity?.let { context ->
                battery = if (Build.VERSION.SDK_INT >= 21) {
                    val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
                    bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                } else {
                    val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    val batteryStatus: Intent? = context.registerReceiver(null, iFilter)
                    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = scale?.toDouble()?.let { level?.div(it) }
                    (batteryPct?.times(100))?.toInt()
                }
            }
        } catch (ignored: Exception) {
        }

        return battery ?: 0
    }

}

object ImageHelper {
    fun detectQrImageToText(bMap: Bitmap): String? {
        try {
            val intArray = IntArray(bMap.width * bMap.height)
            //copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
            val source: LuminanceSource = RGBLuminanceSource(bMap.width, bMap.height, intArray)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val reader: Reader = MultiFormatReader()
            var result: Result? = null
            result = reader.decode(bitmap)
            return result.text
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getWidthScreen(context: Context): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels /*/ displayMetrics.density*/
    }

    fun getHeightScreen(context: Context): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels /* / displayMetrics.density*/
    }

    /**
     * param
     * Bitmap.CompressFormat.JPEG -> jpeg
     * Bitmap.CompressFormat.PNG -> png
     * */
    fun convertBase64(
        bitmap: Bitmap,
        compressFile: Bitmap.CompressFormat,
        quality: Int,
    ): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(compressFile, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
        return stream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun uiToFile(context: Context, view: View): File? {
        bitmapFromView(context, view)?.let {
            return fileFromBitmap(context, it)
        }
        return null
    }

    fun bitmapFromView(context: Context, view: View): Bitmap? {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun fileFromBitmap(context: Context, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(context.cacheDir, "${System.currentTimeMillis()}_temporary")
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()

        return file

    }


    fun loadImage(
        context: Context,
        resource: Any,
        imageView: ImageView,
    ) {
        Glide.with(context)
            .load(resource)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }


    fun loadImage(
        context: Context,
        resource: Any,
        placeHolder: Int,
        imageView: ImageView,
    ) {
        Glide.with(context)
            .load(resource)
            .placeholder(placeHolder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun loadImage(
        context: Context,
        resource: Any,
        placeHolder: Int,
        errorResource: Int,
        imageView: ImageView,
    ) {
        Glide.with(context)
            .load(resource)
            .placeholder(placeHolder)
            .error(errorResource)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun loadImage(
        context: Context,
        resource: Any,
    ): Bitmap {
        return Glide.with(context)
            .asBitmap()
            .load(resource)
            .submit()
            .get()
    }
}

object GsonConverterHelper {


    // TODO: get list all bank account
    fun getBankAccountLists(BankAccountJson: String): ArrayList<SaveBankAccount> {
        if (BankAccountJson.isEmpty()) return ArrayList()
        val gsonConvert = Gson()
        val BankAccountType = object : TypeToken<ArrayList<SaveBankAccount>>() {}.type
        return gsonConvert.fromJson(BankAccountJson, BankAccountType)
    }


    // TODO: save list bank account json
    fun saveBankAccountJson(
        context: Context,
        BankLists: ArrayList<SaveBankAccount>,
    ) {
        val gsonConverter = Gson()
        val readyBankAccountConvert = gsonConverter.toJson(BankLists)
        EazyTaxiHelper.saveListBankAccountNumber(context, readyBankAccountConvert)
    }

    // TODO: save list withdraw bank account json
    fun saveListMyBankAccount(context: Context, myBankAccountList: ArrayList<SaveBankAccount>) {
        val gsonConverter = Gson()
        val readyFirstBankAccountConvert = gsonConverter.toJson(myBankAccountList)
        EazyTaxiHelper.saveFirstBankAccount(context, readyFirstBankAccountConvert)

    }

    // TODO: get list withdraw bank account
    fun getFirstBankAccount(bankAccount: String): SaveBankAccount {
        if (bankAccount.isEmpty()) return SaveBankAccount()
        val gsonConvert = Gson()
        val bankAccountType = object : TypeToken<SaveBankAccount>() {}.type
        return gsonConvert.fromJson(bankAccount, bankAccountType)
    }

    // TODO: get list withdraw my bank account
    fun getListMyBankAccount(myBankAccountJson: String): ArrayList<SaveBankAccount> {
        if (myBankAccountJson.isEmpty()) return ArrayList()
        val gsonConvert = Gson()
        val BankAccountType = object : TypeToken<ArrayList<SaveBankAccount>>() {}.type
        return gsonConvert.fromJson(myBankAccountJson, BankAccountType)
    }

    // TODO: save list searchHistory map json
    fun saveListSearchHistoryMap(
        context: Context,
        mySearchHistoryMapList: ArrayList<SearchMapHistoryModel>,
    ) {
        val gsonConverter = Gson()
        val mySearchHistoryMap = gsonConverter.toJson(mySearchHistoryMapList)
        EazyTaxiHelper.saveListSearchHistoryMap(context, mySearchHistoryMap)
    }

    // TODO: get list search history map
    fun getListMySearchHistoryMap(myHistoryMapJson: String): ArrayList<SearchMapHistoryModel> {
        if (myHistoryMapJson.isEmpty()) return ArrayList()
        val gsonConvert = Gson()
        val historyMapType = object : TypeToken<ArrayList<SearchMapHistoryModel>>() {}.type
        return gsonConvert.fromJson(myHistoryMapJson, historyMapType)
    }

    fun <T> convertGenericClassToJson(
        data: T,
    ): String {
        val gsonConverter = Gson()
        return gsonConverter.toJson(data)
    }

    inline fun <reified T> getJsonObjectToGenericClass(jsonData: String?): T {
        return Gson().fromJson(jsonData, T::class.java)
    }

    inline fun <reified T> getJsonObjectToGenericClassValidate(jsonData: String?): T? {
        try {
            return Gson().fromJson(jsonData, T::class.java)
        } catch (ignored: Exception) {
        }
        return null
    }
}

object AmountHelper {

    fun amountFormat(currencySymbol: String, amount: Double): String {
        try {
            val decimalFm = DecimalFormat("#,###.##")
            return String.format(Locale.US, "%s%.2f", currencySymbol, decimalFm.format(amount))
        } catch (ignored: IllegalArgumentException) {
        }
        return String.format(Locale.US, "%s%.2f", currencySymbol, amount)
    }

    fun decimalFilter(editText: EditText, digit: Int) {
        val moneyValueFilter: MoneyValueFilter = MoneyValueFilter.newInstance()
        val lengthFilter = EazyLengthFilter(12)
        editText.filters = arrayOf(moneyValueFilter, lengthFilter)
        moneyValueFilter.setDigits(digit)
    }

    fun latLngFormat(kilomater: Double): String {
        try {
            val decimalFm = DecimalFormat("#,###.#######")
            return String.format(Locale.US, "%s", decimalFm.format(kilomater))
        } catch (ignored: IllegalArgumentException) {
        }
        return String.format(Locale.US, "%s", kilomater)
    }
}

object DateTimeHelper {

    /**
     * Functionality to do convert server date time to local date time
     *
     * @param dateString => yyyy-MM-dd HH:mm:ss
     * @return => yyyy-MM-dd HH:mm:ss
     */
    fun localDateTimeFm(dateString: String): Date? {
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        serverDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val serverDate = serverDateFormat.parse(dateString) ?: return null
            val timeZone = TimeZone.getDefault()
            val strCurrentZone =
                TimeZone.getTimeZone(timeZone.id).getDisplayName(false, TimeZone.SHORT)
            val strDate: String = formatToCurrentTimeZone(
                serverDate,
                "yyyy-MM-dd",
                TimeZone.getTimeZone(strCurrentZone)
            )
            val strTime: String = formatToCurrentTimeZone(
                serverDate,
                "HH:mm:ss",
                TimeZone.getTimeZone(strCurrentZone)
            )
            if (strDate.isNotEmpty() && strTime.isNotEmpty()) {
                val currentZoneFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                currentZoneFormat.timeZone = TimeZone.getTimeZone(strCurrentZone)

                //return new SimpleDateFormat(format, Locale.getDefault()).format(Objects.requireNonNull(date));
                return currentZoneFormat.parse(String.format("%s %s", strDate, strTime))
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * return date format with local timezone by Locale.getDefault()
     */
    private fun formatToCurrentTimeZone(d: Date, format: String, tz: TimeZone): String {
        //SimpleDateFormat need to set Timezone to parse String to Date
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = tz
        return sdf.format(d)
    }

    fun dateFm(dateStr: Date, pattern: String): String {
        val format = SimpleDateFormat(pattern, Locale.US)
        return try {
            format.format(dateStr)
        } catch (dte: Exception) {
            ""
        }
    }

    fun dateParse(dateStr: String): Date {
        if (dateStr == "") return Date()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dd = simpleDateFormat.parse(dateStr)
        dd?.let {
            return it
        }
        return Date()
    }

    fun dateFm(value: String, patternValue: String, newPattern: String): String {
        try {
            @SuppressLint("SimpleDateFormat") val parser = SimpleDateFormat(patternValue)
            @SuppressLint("SimpleDateFormat") val printer = SimpleDateFormat(newPattern)
            val date = parser.parse(value)
            if (date != null) {
                return printer.format(date)
            }
        } catch (ignored: ParseException) {
        }
        return ""
    }
}

object StatusColor {

    fun colorStatus(context: Context, statusStr: String): Int {
        return when {
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Paid -> {
                ContextCompat.getColor(context, (R.color.green_600))
            }
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Failed -> {
                ContextCompat.getColor(context, (R.color.red))
            }
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Received -> {
                ContextCompat.getColor(context, (R.color.green_600))
            }
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Blocked -> {
                ContextCompat.getColor(context, (R.color.gray_dark))
            }
            else -> {
                ContextCompat.getColor(context, (R.color.colorPrimary))
            }
        }
    }

    fun colorStatusTrx(context: Context, statusStr: String): Drawable? {
        return when {
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Paid -> {
                ContextCompat.getDrawable(context, R.drawable.circle_green_status)
            }
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Received -> {
                ContextCompat.getDrawable(context, R.drawable.circle_green_status)
            }
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Failed -> {
                ContextCompat.getDrawable(context, R.drawable.circle_red_status)
            }
            statusStr.lowercase(Locale.getDefault()) == Config.HistoryStatus.Blocked -> {
                ContextCompat.getDrawable(context, R.drawable.circle_gray_color_drawable)
            }
            else -> {
                ContextCompat.getDrawable(context, R.drawable.circle_color_primary_drawable)
            }
        }
    }
}

object KeyBoardHelper {
    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

object firmareOS {

    var mCrashInfo: ApplicationErrorReport.CrashInfo? = null
    var mErrorMessage = ""
    const val LINE_SEPARATOR = "\n"

    fun errorMsg(exception: Throwable): String {
        val mReportBuilder = StringBuilder()

        if (!"deviceinfo firmware".contains("error")) {
            mReportBuilder.append(reportError(exception))
        }
        if (!"deviceinfo firmware".contains("callstack")) {
            mReportBuilder.append(reportCallStack(exception))
        }
        if (!"deviceinfo firmware".contains("deviceinfo")) {
            mReportBuilder.append(reportDeviceInfo())
        }
        if (!"deviceinfo firmware".contains("firmware")) {
            mReportBuilder.append(reportFirmware())
        }
        return mReportBuilder.toString()
    }

    fun reportError(exception: Throwable?): String {
        mCrashInfo = ApplicationErrorReport.CrashInfo(exception)
        mErrorMessage = if (mCrashInfo!!.exceptionMessage == null) {
            "<unknown error>"
        } else {
            mCrashInfo!!.exceptionMessage
                .replace(": " + mCrashInfo!!.exceptionClassName, "")
        }
        val throwFile =
            if (mCrashInfo!!.throwFileName == null) "<unknown file>" else mCrashInfo!!.throwFileName
        return """
                    ************ ${mCrashInfo!!.exceptionClassName} ************
                    $mErrorMessage$LINE_SEPARATOR
                     File: $throwFile
                     Method: ${mCrashInfo!!.throwMethodName}()
                     Line No.: ${Integer.toString(mCrashInfo!!.throwLineNumber)}$LINE_SEPARATOR"""
        /*+ "Class: " + mCrashInfo.throwClassName + LINE_SEPARATOR;*/
    }

    fun reportCallStack(exception: Throwable): String {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        val callStack = stackTrace.toString()
        val errMsg = exception.toString()
        return """
                
                ************ CALLSTACK ************
                ${callStack.replace(errMsg, "")}$LINE_SEPARATOR
                """.trimIndent()
    }

    fun reportDeviceInfo(): String {
        return """
                
                ************ DEVICE INFORMATION ***********
                Brand: ${Build.BRAND}${LINE_SEPARATOR}Device: ${Build.DEVICE}${LINE_SEPARATOR}Model: ${Build.MODEL}${LINE_SEPARATOR}Id: ${Build.ID}${LINE_SEPARATOR}Product: ${Build.PRODUCT}$LINE_SEPARATOR
                """.trimIndent()
    }

    fun reportFirmware(): String {
        return """
                
                ************ FIRMWARE ************
                SDK: ${Build.VERSION.SDK_INT}${LINE_SEPARATOR}Release: ${Build.VERSION.RELEASE}${LINE_SEPARATOR}Incremental: ${Build.VERSION.INCREMENTAL}$LINE_SEPARATOR
                """.trimIndent()
    }

}

object NumberFormatHelper {
    fun abaFormatBankAccount(cardNumber: String?): String {
        if (cardNumber == null) return ""
        val delimiter = ' '
        return cardNumber.replace(".{3}(?!$)".toRegex(), "$0$delimiter")
    }

    fun acledaFormatBankAccount(cardNumber: String?): String {
        if (cardNumber == null) return ""
        val delimiter = ' '
        return cardNumber.replace(".{4}(?!$)".toRegex(), "$0$delimiter")
    }
}

object AddressHelper {
    fun getStringAddress(context: Context?, latitude: Double?, longitude: Double?): String {
        var addr = ""
        if (latitude != null && longitude != null) {
            val addresses: List<Address>
            if (latitude != 0.0 && longitude != 0.0) {
                try {
                    val geocoder = Geocoder(context)
                    addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        val address = addresses[0]
                        try {
                            addr = address.getAddressLine(0)
                        } catch (ignored: java.lang.IllegalArgumentException) {
                        }
                        if (TextUtils.isEmpty(addr)
                            && !TextUtils.isEmpty(address.featureName)
                            && !TextUtils.isEmpty(address.subAdminArea)
                            && !TextUtils.isEmpty(address.adminArea)
                        ) {
                            addr =
                                address.featureName + ", " + address.subAdminArea + ", " + address.adminArea
                        }
                    }
                } catch (ex: java.lang.Exception) {
                    Toast.makeText(context, ex.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
        return addr
    }

    fun getStringAddressByIndex(
        context: Context?,
        latitude: Double?,
        longitude: Double?,
    ): Address? {
        var address: Address? = null
        if (latitude != null && longitude != null) {
            val addresses: List<Address>
            if (latitude != 0.0 && longitude != 0.0) {
                try {
                    val geocoder = Geocoder(context)
                    addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        address = addresses[0]
                    }
                    return address
                } catch (ex: java.lang.Exception) {
                    Toast.makeText(context, ex.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
        return address
    }
}

object BiometricSecurity {
    fun checkBiometric(context: Context): Boolean {
        val biometricManager: BiometricManager = BiometricManager.from(context)
        var error: String? = null
        var isBiometric = false
        var isAction = false
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> isBiometric = true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                error = "No biometric features available on this device"
                isBiometric = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                error = "Biometric features are currently unavailable."
                isBiometric = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                error = "The user hasn't associated any biometric credentials with their account."
                isBiometric = false
                isAction = true
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                error = "Biometric features are currently security update required."
                isBiometric = false
                isAction = true
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                error = "Biometric features are currently unsupported."
                isBiometric = false
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                error = "Biometric features are currently status unknown."
                isBiometric = false
            }
        }
        if (error != null) {
            MessageUtils.showError(context, "", error)
        }
        return isBiometric
    }
}

object HttpStatusCode {
    fun statusCodeMsg(code: Int): String {
        return when (code) {
            100 -> "Continue"
            101 -> "SwitchingProtocols"
            102 -> "Processing"

            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "NonAuthoritativeInformation"
            204 -> "NoContent"
            205 -> "ResetContent"
            206 -> "PartialContent"
            207 -> "MultiStatus"
            208 -> "AlreadyReported"
            226 -> "IMUsed"

            300 -> "MultipleChoices"
            301 -> "MovedPermanently"
            302 -> "Found"
            303 -> "SeeOther"
            304 -> "NotModified"
            305 -> "UseProxy"
            307 -> "TemporaryRedirect"
            308 -> "PermanentRedirect"

            400 -> "BadRequest"
            401 -> "Unauthorized"
            402 -> "PaymentRequired"
            403 -> "Forbidden"
            404 -> "NotFound"
            405 -> "MethodNotAllowed"
            406 -> "NotAcceptable"
            407 -> "ProxyAuthenticationRequired"
            408 -> "RequestTimeout"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "LengthRequired"
            412 -> "PreconditionFailed"
            413 -> "PayloadTooLarge"
            414 -> "UriTooLong"
            415 -> "UnsupportedMediaType"
            416 -> "RangeNotSatisfiable"
            417 -> "ExpectationFailed"
            418 -> "IAmATeapot"
            421 -> "MisdirectedRequest"
            422 -> "UnprocessableEntity"
            423 -> "Locked"
            424 -> "FailedDependency"
            426 -> "UpgradeRequired"
            428 -> "PreconditionRequired"
            429 -> "TooManyRequests"
            431 -> "RequestHeaderFieldsTooLarge"
            451 -> "UnavailableForLegalReasons"

            500 -> "InternalServerError"
            501 -> "NotImplemented"
            502 -> "BadGateway"
            503 -> "ServiceUnavailable"
            504 -> "GatewayTimeout"
            505 -> "HttpVersionNotSupported"
            506 -> "VariantAlsoNegotiates"
            507 -> "InsufficientStorage"
            508 -> "LoopDetected"
            510 -> "NotExtended"
            511 -> "NetworkAuthenticationRequired"

            0 -> "Unknown"
            else -> "Unknown"
        }
    }
}

object MapHelper {
    fun bitmapDescriptorFromVector(vectorResId: Int, context: Context): BitmapDescriptor? {
        val userPin = BitmapFactory.decodeResource(
            context.resources, vectorResId
        )
        val userPinMarker =
            Bitmap.createScaledBitmap(userPin,
                userPin.width * 2,
                userPin.height * 2,
                false)
        val userPinIcon = BitmapDescriptorFactory.fromBitmap(userPinMarker)
        return userPinIcon

    }

    fun bitmapFromVector(vectorResId: Int, context: Context): BitmapDescriptor? {
        val userPin = BitmapFactory.decodeResource(
            context.resources, vectorResId
        )
        val userPinIcon = BitmapDescriptorFactory.fromBitmap(userPin)
        return userPinIcon

    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}

object QRCodeHelper {
    fun generateQrImage(str: String?): Bitmap? {
        val barcodeEncoder = BarcodeEncoder()
        try {
            return barcodeEncoder.encodeBitmap(str, BarcodeFormat.QR_CODE, 800, 800)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }
}

/**
 * Provides access to SharedPreferences for location to Activities and Services.
 */
internal object SharedPreferenceUtil {

    const val preference_file_key = "${BuildConfig.APPLICATION_ID}.PREFERENCE_FILE_KEY"
    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            SharedPreferenceUtil.preference_file_key, Context.MODE_PRIVATE
        )
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            SharedPreferenceUtil.preference_file_key,
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }
}

