package com.eazy.daiku.utility.pagin

import androidx.paging.PageKeyedDataSource
import java.util.HashMap

class BasePaginationDataSource<V>(
    private var queryKey: HashMap<String, Any>,
    private var iPaginationListener: (HashMap<String, Any>, LoadInitialParams<Int>, LoadInitialCallback<Int, V>) -> Unit
) : PageKeyedDataSource<Int, V>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, V>
    ) {
        iPaginationListener.invoke(queryKey, params, callback)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        TODO("Not yet implemented")
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        TODO("Not yet implemented")
    }
}