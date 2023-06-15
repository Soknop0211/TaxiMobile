package com.eazy.daiku.utility.bottom_sheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.CommunityTaxiRespond
import com.eazy.daiku.databinding.ChooseCommunityTaxiLayoutBinding
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import com.eazy.daiku.utility.view_model.community.CommunityVm
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.HashMap
import kotlin.properties.Delegates

class ChooseCommunityBottomSheet : BaseBottomSheetDialogFragment() {
    private lateinit var binding: ChooseCommunityTaxiLayoutBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var communityTaxiVm: CommunityVm
    private lateinit var selectCommunityTaxListener: (CommunityTaxiRespond) -> Unit
    private var communityTaxiAdapter: CommunityTaxiAdapter? = null
    private var communityLiveData: LiveData<PagedList<CommunityTaxiRespond>>? = null
    private var communityTaxiTemporaryVm: CommunityVm? = null
    private var oldLists: PagedList<CommunityTaxiRespond>? = null
    private var termId by Delegates.notNull<Int>()

    companion object {
        @JvmStatic
        fun newInstance(
            communityTaxiVm: CommunityVm,
            communityTaxiTemporaryVm: CommunityVm,
            communityLiveData: LiveData<PagedList<CommunityTaxiRespond>>,
            termId: Int,
            selectCommunityTaxListener: (CommunityTaxiRespond) -> Unit
        ) =
            ChooseCommunityBottomSheet().apply {
                this.communityTaxiVm = communityTaxiVm
                this.communityLiveData = communityLiveData
                this.communityTaxiTemporaryVm = communityTaxiTemporaryVm
                this.termId = termId
                this.selectCommunityTaxListener = selectCommunityTaxListener
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onStart() {
        super.onStart()
        //this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooseCommunityTaxiLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserved()
        doAction()
        initData()

    }

    private fun initView() {
        communityTaxiAdapter = CommunityTaxiAdapter()
        binding.communityTextRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                fContext, RecyclerView.VERTICAL, false
            )
            adapter = communityTaxiAdapter
        }
    }

    private fun initObserved() {
        communityTaxiTemporaryVm?.hasCommunityTaxiListsMutableLiveData?.observe(fContext) {
            showEmptyData(it)
        }

        communityTaxiVm.communityTaxiLoadingMutableLiveData.observe(fContext) {
            binding.loadingSmallSearchCircularProgressIndicator.visibility =
                if (it) View.VISIBLE else View.GONE
        }
        communityTaxiVm.communityTaxiLoadingPaginationMutableLiveData.observe(fContext) {
            binding.loadingSmallPaginCircularProgressIndicator.visibility =
                if (it) View.VISIBLE else View.GONE
        }

        communityTaxiVm.communityTaxiErrorPaginationMutableLiveData.observe(fContext) {
            if (!it.success) {
                globalShowError(it.message)
            }
        }

    }

    private fun doAction() {
        val timer: CountDownTimer = object : CountDownTimer(500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {

            }
        }
        binding.searchEtd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                timer.cancel()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                timer.start()
            }
        })
        binding.searchEtd.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (binding.searchEtd.text.length >= 3) {
                    searchCommunityTaxi(binding.searchEtd.text.trim().toString())
                    EazyTaxiHelper.hideKeyboard(binding.searchEtd)
                }
                if (binding.searchEtd.text.isEmpty()) {
                    communityTaxiAdapter?.let {
                        it.submitList(oldLists)
                    }

                }
                true
            } else {
                false
            }
        }
        communityTaxiAdapter?.selectRow = {
            this.termId = it.id ?: -1
            selectCommunityTaxListener.invoke(it)
        }
        binding.actionCloseImg.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun initData() {
        communityLiveData?.observe(fContext) {
            communityTaxiAdapter?.submitData(it, termId)
            oldLists = it
        }
    }

    private fun searchCommunityTaxi(searchText: String) {
        communityLiveData = null
        communityTaxiTemporaryVm = null

        val bodyQuery = HashMap<String, Any>()
        bodyQuery["page"] = 1
        bodyQuery["limit"] = 50
        bodyQuery["search"] = searchText.trim()
        communityTaxiVm.fetchCommunityTaxi(bodyQuery).observe(fContext) {
            communityTaxiAdapter?.submitData(it, termId)
        }
    }

    private fun showEmptyData(size: Int) {
        binding.noDataFoundView.root.visibility = if (size == 0) View.VISIBLE else View.GONE
        binding.communityTextRecyclerView.visibility = if (size == 0) View.GONE else View.VISIBLE
    }

    private class CommunityTaxiAdapter :
        PagedListAdapter<
                CommunityTaxiRespond,
                CommunityTaxiAdapter.MyCommunityTaxiViewHolder
                >(USER_COMPARATOR) {

        private var selectCommunityTaxiId: Int = -1
        lateinit var selectRow: (CommunityTaxiRespond) -> Unit

        companion object {
            private val USER_COMPARATOR = object : DiffUtil.ItemCallback<CommunityTaxiRespond>() {
                override fun areItemsTheSame(
                    oldItem: CommunityTaxiRespond,
                    newItem: CommunityTaxiRespond
                ): Boolean {
                    return false
                }

                override fun areContentsTheSame(
                    oldItem: CommunityTaxiRespond,
                    newItem: CommunityTaxiRespond
                ): Boolean =
                    false
            }
        }

        fun submitData(pagedList: PagedList<CommunityTaxiRespond>, termId: Int) {
            this.selectCommunityTaxiId = termId
            submitList(pagedList)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyCommunityTaxiViewHolder {
            return MyCommunityTaxiViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_taxi_bottom_sheet, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MyCommunityTaxiViewHolder, position: Int) {
            val communityTaxi: CommunityTaxiRespond? = getItem(position)
            communityTaxi?.let {
                holder.bind(communityTaxi)
                holder.selectItem(selectCommunityTaxiId == communityTaxi.id)
                holder.itemView.setOnClickListener {
                    selectCommunityTaxiId = communityTaxi.id ?: -1
                    notifyDataSetChanged()
                    selectRow.invoke(communityTaxi)
                }
            }
        }

        class MyCommunityTaxiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTv: TextView = itemView.findViewById(R.id.title_tv)
            private val tickImg: ImageView = itemView.findViewById(R.id.tick_img)

            fun bind(communityTaxi: CommunityTaxiRespond) {
                titleTv.text = communityTaxi.businessName ?: "N/A"
            }

            fun selectItem(selectItem: Boolean) {
                tickImg.visibility = if (selectItem) View.VISIBLE else View.GONE
            }
        }

    }

}