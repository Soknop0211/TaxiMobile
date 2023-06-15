package com.eazy.daiku.utility.view_model.user_case

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.eazy.daiku.BuildConfig
import  com.eazy.daiku.data.repository.Repository
import  com.eazy.daiku.utility.EazyTaxiHelper
import  com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.R
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.CreateUserRespond
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.utility.view_model.user_case.model.CreateUserState
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateUserViewModel
@Inject constructor(private val context: Context, override val repository: Repository) :
    BaseViewModel(context, repository) {

    private val _signupUserValidate = MutableLiveData<CreateUserState>()
    val signupUserValidate: LiveData<CreateUserState> get() = _signupUserValidate

    //validate field
    fun validateSignUpUserField(
        organizationCode: String,
        firstName: String,
        lastName: String,
        gender: String,
        dob: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String,
    ) {
        var createUserState: CreateUserState
        if (TextUtils.isEmpty(organizationCode) && !BuildConfig.IS_CUSTOMER) {
            createUserState = CreateUserState(R.string.organization_code_is_required)
        } else if (TextUtils.isEmpty(firstName)) {
            createUserState = CreateUserState(R.string.please_enter_your_first_name)
        } else if (TextUtils.isEmpty(lastName)) {
            createUserState = CreateUserState(R.string.please_enter_your_last_name)
        } else if (TextUtils.isEmpty(gender)) {
            createUserState = CreateUserState(R.string.please_select_gender)
        } else if (TextUtils.isEmpty(dob)) {
            createUserState = CreateUserState(R.string.please_select_dob)
        } else if (email.isNotEmpty() && !EazyTaxiHelper.isValidEmail(email)) {
            createUserState = CreateUserState(R.string.invalid_email_address)
        } else if (TextUtils.isEmpty(phoneNumber) ||
            phoneNumber.length < Constants.PhoneConfig.PHONE_MIN_LENGTH ||
            phoneNumber.length > Constants.PhoneConfig.PHONE_MAX_LENGTH ||
            !EazyTaxiHelper.phoneNumberValidate(phoneNumber)
        ) {
            createUserState = CreateUserState(R.string.please_enter_a_valid_mobile_number)
        } else if (TextUtils.isEmpty(password.trim())) {
            createUserState = CreateUserState(R.string.please_enter_password)
        } else if (password.trim().length < 6) {
            createUserState = CreateUserState(R.string.pin_code_must_6_digit)
        } else if (TextUtils.isEmpty(confirmPassword.trim())) {
            createUserState = CreateUserState(R.string.please_enter_confirm_password)
        } else if (confirmPassword.trim().length < 6) {
            createUserState = CreateUserState(R.string.pin_code_must_6_digit)
        } else if (password.trim() != confirmPassword.trim()) {
            createUserState = CreateUserState(R.string.password_do_not_match)
        } else {
            createUserState = CreateUserState(hasDoneValidate = true)
        }
        _signupUserValidate.value = createUserState
    }

    // TODO: ====================Start Create User Customer======================
    private var _loadingCreateUserCustomerLiveData = MutableLiveData<Boolean>()
    private var _dataCreateUserCustomerLiveData = MutableLiveData<ApiResWraper<CreateUserRespond>>()

    val loadingCreateUserCustomerLiveData: MutableLiveData<Boolean> get() = _loadingCreateUserCustomerLiveData
    val dataCreateUserCustomerLiveData: MutableLiveData<ApiResWraper<CreateUserRespond>> get() = _dataCreateUserCustomerLiveData

    fun submitCreateUserCustomer(body: HashMap<String, Any>) {
        val requesFlow: Flow<ApiResWraper<CreateUserRespond>> =
            repository.submitCreateUserCustomer(body)
        submit(requesFlow, object : IApiResWrapper<ApiResWraper<CreateUserRespond>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingCreateUserCustomerLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<CreateUserRespond>) {
                _dataCreateUserCustomerLiveData.postValue(respondData)

            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataCreateUserCustomerLiveData.postValue(
                    ApiResWraper(
                        code,
                        message,
                        false,
                        errorHashMap
                    )
                )

            }

        })

    }

}