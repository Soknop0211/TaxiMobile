package com.eazy.daiku.ui.history

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity

import com.eazy.daiku.utility.pagin.history.HistoryPaginAdapter
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.HistoryVm
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.databinding.ActivityHistoryTripBinding
import com.eazy.daiku.utility.QRCodeHelper
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.pagin.customer_history.CustomerHistoryPaginAdapter
import com.eazy.daiku.utility.view_model.CustomerHistoryVm
import com.google.gson.Gson

class HistoryTripActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryTripBinding
    private lateinit var historyAdapter: HistoryPaginAdapter
    private lateinit var customerAdapter: CustomerHistoryPaginAdapter
    private val historyVm: HistoryVm by viewModels {
        factory
    }

    private val customerHistoryVm: CustomerHistoryVm by viewModels {
        factory
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initObserved()
        doAction()
        initData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun initObserved() {
        historyVm.loadingMutableLiveData.observe(this) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        historyVm.loadingPaginationMutableLiveData.observe(this) {
            binding.swipeRefresh.isRefreshing = it
        }
        historyVm.hasHistoryListsMutableLiveData.observe(this) { size ->
            binding.noDataFoundView.root.visibility = if (size == 0) View.VISIBLE else View.GONE
            binding.historyRecyclerView.visibility = if (size > 0) View.VISIBLE else View.GONE
        }
        historyVm.errorPaginationMutableLiveData.observe(this) {
            if (!it.success) {
                globalShowError(it.message)
            }
        }


        customerHistoryVm.customerLoadingMutableLiveData.observe(this) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        customerHistoryVm.customerLoadingPaginationMutableLiveData.observe(this) {
            binding.swipeRefresh.isRefreshing = it
        }
        customerHistoryVm.customerHasHistoryListsMutableLiveData.observe(this) { size ->
            binding.noDataFoundView.root.visibility = if (size == 0) View.VISIBLE else View.GONE
            binding.historyRecyclerView.visibility = if (size > 0) View.VISIBLE else View.GONE
        }
        customerHistoryVm.customerErrorPaginationMutableLiveData.observe(this) {
            if (!it.success) {
                globalShowError(it.message)
            }
        }

        binding.appBar.qrCode.visibility = View.GONE

    }

    private fun doAction() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            initData()
        }

        historyAdapter.selectRowTrx = {
            RedirectClass.gotoMapPreviewActivity(
                self(),
                GsonConverterHelper.convertGenericClassToJson(it),
                true
            )
        }

        customerAdapter.selectRowTrx = {
            RedirectClass.gotoMapPreviewActivity(
                self(),
                GsonConverterHelper.convertGenericClassToJson(it),
                true
            )
        }

        customerAdapter.selectRowQr = {

            MessageUtils.showQrCodeDestination(
                self(),
                it.meta?.qrcodeurl.toString() ,
                it.service?.title.toString()
            )

        }


    }

    private fun initData() {
        val bodyQuery = HashMap<String, Any>()
        bodyQuery["page"] = 1
        bodyQuery["limit"] = 20
        if (BuildConfig.IS_CUSTOMER) {
            val customerHistory = customerHistoryVm.submitCustomerHistory(bodyQuery)
            customerHistory.observe(this) {
                customerAdapter.submitList(it)
            }
        } else {
            val historys = historyVm.fetchHistory(bodyQuery)
            historys.observe(this) {
                historyAdapter.submitList(it)
            }
        }
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            if (BuildConfig.IS_CUSTOMER) getString(R.string.activity) else getString(R.string.history),
            true
        )

        historyAdapter = HistoryPaginAdapter()
        customerAdapter = CustomerHistoryPaginAdapter()
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                self()
            )
            hasFixedSize()
            setItemViewCacheSize(1000)
            adapter = if (BuildConfig.IS_CUSTOMER) {
                customerAdapter
            } else {
                historyAdapter
            }

        }


    }


}