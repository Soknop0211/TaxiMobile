package com.eazy.daiku.utility.view_model.withdraw

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.eazy.daiku.data.model.ListAllBankAccountModel
import com.eazy.daiku.data.model.WithdrawMoneyRespondModel
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.data.model.VerifyBankAccountModel
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WithdrawViewModel @Inject constructor(
    private val context: Context,
    override val repository: Repository,
) : BaseViewModel(context, repository) {
    // TODO: ====================== Start Withdraw Money==========================
    private val _loadingWithdrawLiveData = MutableLiveData<Boolean>()
    private val _dataWithdrawLiveData = MutableLiveData<ApiResWraper<WithdrawMoneyRespondModel>>()

    val loadingWithdrawLiveData: MutableLiveData<Boolean> get() = _loadingWithdrawLiveData
    val dataWithdrawLiveData: MutableLiveData<ApiResWraper<WithdrawMoneyRespondModel>> get() = _dataWithdrawLiveData

    fun submitWithdraw(bodyMap: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<WithdrawMoneyRespondModel>> =
            repository.submitWithdraw(bodyMap)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<WithdrawMoneyRespondModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingWithdrawLiveData.value = hasLoading

            }

            override fun onData(respondData: ApiResWraper<WithdrawMoneyRespondModel>) {
                if (respondData.success) {
                    _dataWithdrawLiveData.value = respondData
                }

            }

            override fun onError(
                message: String,
                code: Int,
                errorHashMap: JsonObject,
            ) {
                _dataWithdrawLiveData.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }

        })
    }
    // TODO: ======================== End Withdraw Money=====================================


    // TODO: =======================Start Verify BankAccount==================================
    private val _loadingVerifyBankAccount = MutableLiveData<Boolean>()
    private val _dataRespondVerifyBankAccount =
        MutableLiveData<ApiResWraper<VerifyBankAccountModel>>()

    val loadingVerifyBankAccount: MutableLiveData<Boolean> get() = _loadingVerifyBankAccount
    val dataRespondVerifyBankAccount: MutableLiveData<ApiResWraper<VerifyBankAccountModel>> get() = _dataRespondVerifyBankAccount

    fun submitVerifyBankAccount(bodyMap: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<VerifyBankAccountModel>> =
            repository.submitVerifyBankAccount(bodyMap)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<VerifyBankAccountModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingVerifyBankAccount.value = hasLoading

            }

            override fun onData(respondData: ApiResWraper<VerifyBankAccountModel>) {
                _dataRespondVerifyBankAccount.value = respondData
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataRespondVerifyBankAccount.value = ApiResWraper(
                    code,
                    message,
                    false,
                    errorHashMap,
                )
            }

        })
    }
    // TODO: =======================End Verify BankAccount==================================

    // TODO: =======================Start List All BankAccount==================================
    private val _loadingListAllBankLiveData = MutableLiveData<Boolean>()
    private val _dataListAllBankLiveData =
        MutableLiveData<ApiResWraper<List<ListAllBankAccountModel>>>()

    val loadingListAllBankLiveData: MutableLiveData<Boolean> get() = _loadingListAllBankLiveData
    val dataListAllBankLiveData: MutableLiveData<ApiResWraper<List<ListAllBankAccountModel>>> get() = _dataListAllBankLiveData

    fun submitListAllBank() {
        val requestFlow: Flow<ApiResWraper<List<ListAllBankAccountModel>>> =
            repository.submitListAllBank()
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<List<ListAllBankAccountModel>>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingListAllBankLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<List<ListAllBankAccountModel>>) {
                _dataListAllBankLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataListAllBankLiveData.postValue(ApiResWraper(code, message, false, errorHashMap))
            }

        })
    }
    // TODO: =======================End List All BankAccount==================================


    // TODO: =======================Start user with draw==================================
    private val _loadingUserWithdrawLiveData = MutableLiveData<Boolean>()
    private val _dataUserWithdrawLiveData =
        MutableLiveData<ApiResWraper<WithdrawMoneyRespondModel>>()

    val loadingUserWithdrawLiveData: MutableLiveData<Boolean> get() = _loadingUserWithdrawLiveData
    val dataUserWithdrawLiveData: MutableLiveData<ApiResWraper<WithdrawMoneyRespondModel>> get() = _dataUserWithdrawLiveData

    fun userSubmitWithdraw(bodyMap: HashMap<String, Any>) {
        val requestFlow: Flow<ApiResWraper<WithdrawMoneyRespondModel>> =
            repository.userSubmitWithdraw(bodyMap)
        submit(requestFlow, object : IApiResWrapper<ApiResWraper<WithdrawMoneyRespondModel>> {
            override fun onLoading(hasLoading: Boolean) {
                _loadingUserWithdrawLiveData.postValue(hasLoading)
            }

            override fun onData(respondData: ApiResWraper<WithdrawMoneyRespondModel>) {
                _dataUserWithdrawLiveData.postValue(respondData)
            }

            override fun onError(message: String, code: Int, errorHashMap: JsonObject) {
                _dataUserWithdrawLiveData.postValue(ApiResWraper(code,
                    message,
                    false,
                    errorHashMap))
            }

        })
    }
    // TODO: =======================End user with draw==================================

}