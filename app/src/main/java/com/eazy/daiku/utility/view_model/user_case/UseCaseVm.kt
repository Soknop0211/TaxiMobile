package com.eazy.daiku.utility.view_model.user_case

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.eazy.daiku.R
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.utility.view_model.user_case.model.changePwdState
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCaseVm
@Inject constructor(
    private val context: Context,
    override val repository: Repository
) : BaseViewModel(context, repository) {


    /**
     * fetch user information
     * */

    private val _loadingUserInfoLiveData = MutableLiveData<Boolean>()
    private val _dataUserInLiveData = MutableLiveData<ApiResWraper<User>>()
    val loadingUserLiveData: MutableLiveData<Boolean> get() = _loadingUserInfoLiveData
    val dataUserLiveData: LiveData<ApiResWraper<User>> get() = _dataUserInLiveData

    fun fetchUserInfo() {
        val requestFlow: Flow<ApiResWraper<User>> = repository.userInfo()
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<User>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserInfoLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<User>) {
                if (respondData.success) {
                    _dataUserInLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _dataUserInLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }

        })
    }

    private val _logoutUserInLiveData = MutableLiveData<ApiResWraper<JsonElement>>()
    val logoutUserInLiveData: MutableLiveData<ApiResWraper<JsonElement>> get() = _logoutUserInLiveData
    fun logout() {
        val requestFlow: Flow<ApiResWraper<JsonElement>> = repository.logout()
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserInfoLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                if (respondData.success) {
                    _logoutUserInLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject,
            ) {
                _logoutUserInLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }

        })
    }

    fun updateAvailableTaxi(bodyRequest: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<User>> = repository.updateAvailableTaxi(bodyRequest)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<User>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserInfoLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<User>) {
                if (respondData.success) {
                    _dataUserInLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _dataUserInLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }

        })

    }

    /**
     * request otp
     * */
    private val _requestOtpMutableLiveData =
        MutableLiveData<ApiResWraper<JsonElement>>()
    val requestOtpMutableLiveData: LiveData<ApiResWraper<JsonElement>> get() = _requestOtpMutableLiveData

    fun submitRequestOtp(bodyMap: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<JsonElement>> =
            repository.submitRequestOtp(bodyMap)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserInfoLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                if (respondData.success) {
                    _requestOtpMutableLiveData.value = respondData
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
     * verify otp
     * */
    private val _verifyOtpMutableLiveData =
        MutableLiveData<ApiResWraper<JsonElement>>()
    val verifyOtpMutableLiveData: LiveData<ApiResWraper<JsonElement>> get() = _verifyOtpMutableLiveData
    fun submitVerifyOtp(bodyMap: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<JsonElement>> =
            repository.submitVerifyOtp(bodyMap)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserInfoLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                if (respondData.success) {
                    _verifyOtpMutableLiveData.value = respondData
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
     * change password with otp
     * */
    private val _submitChangePasswordByOtpMutableLiveData =
        MutableLiveData<ApiResWraper<JsonElement>>()
    val submitChangePasswordByOtpMutableLiveData: LiveData<ApiResWraper<JsonElement>> get() = _submitChangePasswordByOtpMutableLiveData

    fun submitChangePasswordByOtp(bodyMap: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<JsonElement>> =
            repository.submitChangePasswordByOtp(bodyMap)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _submitChangePasswordLoadingMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                if (respondData.success) {
                    _submitChangePasswordByOtpMutableLiveData.value = respondData
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
     * change password
     * */

    private val _changePwdValidate = MutableLiveData<changePwdState>()
    val changePwdValidate: LiveData<changePwdState> get() = _changePwdValidate

    fun validateField(
        newPwd: String,
        oldPwd: String,
    ) {
        val pwdState: changePwdState = if (TextUtils.isEmpty(newPwd) || newPwd.length < 6) {
            changePwdState(
                newPwd = R.string.please_enter_new_password
            )
        } else if (TextUtils.isEmpty(oldPwd) || oldPwd.length < 6) {
            changePwdState(confirmPwd = R.string.please_enter_confirm_password)
        } else if (newPwd.trim() != oldPwd.trim()) {
            changePwdState(pwdNotMatch = R.string.password_do_not_match)
        } else {
            changePwdState(hasDoneValidate = true)
        }
        _changePwdValidate.value = pwdState

    }

    private var _submitChangePasswordLoadingMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _submitChangePasswordMutableLiveData: MutableLiveData<ApiResWraper<JsonElement>> =
        MutableLiveData<ApiResWraper<JsonElement>>()

    val submitChangePasswordLoadingMutableLiveData
        get() =
            _submitChangePasswordLoadingMutableLiveData
    val submitChangePasswordMutableLiveData
        get() =
            _submitChangePasswordMutableLiveData

    fun submitChangePwd(requestBody: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<JsonElement>> =
            repository.submitChangePwd(requestBody)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<JsonElement>> {
            override fun onLoading(hasLoading: Boolean) {
                _submitChangePasswordLoadingMutableLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<JsonElement>) {
                if (respondData.success) {
                    _submitChangePasswordMutableLiveData.value = respondData
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
     * update user
     * */


    fun updateUser(requestBody: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<User>> =
            repository.updateUserInfo(requestBody)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<User>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserInfoLiveData.value = hasLoading
            }

            override fun onData(respondData: ApiResWraper<User>) {
                if (respondData.success) {
                    _dataUserInLiveData.value = respondData
                }
            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject
            ) {
                _dataUserInLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }
        })

    }
}


