package com.eazy.daiku.data.repository

import android.content.Context
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.data.model.ListAllBankAccountModel
import com.eazy.daiku.data.model.WithdrawMoneyRespondModel
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.*
import com.eazy.daiku.data.remote.EazyTaxiApi
import com.eazy.daiku.data.model.VerifyBankAccountModel
import com.eazy.daiku.data.network.Resource
import com.eazy.daiku.ui.customer.model.SearchMapRespondModel
import com.eazy.daiku.ui.customer.model.ListKioskModel
import com.eazy.daiku.ui.customer.model.PreviewCheckoutModel
import com.eazy.daiku.ui.customer.model.WebPayRespondModel
import com.eazy.daiku.ui.customer.model.sear_map.SearchMapModel
import com.example.example.BookingProcessingModel
import com.github.kotlintelegrambot.dispatcher.Dispatcher

import com.google.gson.JsonElement
import com.parse.ParseQuery.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import javax.inject.Inject

class Repository
@Inject constructor(
    private val context: Context,
    private val apiService: EazyTaxiApi,
) {

    fun login(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<JsonElement>> = flow {
        val respond = apiService.login(bodyMap)
        emit(respond)
    }

    fun logout(): Flow<ApiResWraper<JsonElement>> = flow {
        val respond = apiService.logout()
        emit(respond)
    }

    fun userInfo(): Flow<ApiResWraper<User>> = flow {
        val respond = apiService.userInfo()
        emit(respond)
    }

    fun updateAvailableTaxi(bodyRequest: HashMap<String, Any>): Flow<ApiResWraper<User>> = flow {
        val respond = apiService.updateAvailableTaxi(bodyRequest)
        emit(respond)
    }

    fun createUser(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<CreateUserRespond>> = flow {
        val respond = apiService.createUser(bodyMap)
        emit(respond)
    }

    fun fetchVehicleType(vehicleType: String): Flow<ApiResWraper<ArrayList<VehicleTypeRespond>>> =
        flow {
            val respond = apiService.fetchVehicleType(vehicleType)
            emit(respond)
        }

    fun fetchQrCode(code: String): Flow<ApiResWraper<QrCodeRespond>> = flow {
        val respond = apiService.fetchQrCode(code)
        emit(respond)
    }

    fun startTrip(code: String): Flow<ApiResWraper<QrCodeRespond>> = flow {
        val respond = apiService.startTrip(code)
        emit(respond)
    }

    fun endTrip(code: String): Flow<ApiResWraper<QrCodeRespond>> = flow {
        val respond = apiService.endTrip(code)
        emit(respond)
    }

    fun fetchTripProcessing(): Flow<ApiResWraper<QrCodeRespond>> = flow {
        val respond = apiService.fetchTripProcessing()
        emit(respond)
    }

    fun reUploadKycDoc(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<User>> = flow {
        val respond = apiService.reUploadKycDoc(bodyMap)
        emit(respond)
    }

    fun fetchTransaction(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<TransactionRespond>> =
        flow {
            val respond = apiService.fetchTransaction(bodyMap)
            emit(respond)
        }

    fun fetchHistory(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<List<History>>> = flow {
        val respond = apiService.fetchHistory(bodyMap)
        emit(respond)
    }

    fun submitCustomerHistory(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<List<CustomerHistoryModel>>> =
        flow {
            val respond = apiService.submitCustomerHistory(bodyMap)
            emit(respond)
        }

    fun submitWithdraw(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<WithdrawMoneyRespondModel>> =
        flow {
            val respond = apiService.submitWithdraw(bodyMap)
            emit(respond)
        }

    fun submitChangePwd(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<JsonElement>> = flow {
        val respond = apiService.submitChangePwd(bodyMap)
        emit(respond)
    }

    fun submitRequestOtp(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<JsonElement>> = flow {
        val respond = apiService.submitRequestOtp(bodyMap)
        emit(respond)
    }

    fun submitVerifyOtp(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<JsonElement>> = flow {
        val respond = apiService.submitVerifyOtp(bodyMap)
        emit(respond)
    }

    fun submitChangePasswordByOtp(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<JsonElement>> =
        flow {
            val respond = apiService.submitChangePasswordByOtp(bodyMap)
            emit(respond)
        }

    fun fetchCommunityTaxi(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<List<CommunityTaxiRespond>>> =
        flow {
            val respond = apiService.fetchCommunityTaxi(bodyMap)
            emit(respond)
        }

    fun updateUserInfo(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<User>> = flow {
        val respond = apiService.updateUserInfo(bodyMap)
        emit(respond)
    }

    fun submitVerifyBankAccount(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<VerifyBankAccountModel>> =
        flow {
            val respond = apiService.verifyBankAccount(bodyMap)
            emit(respond)
        }

    fun submitSearchMap(textSearch: String): Flow<SearchMapModel> =
        flow {
            val respond = apiService.submitSearchMap(textSearch)
            emit(respond)
        }

    fun submitSearchMapLocal(textSearch: String): Flow<SearchMapModel> =
        flow {
            val respond = apiService.submitSearchMapLocal(textSearch)
            emit(respond)
        }

    fun submitCreateUserCustomer(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<CreateUserRespond>> =
        flow {
            val respond = apiService.submitCreateUserCustomer(bodyMap)
            emit(respond)
        }

    fun submitSearchLocationKioskNearBy(): Flow<ApiResWraper<ListKioskModel>> =
        flow {
            val respond = apiService.submitSearchLocationKioskNearBy()
            emit(respond)
        }

    fun submitGetTaxiDriver(
        map: HashMap<String, Any>,
    ): Flow<ApiResWraper<JsonElement>> =
        flow {
            val respond = apiService.submitGetTaxiDriver(map)
            emit(respond)
        }

    fun submitCheckoutTaxi(
        body: HashMap<String, Any>,
    ): Flow<ApiResWraper<PreviewCheckoutModel>> =
        flow {
            val respond = apiService.submitCheckoutTaxi(body)
            emit(respond)
        }

    fun submitListAllLocationKiosk(
        allDevice: Boolean,
    ): Flow<ApiResWraper<List<ListKioskModel>>> =
        flow {
            val respond = apiService.submitListAllLocationKiosk(allDevice)
            emit(respond)
        }

    fun submitCheckout(
        bodyMap: HashMap<String, Any>,
    ): Flow<ApiResWraper<WebPayRespondModel>> =
        flow {
            val respond = apiService.submitCheckout(bodyMap)
            emit(respond)
        }

    fun submitListAllBank(): Flow<ApiResWraper<List<ListAllBankAccountModel>>> =
        flow {
            val respond = apiService.submitListAllBank()
            emit(respond)
        }

    fun userSubmitWithdraw(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<WithdrawMoneyRespondModel>> =
        flow {
            val respond = apiService.userSubmitWithdraw(bodyMap)
            emit(respond)
        }

    fun submitBookingPreviewDoCheckout(bodyMap: HashMap<String, Any>): Flow<ApiResWraper<BookingPreviewDoCheckoutModel>> =
        flow {
            val respond = apiService.submitBookingPreviewDoCheckout(bodyMap)
            emit(respond)
        }

    fun submitBookingProcessing(): Flow<ApiResWraper<BookingProcessingModel>> =
        flow {
            val respond = apiService.submitBookingProcessing()
            emit(respond)
        }

    fun submitCancelBookingTaxi(code: String): Flow<ApiResWraper<JsonElement>> =
        flow {
            val respond = apiService.submitCancelBookingTaxi(code)
            emit(respond)
        }
}
