package com.eazy.daiku.utility.pagin.community_taxi

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.eazy.daiku.data.model.server_model.CommunityTaxiRespond
import com.eazy.daiku.utility.base.BaseViewModel

class CommunityTaxiPaginDataSourceFactory(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>,
) : DataSource.Factory<Int, CommunityTaxiRespond>() {

    private var _communityTaxiDataSourceMutableLiveData: MutableLiveData<CommunityTaxiPaginDataSource> =
        MutableLiveData<CommunityTaxiPaginDataSource>()
    val communityTaxiDataSourceMutableLiveData: LiveData<CommunityTaxiPaginDataSource>
        get() =
            _communityTaxiDataSourceMutableLiveData

    override fun create(): DataSource<Int, CommunityTaxiRespond> {
        val communityTaxiDataSource = CommunityTaxiPaginDataSource(
            context,
            baseViewModel,
            queryKey
        )
        _communityTaxiDataSourceMutableLiveData.postValue(communityTaxiDataSource)
        return communityTaxiDataSource
    }

}