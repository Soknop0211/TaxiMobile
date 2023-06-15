package com.eazy.daiku.ui.wallet

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.eazy.daiku.data.model.server_model.TransactionRespond
import com.eazy.daiku.utility.AmountHelper
import com.eazy.daiku.utility.GsonConverterHelper
import  com.eazy.daiku.utility.base.BaseActivity
import  com.eazy.daiku.utility.bottom_sheet.TransactionDetailBtsFragment
import  com.eazy.daiku.utility.pagin.transaction.TransactionPaginAdapter
import  com.eazy.daiku.utility.redirect.RedirectClass
import  com.eazy.daiku.utility.view_model.TransactionVm
import com.eazy.daiku.databinding.ActivityMainWalletBinding
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.eazy.daiku.utility.view_model.withdraw.WithdrawViewModel
import com.google.gson.Gson
import java.util.*

class MainWalletActivity : BaseActivity() {

    private val intentFilter = IntentFilter()
    private var totalBalance = 0.00
    private var blockAmount = 0.00
    private var totalAmount = 0.00
    private lateinit var transactionRespond: TransactionRespond
    private lateinit var binding: ActivityMainWalletBinding
    private lateinit var trxAdapter: TransactionPaginAdapter
    private val transactionVm: TransactionVm by viewModels {
        factory
    }
    private val withdrawViewModel: WithdrawViewModel by viewModels {
        factory
    }

    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
        doAction()
        initObserved()
        initData()
    }

    private fun initData() {
        val bodyQuery = HashMap<String, Any>()
        bodyQuery["page"] = 1
        bodyQuery["limit"] = 20
        val listTrxs = transactionVm.fetchTrx(bodyQuery)
        listTrxs.observe(this) {
            trxAdapter.submitList(it)
        }
    }

    private fun initUi() {
        transactionRespond = TransactionRespond()
        trxAdapter = TransactionPaginAdapter()
        binding.trxRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                self()
            )
            adapter = trxAdapter
        }
    }

    private fun initObserved() {
        transactionVm.loadingMutableLiveData.observe(this) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        transactionVm.loadingPaginationMutableLiveData.observe(this) {
            binding.swipeRefresh.isRefreshing = it
        }
        transactionVm.hasTrxListsMutableLiveData.observe(this) { size ->
            binding.noDataFoundView.root.visibility = if (size == 0) View.VISIBLE else View.GONE
            binding.trxRecyclerView.visibility = if (size > 0) View.VISIBLE else View.GONE
        }
        transactionVm.errorPaginationMutableLiveData.observe(this) {
            if (!it.success) {
                globalShowError(it.message)
            }
        }

        transactionVm.totalBalanceMutableLiveData.observe(this) { transactionRespon ->
            transactionRespon?.let { transactionRespond ->
                this.transactionRespond = transactionRespond
            }
            binding.pendingAmountTv.text =
                transactionRespon?.let {
                    AmountHelper.amountFormat(
                        "$",
                        transactionRespon.blockedBalance ?: 0.00
                    )
                }
            transactionRespon?.let { transactionRespond ->
                transactionRespon.totalBalance?.let {
                    totalBalance = transactionRespon.totalBalance!!
                }
                transactionRespon.blockedBalance?.let {
                    blockAmount = transactionRespon.blockedBalance!!
                }
            }
            totalAmount = totalBalance - blockAmount

            binding.tvTotalBalance.text = AmountHelper.amountFormat(
                "$",
                totalAmount
            )
            binding.pendingAmountTv.text = AmountHelper.amountFormat(
                "$",
                blockAmount
            )

        }


        withdrawViewModel.loadingListAllBankLiveData.observe(self()) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        withdrawViewModel.dataListAllBankLiveData.observe(self()) {
            if (it != null && it.success) {
                RedirectClass.gotoWithdrawMoneyActivity(self(),
                    GsonConverterHelper.convertGenericClassToJson(transactionRespond),GsonConverterHelper.convertGenericClassToJson(it.data))
            } else {
                globalShowError(it.message)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(myBroadcastReceiver, intentFilter)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }

    private val myBroadcastReceiver = object : MyBroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {
            super.onReceive(p0, p1)
            if (p1 != null) {
                when {
                    p1.hasExtra(reloadMainWalletWhenWithdrawSuccessKey) -> {
                        initData()
                    }
                }
            }
        }

    }

    private fun doAction() {

        binding.actionBackImg.setOnClickListener {
            finish()
        }

        binding.actionWithdrawLinearLayout.setOnClickListener {
            withdrawViewModel.submitListAllBank()
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            initData()
        }

        trxAdapter.selectRowTrx = {
            val trxBts = TransactionDetailBtsFragment.newInstance(
                it
            )
            trxBts.show(supportFragmentManager, "TransactionDetailBtsFragment")
        }

    }


}