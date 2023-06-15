package com.eazy.daiku.utility.pagin.history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.eazy.daiku.data.model.server_model.History
import com.eazy.daiku.utility.base.BaseViewModel


class HistoryPaginDataSourceFactory(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>,
) : DataSource.Factory<Int, History>() {

    private var _historyDataSourceMutableLiveData: MutableLiveData<HistoryPaginDataSource> =
        MutableLiveData<HistoryPaginDataSource>()
    val historyDataSourceMutableLiveData: LiveData<HistoryPaginDataSource>
        get() =
            _historyDataSourceMutableLiveData

    override fun create(): DataSource<Int, History> {
        val trxDataSource = HistoryPaginDataSource(
            context,
            baseViewModel,
            queryKey
        )
        _historyDataSourceMutableLiveData.postValue(trxDataSource)
        return trxDataSource
    }

}