package com.eazy.daiku.utility.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import  com.eazy.daiku.data.model.base.ApiResWraper
import  com.eazy.daiku.data.model.server_model.History
import  com.eazy.daiku.data.repository.Repository
import  com.eazy.daiku.utility.EazyTaxiHelper
import  com.eazy.daiku.utility.base.BaseViewModel
import  com.eazy.daiku.utility.pagin.history.HistoryPaginDataSource
import  com.eazy.daiku.utility.pagin.history.HistoryPaginDataSourceFactory
import java.util.HashMap
import javax.inject.Inject

class HistoryVm @Inject constructor(
    private val context: Context,
    override val repository: Repository
) : BaseViewModel(context, repository) {

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


    fun fetchHistory(
        bodyQuery: HashMap<String, Any>
    ): LiveData<PagedList<History>> {
        val pagedListConfig = EazyTaxiHelper.configurePagedListConfig()
        val historyDataSourceFactory = HistoryPaginDataSourceFactory(
            context,
            this,
            bodyQuery
        )

        Transformations.switchMap(
            historyDataSourceFactory.historyDataSourceMutableLiveData,
            HistoryPaginDataSource::loadingMutableLiveData
        ).observeForever {
            _loadingMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            historyDataSourceFactory.historyDataSourceMutableLiveData,
            HistoryPaginDataSource::loadingPaginationMutableLiveData
        ).observeForever {
            _loadingPaginationMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            historyDataSourceFactory.historyDataSourceMutableLiveData,
            HistoryPaginDataSource::hasHistoryListsMutableLiveData
        ).observeForever {
            _hasHistoryListsMutableLiveData.postValue(it)
        }


        Transformations.switchMap(
            historyDataSourceFactory.historyDataSourceMutableLiveData,
            HistoryPaginDataSource::errorPaginationMutableLiveData
        ).observeForever {
            _errorPaginationMutableLiveData.postValue(it)
        }


        return LivePagedListBuilder(
            historyDataSourceFactory, pagedListConfig
        ).build()

    }

}