package com.eazy.daiku.ui.customer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.BookingPreviewDoCheckoutModel
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.ui.customer.model.SearchMapRespondModel
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.ui.customer.model.ListKioskModel
import com.eazy.daiku.ui.customer.model.PreviewCheckoutModel
import com.eazy.daiku.ui.customer.model.WebPayRespondModel
import com.eazy.daiku.ui.customer.model.sear_map.SearchMapModel
import com.eazy.daiku.utility.other.AppLOGG
import com.example.example.BookingProcessingModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProcessCustomerBookingViewModel @Inject constructor(
    context: Context,
    override val repository: Repository,
    // TODO: ==========================Start Search Map From google map==========================
) : BaseViewModel(context, repository) {
    private val _loadingSearchMapLiveData = MutableLiveData<Boolean>()
    private val _dataSearchMapLiveData = MutableLiveData<SearchMapModel>()


    val loadingSearchMapLiveData: MutableLiveData<Boolean> get() = _loadingSearchMapLiveData
    val dataSearchMapLiveData: MutableLiveData<SearchMapModel> get() = _dataSearchMapLiveData

    fun submitSearchMap(textSearchMap: String) {
        val requesFlow: Flow<SearchMapModel> = repository.submitSearchMap(textSearchMap)
        submit(requesFlow, object : IApiResWrapper<SearchMapModel> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingSearchMapLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: SearchMapModel) {
                _dataSearchMapLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {

            }

        })

    }
    // TODO: ==========================End Search Map From google ==========================

    // TODO: ==========================Start Search Map Local==========================
    private val _loadingSearchMapLocalLiveData = MutableLiveData<Boolean>()
    private val _dataSearchMapLocalLiveData = MutableLiveData<SearchMapModel>()

    val loadingSearchMapLocalLiveData: MutableLiveData<Boolean> get() = _loadingSearchMapLocalLiveData
    val dataSearchMapLocalLiveData: MutableLiveData<SearchMapModel> get() = _dataSearchMapLocalLiveData

    fun submitSearchMapLocal(textSearchMap: String) {
        val requesFlow: Flow<SearchMapModel> = repository.submitSearchMapLocal(textSearchMap)
        submit(requesFlow, object : IApiResWrapper<SearchMapModel> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingSearchMapLocalLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: SearchMapModel) {
                _dataSearchMapLocalLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {

            }

        })
    }

    // TODO: ==========================End Search Map Local==========================

    // TODO: ==========================Start search location kiosk ==========================
    private val _loadingSearchLocationKioskLiveData = MutableLiveData<Boolean>()
    private val _dataSearchLocationKioskLiveData = MutableLiveData<ApiResWraper<ListKioskModel>>()

    val loadingSearchLocationKioskLiveData: MutableLiveData<Boolean> get() = _loadingSearchLocationKioskLiveData
    val dataSearchLocationKioskLiveData: MutableLiveData<ApiResWraper<ListKioskModel>> get() = _dataSearchLocationKioskLiveData

    fun submitSearchLocationKioskNearBy() {
        val requesFlow: Flow<ApiResWraper<ListKioskModel>> =
            repository.submitSearchLocationKioskNearBy()
        submit(requesFlow, object : IApiResWrapper<ApiResWraper<ListKioskModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingSearchLocationKioskLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<ListKioskModel>) {
                _dataSearchLocationKioskLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataSearchLocationKioskLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })

    }

    // TODO: ==========================End Search location kiosk ==========================


    // TODO: ==========================Start get list taxi ==========================
    private val _loadingGetTaxiDriverLiveData = MutableLiveData<Boolean>()
    private val _dataGetTaxiDriverLiveData = MutableLiveData<ApiResWraper<JsonElement>>()

    val loadingGetTaxiDriverLiveData: MutableLiveData<Boolean> get() = _loadingGetTaxiDriverLiveData
    val dataGetTaxiDriverLiveData: MutableLiveData<ApiResWraper<JsonElement>> get() = _dataGetTaxiDriverLiveData

    fun submitGetTaxiDriver(map: HashMap<String, Any>) {
        val requesFlow: Flow<ApiResWraper<JsonElement>> =
            repository.submitGetTaxiDriver(map)
        submit(requesFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingGetTaxiDriverLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                _dataGetTaxiDriverLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataGetTaxiDriverLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })
    }
    // TODO: ==========================End get list taxi ==========================


    // TODO: ==========================Start checkout taxi ==========================

    private val _loadingCheckoutTaxiLiveData = MutableLiveData<Boolean>()
    private val _dataCheckoutTaxiLiveData = MutableLiveData<ApiResWraper<PreviewCheckoutModel>>()

    val loadingCheckoutTaxiLiveData: MutableLiveData<Boolean> get() = _loadingCheckoutTaxiLiveData
    val dataCheckoutTaxiLiveData: MutableLiveData<ApiResWraper<PreviewCheckoutModel>> get() = _dataCheckoutTaxiLiveData

    fun submitCheckoutTaxi(body: HashMap<String, Any>) {
        val requesFlow: Flow<ApiResWraper<PreviewCheckoutModel>> =
            repository.submitCheckoutTaxi(body)
        submit(requesFlow, object : IApiResWrapper<ApiResWraper<PreviewCheckoutModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingCheckoutTaxiLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<PreviewCheckoutModel>) {
                _dataCheckoutTaxiLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataCheckoutTaxiLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })
    }

    // TODO: ==========================End checkout taxi ==========================


    // TODO: ==========================Start list all location kiosk taxi ==========================
    private val _loadingListAllLocationKioskLiveData = MutableLiveData<Boolean>()
    private val _dataListAllLocationKioskLiveData =
        MutableLiveData<ApiResWraper<List<ListKioskModel>>>()

    val loadingListAllLocationKioskLiveData: MutableLiveData<Boolean> get() = _loadingListAllLocationKioskLiveData
    val dataListAllLocationKioskLiveData: MutableLiveData<ApiResWraper<List<ListKioskModel>>> get() = _dataListAllLocationKioskLiveData

    fun submitListAllLocationKiosk(allDevice: Boolean) {
        val requesFlow: Flow<ApiResWraper<List<ListKioskModel>>> =
            repository.submitListAllLocationKiosk(allDevice)
        submit(requesFlow, object : IApiResWrapper<ApiResWraper<List<ListKioskModel>>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingListAllLocationKioskLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<List<ListKioskModel>>) {
                _dataListAllLocationKioskLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataListAllLocationKioskLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })
    }
    // TODO: ==========================End list all location kiosk taxi ==========================

    // TODO: ==========================Start checkout ==========================
    private val _loadingCheckoutLiveData = MutableLiveData<Boolean>()
    private val _dataCheckoutLiveData = MutableLiveData<ApiResWraper<WebPayRespondModel>>()

    val loadingCheckoutLiveData: MutableLiveData<Boolean> get() = _loadingCheckoutLiveData
    val dataCheckoutLiveData: MutableLiveData<ApiResWraper<WebPayRespondModel>> get() = _dataCheckoutLiveData

    fun submitCheckout(body: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<WebPayRespondModel>> = repository.submitCheckout(body)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<WebPayRespondModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingCheckoutLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<WebPayRespondModel>) {
                _dataCheckoutLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataCheckoutLiveData.postValue(ApiResWraper(code, message, false, errorHashMap))
            }

        })
    }


    // TODO: ==========================End checkout ==========================

    // TODO: ==========================Start Booking Preview Do Checkout==========================

    private val _loadingBookingPreviewDoCheckoutLiveData = MutableLiveData<Boolean>()
    private val _dataBookingPreviewDoCheckoutLiveData =
        MutableLiveData<ApiResWraper<BookingPreviewDoCheckoutModel>>()

    val loadingBookingPreviewDoCheckoutLiveData: MutableLiveData<Boolean> get() = _loadingBookingPreviewDoCheckoutLiveData
    val dataBookingPreviewDoCheckoutLiveData: MutableLiveData<ApiResWraper<BookingPreviewDoCheckoutModel>> get() = _dataBookingPreviewDoCheckoutLiveData

    fun submitBookingPreviewDoCheckout(body: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<BookingPreviewDoCheckoutModel>> =
            repository.submitBookingPreviewDoCheckout(body)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<BookingPreviewDoCheckoutModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingBookingPreviewDoCheckoutLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<BookingPreviewDoCheckoutModel>) {
                _dataBookingPreviewDoCheckoutLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataBookingPreviewDoCheckoutLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })
    }

    // TODO: ==========================End Booking Preview Do Checkout==========================


    // TODO: ==========================Start Booking Processing==========================
    private val _loadingBookingProcessingLiveData = MutableLiveData<Boolean>()
    private val _dataBookingProcessingLiveData = MutableLiveData<ApiResWraper<BookingProcessingModel>>()

    val loadingBookingProcessingLiveData: MutableLiveData<Boolean> get() = _loadingBookingProcessingLiveData
    val dataBookingProcessingLiveData: MutableLiveData<ApiResWraper<BookingProcessingModel>> get() = _dataBookingProcessingLiveData

    fun submitBookingProcessing() {
        val requestFlow: Flow<ApiResWraper<BookingProcessingModel>> =
            repository.submitBookingProcessing()
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<BookingProcessingModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingBookingProcessingLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<BookingProcessingModel>) {
                _dataBookingProcessingLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataBookingProcessingLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))

            }

        })
    }

    // TODO: ==========================End Booking Processing==========================


    // TODO: ==========================Start Cancel Booking Taxi==========================
    private val _loadingCancelBookingLiveData = MutableLiveData<Boolean>()
    private val _dataCancelBookingLiveData = MutableLiveData<ApiResWraper<JsonElement>>()

    val loadingCancelBookingLiveData: MutableLiveData<Boolean> get() = _loadingCancelBookingLiveData
    val dataCancelBookingLiveData: MutableLiveData<ApiResWraper<JsonElement>> get() = _dataCancelBookingLiveData

    fun submitCancelBooingTaxi(code: String) {
        val requestFlow: Flow<ApiResWraper<JsonElement>> =
            repository.submitCancelBookingTaxi(code)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingCancelBookingLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                _dataCancelBookingLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataCancelBookingLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })
    }
    // TODO: =========================End Cancel Booking Taxi==========================
}