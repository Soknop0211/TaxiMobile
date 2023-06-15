package com.eazy.daiku.utility.pagin.history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import  com.eazy.daiku.data.model.base.ApiResWraper
import  com.eazy.daiku.data.model.server_model.History
import  com.eazy.daiku.utility.base.BaseViewModel
import  com.eazy.daiku.utility.call_back.IApiResWrapper
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import java.util.*

class HistoryPaginDataSource(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>,
) : PageKeyedDataSource<Int, History>() {

    private var _loadingMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _loadingPaginationMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _hasHistoryListsMutableLiveData: MutableLiveData<Int> =
        MutableLiveData<Int>()
    private var _errorPaginationMutableLiveData: MutableLiveData<ApiResWraper<List<History>>> =
        MutableLiveData<ApiResWraper<List<History>>>()

    val loadingMutableLiveData: LiveData<Boolean> get() = _loadingMutableLiveData

    val loadingPaginationMutableLiveData: LiveData<Boolean>
        get() =
            _loadingPaginationMutableLiveData

    val hasHistoryListsMutableLiveData: LiveData<Int>
        get() =
            _hasHistoryListsMutableLiveData

    val errorPaginationMutableLiveData: LiveData<ApiResWraper<List<History>>>
        get() =
            _errorPaginationMutableLiveData

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, History>,
    ) {
        val requestFlow: Flow<ApiResWraper<List<History>>> =
            baseViewModel.repository.fetchHistory(
                queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<List<History>>> {
                override fun onLoading(hasLoading: Boolean) {
                    _loadingMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<List<History>>) {
                    val key = queryKey["page"]
                    val keyInt = key?.toString()?.toInt() ?: 1
                    val updateKey = keyInt + 1
                    queryKey["page"] = updateKey
                    respondData.data?.let {
                        callback.onResult(
                            it, null, updateKey
                        )
                        _hasHistoryListsMutableLiveData.postValue(it.size)
                    }

                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject,
                ) {

                    _errorPaginationMutableLiveData.postValue(
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
        callback: LoadCallback<Int, History>,
    ) {

    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, History>,
    ) {
        val requestFlow: Flow<ApiResWraper<List<History>>> =
            baseViewModel.repository.fetchHistory(queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<List<History>>> {
                override fun onLoading(hasLoading: Boolean) {
                    _loadingPaginationMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<List<History>>) {
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
                    _errorPaginationMutableLiveData.postValue(
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

