package com.eazy.daiku.utility.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import  com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.CustomerHistoryModel
import  com.eazy.daiku.data.repository.Repository
import  com.eazy.daiku.utility.EazyTaxiHelper
import  com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.pagin.customer_history.CustomerHistoryPaginDataSource
import com.eazy.daiku.utility.pagin.customer_history.CustomerHistoryPaginDataSourceFactory
import java.util.HashMap
import javax.inject.Inject

class CustomerHistoryVm @Inject constructor(
    private val context: Context,
    override val repository: Repository,
) : BaseViewModel(context, repository) {

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


    fun submitCustomerHistory(
        bodyQuery: HashMap<String, Any>,
    ): LiveData<PagedList<CustomerHistoryModel>> {
        val pagedListConfig = EazyTaxiHelper.configurePagedListConfig()
        val customerHistoryDataSourceFactory = CustomerHistoryPaginDataSourceFactory(
            context,
            this,
            bodyQuery
        )

        Transformations.switchMap(
            customerHistoryDataSourceFactory.customerHistoryDataSourceMutableLiveData,
            CustomerHistoryPaginDataSource::customerLoadingMutableLiveData
        ).observeForever {
            _customerLoadingMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            customerHistoryDataSourceFactory.customerHistoryDataSourceMutableLiveData,
            CustomerHistoryPaginDataSource::customerLoadingPaginationMutableLiveData
        ).observeForever {
            _customerLoadingPaginationMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            customerHistoryDataSourceFactory.customerHistoryDataSourceMutableLiveData,
            CustomerHistoryPaginDataSource::customerHasHistoryListsMutableLiveData
        ).observeForever {
            _customerHasHistoryListsMutableLiveData.postValue(it)
        }


        Transformations.switchMap(
            customerHistoryDataSourceFactory.customerHistoryDataSourceMutableLiveData,
            CustomerHistoryPaginDataSource::customerErrorPaginationMutableLiveData
        ).observeForever {
            _customerErrorPaginationMutableLiveData.postValue(it)
        }


        return LivePagedListBuilder(
            customerHistoryDataSourceFactory, pagedListConfig
        ).build()

    }

}