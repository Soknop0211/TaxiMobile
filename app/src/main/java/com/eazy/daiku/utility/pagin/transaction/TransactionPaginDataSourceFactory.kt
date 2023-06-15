package com.eazy.daiku.utility.pagin.transaction

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.eazy.daiku.data.model.MyTransaction
import com.eazy.daiku.utility.base.BaseViewModel

class TransactionPaginDataSourceFactory(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>,
) : DataSource.Factory<Int, MyTransaction>() {

    private var _trxDataSourceMutableLiveData: MutableLiveData<TransactionPaginDataSource> =
        MutableLiveData<TransactionPaginDataSource>()
    val trxDataSourceMutableLiveData: LiveData<TransactionPaginDataSource>
        get() =
            _trxDataSourceMutableLiveData

    override fun create(): DataSource<Int, MyTransaction> {
        val trxDataSource = TransactionPaginDataSource(
            context,
            baseViewModel,
            queryKey
        )
        _trxDataSourceMutableLiveData.postValue(trxDataSource)
        return trxDataSource
    }

}