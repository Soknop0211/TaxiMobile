package com.eazy.daiku.utility.view_model.uplaod_doc_vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.CreateUserRespond
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.google.gson.JsonObject

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadDocVM
@Inject constructor(
    private val context: Context,
    override val repository: Repository
) : BaseViewModel(context, repository) {

    private val _loadingUploadDocMutableLiveData = MutableLiveData<Boolean>()
    private val _vehicleTypeUploadDocMutableLiveData =
        MutableLiveData<ApiResWraper<ArrayList<VehicleTypeRespond>>>()

    val loadingUploadDocMutableLiveData
        get() =
            _loadingUploadDocMutableLiveData
    val vehicleTypeUploadDocMutableLiveData
        get() =
            _vehicleTypeUploadDocMutableLiveData

    fun fetchVehicleType(vehicleType: String) {
        val requestFlow: Flow<ApiResWraper<ArrayList<VehicleTypeRespond>>> =
            repository.fetchVehicleType(vehicleType)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<ArrayList<VehicleTypeRespond>>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUploadDocMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<ArrayList<VehicleTypeRespond>>) {
                if (respondData.success) {
                    _vehicleTypeUploadDocMutableLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _globalErrorMutableLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }
        })
    }


    /**
     * register new user
     * */
    private val _registerMutableLiveData = MutableLiveData<ApiResWraper<CreateUserRespond>>()
    val registerMutableLiveData: LiveData<ApiResWraper<CreateUserRespond>> get() = _registerMutableLiveData

    fun submitCreateUser(body: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<CreateUserRespond>> = repository.createUser(body)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<CreateUserRespond>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUploadDocMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<CreateUserRespond>) {
                if (respondData.success) {
                    _registerMutableLiveData.value = respondData
                }

            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {

                _registerMutableLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }

        })
    }

    /**
     * re upload kyc
     * */

    private val _reUploadKycDocMutableLiveData = MutableLiveData<ApiResWraper<User>>()
    val reUploadKycDocMutableLiveData: LiveData<ApiResWraper<User>> get() = _reUploadKycDocMutableLiveData

    fun reUploadKycDoc(body: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<User>> = repository.reUploadKycDoc(body)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<User>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUploadDocMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<User>) {
                if (respondData.success) {
                    _reUploadKycDocMutableLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _reUploadKycDocMutableLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }
        })
    }
}