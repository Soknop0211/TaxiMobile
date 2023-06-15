package com.eazy.daiku.utility.pagin.customer_history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import  com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.CustomerHistoryModel
import  com.eazy.daiku.utility.base.BaseViewModel
import  com.eazy.daiku.utility.call_back.IApiResWrapper
import com.eazy.daiku.utility.other.AppLOGG
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import java.util.*

class CustomerHistoryPaginDataSource(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>,
) : PageKeyedDataSource<Int, CustomerHistoryModel>() {

    private var _customerLoadingMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _customerLoadingPaginationMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _customerHasHistoryListsMutableLiveData: MutableLiveData<Int> =
        MutableLiveData<Int>()
    private var _customerErrorPaginationMutableLiveData: MutableLiveData<ApiResWraper<List<CustomerHistoryModel>>> =
        MutableLiveData<ApiResWraper<List<CustomerHistoryModel>>>()

    val customerLoadingMutableLiveData: LiveData<Boolean> get() = _customerLoadingMutableLiveData

    val customerLoadingPaginationMutableLiveData: LiveData<Boolean>
        get() =
            _customerLoadingPaginationMutableLiveData

    val customerHasHistoryListsMutableLiveData: LiveData<Int>
        get() =
            _customerHasHistoryListsMutableLiveData

    val customerErrorPaginationMutableLiveData: LiveData<ApiResWraper<List<CustomerHistoryModel>>>
        get() =
            _customerErrorPaginationMutableLiveData

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, CustomerHistoryModel>,
    ) {
        val requestFlow: Flow<ApiResWraper<List<CustomerHistoryModel>>> =
            baseViewModel.repository.submitCustomerHistory(
                queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<List<CustomerHistoryModel>>> {
                override fun onLoading(hasLoading: Boolean) {
                    _customerLoadingMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<List<CustomerHistoryModel>>) {
                    val key = queryKey["page"]
                    val keyInt = key?.toString()?.toInt() ?: 1
                    val updateKey = keyInt + 1
                    queryKey["page"] = updateKey
                    respondData.data?.let {
                        callback.onResult(
                            it, null, updateKey
                        )
                        _customerHasHistoryListsMutableLiveData.postValue(it.size)
                    }
                    AppLOGG.d("DATACUSTOMERHISTORY", "data-->" + Gson().toJson(respondData.data))

                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject,
                ) {

                    _customerErrorPaginationMutableLiveData.postValue(
                        ApiResWraper(
                            code,
                            message,
                            false,
                            errorHashMap,
                        )
                    )
                }
            })

    }

    override fun loadBefore(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, CustomerHistoryModel>,
    ) {

    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, CustomerHistoryModel>,
    ) {
        val requestFlow: Flow<ApiResWraper<List<CustomerHistoryModel>>> =
            baseViewModel.repository.submitCustomerHistory(queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<List<CustomerHistoryModel>>> {
                override fun onLoading(hasLoading: Boolean) {
                    _customerLoadingPaginationMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<List<CustomerHistoryModel>>) {
                    val next = params.key + 1
                    queryKey["page"] = next
                    respondData.data?.let {
                        callback.onResult(it, next)
                    }
                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject,
                ) {
                    _customerErrorPaginationMutableLiveData.postValue(
                        ApiResWraper(
                            code,
                            message,
                            false,
                            errorHashMap,
                        )
                    )
                }
            })
    }

}

