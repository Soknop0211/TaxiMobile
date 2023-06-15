package com.eazy.daiku.utility.pagin.customer_history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.eazy.daiku.data.model.server_model.CustomerHistoryModel
import com.eazy.daiku.utility.base.BaseViewModel


class CustomerHistoryPaginDataSourceFactory(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>,
) : DataSource.Factory<Int, CustomerHistoryModel>() {

    private var _customerHistoryDataSourceMutableLiveData: MutableLiveData<CustomerHistoryPaginDataSource> =
        MutableLiveData<CustomerHistoryPaginDataSource>()
    val customerHistoryDataSourceMutableLiveData: LiveData<CustomerHistoryPaginDataSource>
        get() =
            _customerHistoryDataSourceMutableLiveData

    override fun create(): DataSource<Int, CustomerHistoryModel> {
        val trxDataSource = CustomerHistoryPaginDataSource(
            context,
            baseViewModel,
            queryKey
        )
        _customerHistoryDataSourceMutableLiveData.postValue(trxDataSource)
        return trxDataSource
    }

}