package com.eazy.daiku.utility.view_model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.utility.enumerable.TripEnum
import com.google.gson.JsonObject

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QrCodeVm
@Inject constructor(
    private val context: Context,
    override val repository: Repository
) : BaseViewModel(context, repository) {

    private val _loadingScanQrMutableLiveData = MutableLiveData<Boolean>()
    private val _dataMutableLiveData =
        MutableLiveData<ApiResWraper<QrCodeRespond>>()

    val loadingScanQrMutableLiveData
        get() =
            _loadingScanQrMutableLiveData
    val qrCodeDataMutableLiveData
        get() =
            _dataMutableLiveData

    fun fetchDataQrCode(code: String) {
        val requestFlow: Flow<ApiResWraper<QrCodeRespond>> =
            repository.fetchQrCode(code)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<QrCodeRespond>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingScanQrMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<QrCodeRespond>) {
                if (respondData.success) {
                    _dataMutableLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _dataMutableLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }
        })

    }

    fun requestTripToServer(
        tipEnum: TripEnum,
        code: String,
        tripListener: (Boolean) -> Unit
    ) {
        var requestFlow: Flow<ApiResWraper<QrCodeRespond>>? = null
        when (tipEnum) {
            TripEnum.Start -> {
                requestFlow =
                    repository.startTrip(code)
            }
            TripEnum.End -> {
                requestFlow =
                    repository.endTrip(code)
            }
            else -> {
            }
        }
        requestFlow?.let {
            submit(requestFlow, object : IApiResWrapper<ApiResWraper<QrCodeRespond>> {
                override fun onLoading(hasLoading: Boolean) {
                    _loadingScanQrMutableLiveData.value = hasLoading
                }

                override fun onData(respondData: ApiResWraper<QrCodeRespond>) {
                    if (respondData.success) {
                        _dataMutableLiveData.value = respondData
                        tripListener.invoke(true)
                    }
                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject
                ) {
                    _dataMutableLiveData.value = ApiResWraper(
                        code,
                        message,
                        false,
                        errorHashMap,
                    )
                }
            })
        }

    }


    private val _loadingMutableLiveData = MutableLiveData<Boolean>()
    private val _fetchProcessingMutableLiveData =
        MutableLiveData<ApiResWraper<QrCodeRespond>>()

    val loadingMutableLiveData
        get() =
            _loadingMutableLiveData
    val fetchProcessingMutableLiveData
        get() =
            _fetchProcessingMutableLiveData

    fun fetchTripProcessing() {
        val requestFlow: Flow<ApiResWraper<QrCodeRespond>> =
            repository.fetchTripProcessing()
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<QrCodeRespond>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<QrCodeRespond>) {
                if (respondData.success) {
                    _fetchProcessingMutableLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _fetchProcessingMutableLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }
        })

    }
}