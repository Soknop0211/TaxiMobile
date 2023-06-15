package com.eazy.daiku.utility.pagin.transaction

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.eazy.daiku.data.model.MyTransaction
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.TransactionRespond
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.DateTimeHelper
import com.eazy.daiku.utility.base.BaseViewModel
import com.eazy.daiku.utility.call_back.IApiResWrapper
import com.google.gson.JsonObject

import kotlinx.coroutines.flow.Flow
import java.util.*

class TransactionPaginDataSource(
    private var context: Context,
    private var baseViewModel: BaseViewModel,
    private var queryKey: HashMap<String, Any>
) : PageKeyedDataSource<Int, MyTransaction>() {

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


    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, MyTransaction>
    ) {
        val requestFlow: Flow<ApiResWraper<TransactionRespond>> =
            baseViewModel.repository.fetchTransaction(queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<TransactionRespond>> {
                override fun onLoading(hasLoading: Boolean) {
                    _loadingMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<TransactionRespond>) {
                    val key = queryKey["page"]
                    val keyInt = key?.toString()?.toInt() ?: 1
                    val updateKey = keyInt + 1
                    queryKey["page"] = updateKey
                    respondData.data?.let {
                        callback.onResult(
                            migrateTrx(it), null, updateKey
                        )
                        _hasTrxListsMutableLiveData.postValue(it.transactions.size)
                        _totalBalanceMutableLiveData.postValue(it)
                    }

                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject
                ) {

                    _errorPaginationMutableLiveData.postValue(
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
        callback: LoadCallback<Int, MyTransaction>
    ) {
    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, MyTransaction>
    ) {

        val requestFlow: Flow<ApiResWraper<TransactionRespond>> =
            baseViewModel.repository.fetchTransaction(queryKey)
        baseViewModel.submit(
            requestFlow,
            object : IApiResWrapper<ApiResWraper<TransactionRespond>> {
                override fun onLoading(hasLoading: Boolean) {
                    _loadingPaginationMutableLiveData.postValue(hasLoading)
                }

                override fun onData(respondData: ApiResWraper<TransactionRespond>) {
                    val next = params.key + 1
                    queryKey["page"] = next

                    respondData.data?.let {
                        callback.onResult(migrateTrx(it), next)
                        _totalBalanceMutableLiveData.postValue(it)
                    }
                }

                override fun onError(
                    message: String,
                    code: Int,
                    errorHashMap: JsonObject
                ) {
                    _errorPaginationMutableLiveData.postValue(
                        ApiResWraper<TransactionRespond>(
                            code,
                            message,
                            false,
                            errorHashMap,
                        )
                    )
                }
            })
    }

    private val mapDateHeaderHm = HashMap<String, String>()
    private val totalAmountByDate = HashMap<String, Double>()

    private fun migrateTrx(trxRespond: TransactionRespond): ArrayList<MyTransaction> {
        val newTrxs = ArrayList<MyTransaction>()
        var totalAmount = 0.00
        trxRespond.transactions.forEach {
            val myTrx = MyTransaction(
                it.bookingCode,
                it.kessTransactionId,
                it.payBy,
                it.total,
                it.walletTransactionStatus,
                it.createdAt,
                trxRespond.totalBalance,
                trxRespond.blockedBalance,
            )

            val createDate = it.createdAt ?: ""
            val createDateStr = DateTimeHelper.dateFm(
                createDate,
                "yyyy-MM-dd HH:mm:ss",
                "EEEE, dd MMMM yyyy"
            )
            val hasNewDay = !mapDateHeaderHm.containsKey(createDateStr)
            if (hasNewDay) {
                mapDateHeaderHm[createDateStr] = createDateStr
                myTrx.showHeaderDate = true
                totalAmount = 0.0
            }
            if (it.walletTransactionStatus?.lowercase(Locale.getDefault()) == Config.HistoryStatus.Received) {
                totalAmount += it.total ?: 0.00
            }
            totalAmountByDate[createDateStr] = totalAmount
            myTrx.totalAmountByDate = totalAmountByDate
            myTrx.mUserWithdraw = it.userWithdraw
            myTrx.type = it.type

            newTrxs.add(myTrx)
        }
        return newTrxs
    }

}

