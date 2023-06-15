package com.eazy.daiku.utility.view_model.community

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.CommunityTaxiRespond
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.pagin.community_taxi.CommunityTaxiPaginDataSource
import com.eazy.daiku.utility.pagin.community_taxi.CommunityTaxiPaginDataSourceFactory
import java.util.HashMap
import javax.inject.Inject

class CommunityVm
@Inject constructor(
    private val context: Context,
    override val repository: Repository
) : BaseViewModel(context, repository) {
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

    fun fetchCommunityTaxi(
        bodyQuery: HashMap<String, Any>
    ): LiveData<PagedList<CommunityTaxiRespond>> {
        val pagedListConfig = EazyTaxiHelper.configurePagedListConfig()
        val communityTaxiDataSourceFactory = CommunityTaxiPaginDataSourceFactory(
            context,
            this,
            bodyQuery
        )

        val communityTaxiPaginDataSource =
            communityTaxiDataSourceFactory.communityTaxiDataSourceMutableLiveData

        Transformations.switchMap(
            communityTaxiPaginDataSource,
            CommunityTaxiPaginDataSource::communityTaxiLoadingMutableLiveData
        ).observeForever {
            _communityTaxiLoadingMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            communityTaxiPaginDataSource,
            CommunityTaxiPaginDataSource::communityTaxiLoadingPaginationMutableLiveData
        ).observeForever {
            _communityTaxiLoadingPaginationMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            communityTaxiPaginDataSource,
            CommunityTaxiPaginDataSource::hasCommunityTaxiListsMutableLiveData
        ).observeForever {
            _hasCommunityTaxiListsMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            communityTaxiPaginDataSource,
            CommunityTaxiPaginDataSource::communityTaxiErrorPaginationMutableLiveData
        ).observeForever {
            _communityTaxiErrorPaginationMutableLiveData.postValue(it)
        }

        return LivePagedListBuilder(
            communityTaxiDataSourceFactory, pagedListConfig
        ).build()

    }




}
