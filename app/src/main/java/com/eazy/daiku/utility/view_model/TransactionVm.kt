package com.eazy.daiku.utility.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.eazy.daiku.data.model.MyTransaction
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.TransactionRespond
import com.eazy.daiku.data.repository.Repository
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.pagin.transaction.TransactionPaginDataSource
import com.eazy.daiku.utility.pagin.transaction.TransactionPaginDataSourceFactory

import java.util.HashMap
import javax.inject.Inject

class TransactionVm
@Inject constructor(
    private val context: Context,
    override val repository: Repository
) : BaseViewModel(context, repository) {

    private var _loadingMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _loadingPaginationMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    private var _hasTrxListsMutableLiveData: MutableLiveData<Int> =
        MutableLiveData<Int>()
    private var _errorPaginationMutableLiveData: MutableLiveData<ApiResWraper<TransactionRespond>> =
        MutableLiveData<ApiResWraper<TransactionRespond>>()

    private var _totalBalanceMutableLiveData: MutableLiveData<TransactionRespond> =
        MutableLiveData<TransactionRespond>()


    val loadingMutableLiveData: LiveData<Boolean> get() = _loadingMutableLiveData

    val loadingPaginationMutableLiveData: LiveData<Boolean>
        get() =
            _loadingPaginationMutableLiveData

    val hasTrxListsMutableLiveData: LiveData<Int>
        get() =
            _hasTrxListsMutableLiveData

    val errorPaginationMutableLiveData: LiveData<ApiResWraper<TransactionRespond>>
        get() =
            _errorPaginationMutableLiveData

    val totalBalanceMutableLiveData: LiveData<TransactionRespond>
        get() =
            _totalBalanceMutableLiveData

    fun fetchTrx(
        bodyQuery: HashMap<String, Any>
    ): LiveData<PagedList<MyTransaction>> {
        val pagedListConfig = EazyTaxiHelper.configurePagedListConfig()
        val trxDataSourceFactory = TransactionPaginDataSourceFactory(
            context,
            this,
            bodyQuery
        )

        Transformations.switchMap(
            trxDataSourceFactory.trxDataSourceMutableLiveData,
            TransactionPaginDataSource::loadingMutableLiveData
        ).observeForever {
            _loadingMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            trxDataSourceFactory.trxDataSourceMutableLiveData,
            TransactionPaginDataSource::loadingPaginationMutableLiveData
        ).observeForever {
            _loadingPaginationMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            trxDataSourceFactory.trxDataSourceMutableLiveData,
            TransactionPaginDataSource::hasTrxListsMutableLiveData
        ).observeForever {
            _hasTrxListsMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            trxDataSourceFactory.trxDataSourceMutableLiveData,
            TransactionPaginDataSource::errorPaginationMutableLiveData
        ).observeForever {
            _errorPaginationMutableLiveData.postValue(it)
        }

        Transformations.switchMap(
            trxDataSourceFactory.trxDataSourceMutableLiveData,
            TransactionPaginDataSource::totalBalanceMutableLiveData
        ).observeForever {
            _totalBalanceMutableLiveData.postValue(it)
        }

        return LivePagedListBuilder(
            trxDataSourceFactory, pagedListConfig
        ).build()

    }

}