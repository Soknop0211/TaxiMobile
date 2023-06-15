package com.eazy.daiku.utility.pagin.community_taxi

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.CommunityTaxiRespond
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import java.util.HashMap

class CommunityTaxiPaginDataSource(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>
) : PageKeyedDataSource<Int, CommunityTaxiRespond>() {

    private var _communityTaxiLoadingMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val communityTaxiLoadingMutableLiveData: LiveData<Boolean> get() = _communityTaxiLoadingMutableLiveData

    private var _communityTaxiLoadingPaginationMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val communityTaxiLoadingPaginationMutableLiveData: LiveData<Boolean> get() = _communityTaxiLoadingPaginationMutableLiveData

    private var _hasCommunityTaxiListsMutableLiveData: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val hasCommunityTaxiListsMutableLiveData: LiveData<Int> get() = _hasCommunityTaxiListsMutableLiveData

    private var _communityTaxiErrorPaginationMutableLiveData: MutableLiveData<ApiResWraper<List<CommunityTaxiRespond>>> =
        MutableLiveData<ApiResWraper<List<CommunityTaxiRespond>>>()
    val communityTaxiErrorPaginationMutableLiveData: LiveData<ApiResWraper<List<CommunityTaxiRespond>>> get() = _communityTaxiErrorPaginationMutableLiveData

    private var _communityTaxiListsMutableLiveData: MutableLiveData<List<CommunityTaxiRespond>> =
        MutableLiveData<List<CommunityTaxiRespond>>()
    val communityTaxiListsMutableLiveData: LiveData<List<CommunityTaxiRespond>> get() = _communityTaxiListsMutableLiveData


    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, CommunityTaxiRespond>
    ) {
        val requestFlow: Flow<ApiResWraper<List<CommunityTaxiRespond>>> =
            baseViewModel.repository.fetchCommunityTaxi(queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<List<CommunityTaxiRespond>>> {
                override fun onLoading(hasLoading: Boolean) {
                    _communityTaxiLoadingMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<List<CommunityTaxiRespond>>) {
                    val key = queryKey["page"]
                    val keyInt = key?.toString()?.toInt() ?: 1
                    val updateKey = keyInt + 1
                    queryKey["page"] = updateKey
                    respondData.data?.let {
                        callback.onResult(
                            it, null, updateKey
                        )
                        _hasCommunityTaxiListsMutableLiveData.postValue(it.size)
                        _communityTaxiListsMutableLiveData.postValue(it)
                    }
                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject
                ) {
                    _communityTaxiErrorPaginationMutableLiveData.postValue(
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
        callback: LoadCallback<Int, CommunityTaxiRespond>
    ) {
        val requestFlow: Flow<ApiResWraper<List<CommunityTaxiRespond>>> =
            baseViewModel.repository.fetchCommunityTaxi(queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<List<CommunityTaxiRespond>>> {
                override fun onLoading(hasLoading: Boolean) {
                    _communityTaxiLoadingPaginationMutableLiveData.postValue(hasLoading)

                }

                override fun onData(respondData: ApiResWraper<List<CommunityTaxiRespond>>) {
                    val next = params.key + 1
                    queryKey["page"] = next

                    respondData.data?.let {
                        callback.onResult(it, next)
                        _hasCommunityTaxiListsMutableLiveData.postValue(it.size)
                        _communityTaxiListsMutableLiveData.postValue(it)
                    }
                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject
                ) {
                    _communityTaxiErrorPaginationMutableLiveData.postValue(
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

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, CommunityTaxiRespond>
    ) {

    }

}