package com.eazy.daiku.ui.customer.map

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivitySearchMapBinding
import com.eazy.daiku.ui.customer.adapter.HistorySearMapAdapter
import com.eazy.daiku.ui.customer.adapter.SearchMapAdapter
import com.eazy.daiku.ui.customer.model.SearchMapHistoryModel
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity

class SearchMapActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchMapBinding
    private var userId = -100
    private val viewModel: ProcessCustomerBookingViewModel by viewModels {
        factory
    }
    private lateinit var searchMapAdapter: SearchMapAdapter
    private lateinit var listSearchMapHistory: ArrayList<SearchMapHistoryModel>
    private lateinit var historySearMapAdapter: HistorySearMapAdapter

    companion object {
        var geometyModelKey = "geometyModelKey"
        var keySearch = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initAction()
        initObserver()
        initSearchHistoryMap()

    }

    private fun initView() {
        if (!TextUtils.isEmpty(keySearch) && keySearch == Config.KeySearch.pointA) {
            binding.containerCurrentGps.visibility = View.VISIBLE
            binding.containerSetPointOnMap.visibility = View.VISIBLE
        } else if (!TextUtils.isEmpty(keySearch) && keySearch == Config.KeySearch.pointB) {
            binding.containerCurrentGps.visibility = View.GONE
            binding.containerSetPointOnMap.visibility = View.VISIBLE
        } else {
            binding.containerCurrentGps.visibility = View.GONE
            binding.containerSetPointOnMap.visibility = View.GONE
        }
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.search_location),
            true
        )
        val user = getUserUserToSharePreference()
        user?.id?.let {
            userId = it
        }

        listSearchMapHistory = ArrayList()
        searchMapAdapter = SearchMapAdapter()
        historySearMapAdapter = HistorySearMapAdapter()
    }

    private fun initAction() {
        binding.edSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!TextUtils.isEmpty(binding.edSearch.text.toString())) {
                        saveTextSearchMap()
                        viewModel.submitSearchMapLocal(binding.edSearch.text.toString())
                    }
                    return true
                }
                return false
            }
        })

        searchMapAdapter.onSelectRowSearchMap = { gemoety ->
            val intent = Intent()
            intent.putExtra(geometyModelKey, GsonConverterHelper.convertGenericClassToJson(gemoety))
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.actionSearch.setOnClickListener {
            if (!TextUtils.isEmpty(binding.edSearch.text.toString())) {
                saveTextSearchMap()
                viewModel.submitSearchMapLocal(binding.edSearch.text.toString())
            }
        }

        historySearMapAdapter.onSelectRowHistorySearchMap = { data, hasDelete ->
            if (hasDelete) {
                val newListHistory = ArrayList<SearchMapHistoryModel>()
                newListHistory.addAll(listSearchMapHistory)
                if (listSearchMapHistory.size > 0) {
                    for (history in listSearchMapHistory) {
                        if (history.searchTitle == data.searchTitle) {
                            newListHistory.remove(history)
                        }
                    }
                }
                GsonConverterHelper.saveListSearchHistoryMap(self(), newListHistory)
                initSearchHistoryMap()
            } else {
                binding.edSearch.setText(data.searchTitle)
                data.searchTitle?.let {
                    viewModel.submitSearchMapLocal(it)
                }
            }
        }

        binding.containerCurrentGps.setOnClickListener {
            val intent = Intent()
            if (!TextUtils.isEmpty(keySearch) && keySearch == Config.KeySearch.pointA) {
                intent.putExtra(Config.KeySearch.pointA, Config.KeySearch.pointA)
                intent.putExtra(Config.KeySearch.currentGPS, Config.KeySearch.currentGPS)
            }
            setResult(RESULT_OK, intent)
            finish()
        }



        binding.containerSetPointOnMap.setOnClickListener {
            val intent = Intent()
            if (!TextUtils.isEmpty(keySearch) && keySearch == Config.KeySearch.pointA) {
                intent.putExtra(Config.KeySearch.pointA, Config.KeySearch.pointA)
                intent.putExtra(Config.KeySearch.setPointOnMapA, Config.KeySearch.setPointOnMapA)
            } else if (!TextUtils.isEmpty(keySearch) && keySearch == Config.KeySearch.pointB) {
                intent.putExtra(Config.KeySearch.pointB, Config.KeySearch.pointB)
                intent.putExtra(Config.KeySearch.setPointOnMapB, Config.KeySearch.setPointOnMapB)
            }
            setResult(RESULT_OK,intent)
            finish()
        }
    }

    private fun initSearchHistoryMap() {
        val mySearchHistoryMapJson = EazyTaxiHelper.getSearchHistoryMap(self())
        listSearchMapHistory = GsonConverterHelper.getListMySearchHistoryMap(mySearchHistoryMapJson)
        switchView(false, listSearchMapHistory.size > 0)
        val newListHistory = ArrayList<SearchMapHistoryModel>()
        if (listSearchMapHistory.size > 0) {
            for (history in listSearchMapHistory) {
                if (history.id == userId) {
                    newListHistory.add(history)
                }
            }
        }
        newListHistory.reverse()
        historySearMapAdapter.add(newListHistory)
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = historySearMapAdapter
        }
    }

    private fun initObserver() {
        viewModel.loadingSearchMapLiveData.observe(self()) { haseLoading ->
            binding.searchMapLoading.root.visibility = if (haseLoading) View.VISIBLE else View.GONE
        }
        viewModel.dataSearchMapLiveData.observe(self()) {
            if (it != null && it.success == true && it.results.size > 0) {
                switchView(haveDataSearch = true, haveHistory = false)
                searchMapAdapter.add(it.results)
                binding.searchMapRecyclerView.apply {
                    layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    adapter = searchMapAdapter
                }
            } else {
                switchView(haveDataSearch = false, haveHistory = false)
            }

        }

        viewModel.loadingSearchMapLocalLiveData.observe(self()) { haseLoading ->
            binding.searchMapLoading.root.visibility = if (haseLoading) View.VISIBLE else View.GONE
        }

        viewModel.dataSearchMapLocalLiveData.observe(self()) {
            if (it != null && it.success == true && it.results.size > 0) {
                switchView(haveDataSearch = true, haveHistory = false)
                searchMapAdapter.add(it.results)
                binding.searchMapRecyclerView.apply {
                    layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    adapter = searchMapAdapter
                }
            } else {
                viewModel.submitSearchMap(binding.edSearch.text.toString())
            }
        }
    }

    private fun switchView(haveDataSearch: Boolean, haveHistory: Boolean) {
        binding.searchMapRecyclerView.visibility =
            if (haveDataSearch && !haveHistory) View.VISIBLE else View.GONE
        binding.noDataFound.visibility =
            if (haveDataSearch || haveHistory) View.GONE else View.VISIBLE
        binding.historyRecyclerView.visibility = if (haveHistory) View.VISIBLE else View.GONE

    }

    private fun saveTextSearchMap() {
        var searchText = binding.edSearch.text.toString()
        val newHistoryList = ArrayList<SearchMapHistoryModel>()
        newHistoryList.addAll(listSearchMapHistory)
        if (listSearchMapHistory.size > 0) {
            for (history in listSearchMapHistory) {
                if (!TextUtils.isEmpty(history.searchTitle)) {
                    if (binding.edSearch.text.toString() == history.searchTitle) {
                        newHistoryList.remove(history)
                        searchText = binding.edSearch.text.toString()
                    }
                }
            }
        } else if (listSearchMapHistory.size == 0) {
            searchText = binding.edSearch.text.toString()
        }
        newHistoryList.add(SearchMapHistoryModel(userId, searchText))
        GsonConverterHelper.saveListSearchHistoryMap(self(), newHistoryList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}