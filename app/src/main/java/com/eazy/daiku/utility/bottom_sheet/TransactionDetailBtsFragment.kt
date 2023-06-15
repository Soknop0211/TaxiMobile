package com.eazy.daiku.utility.bottom_sheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.R
import  com.eazy.daiku.data.model.MyTransaction
import com.eazy.daiku.databinding.TransactionDetailBtsFragmentBinding
import  com.eazy.daiku.utility.AmountHelper
import  com.eazy.daiku.utility.DateTimeHelper
import  com.eazy.daiku.utility.StatusColor
import  com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment

class TransactionDetailBtsFragment : BaseBottomSheetDialogFragment() {
    private lateinit var binding: TransactionDetailBtsFragmentBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var mTrx: MyTransaction

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            myTrx: MyTransaction
        ) =
            TransactionDetailBtsFragment().apply {
                this.mTrx = myTrx
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,R.style.TopRoundCornerBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TransactionDetailBtsFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

    }

    private fun initData() {
        var totalAmountColor = StatusColor.colorStatus(
            fContext, mTrx.walletTransactionStatus ?: ""
        )
        if (mTrx.total ?: 0.00 < 0) {
            totalAmountColor = ContextCompat.getColor(fContext, R.color.red_300)
        }
        val createDate = mTrx.createdAt ?: ""
        val createDateStr = DateTimeHelper.dateFm(
            createDate,
            "yyyy-MM-dd HH:mm:ss",
            "EEEE, dd MMMM yyyy"
        )


        val isWithdrawData = mTrx.type?.lowercase().equals("withdraw")
        if (isWithdrawData) {
            binding.typeContainerLine.visibility = View.VISIBLE
            binding.typeContainer.visibility = View.VISIBLE

            binding.toBankContainerLine.visibility = View.VISIBLE
            binding.toBankContainer.visibility = View.VISIBLE

            binding.bookingCodeContainerLine.visibility = View.GONE
            binding.bookingCodeContainer.visibility = View.GONE

            binding.trxIdContainerLine.visibility = View.GONE
            binding.trxIdContainer.visibility = View.GONE

            binding.payByContainerLine.visibility = View.GONE
            binding.payByContainer.visibility = View.GONE

            var title = "---"
            val bic = mTrx.mUserWithdraw?.bic ?: ""
            if (bic == "ABAAKHPP") {
                title = "ABA PAY"
            } else if (bic == "ACLBKPP") {
                title = "ACLEDA BANK Plc"
            }

            binding.toBankTrxDetailTv.text = title

        } else {
            binding.typeContainerLine.visibility = View.GONE
            binding.typeContainer.visibility = View.GONE

            binding.bookingCodeTrxDetailTv.text = mTrx.bookingCode ?: "---"
            binding.trxIdTrxDetailTv.text = mTrx.kessTransactionId ?: "---"
            binding.payByTrxDetailTv.text = mTrx.payBy ?: "---"

        }
        binding.totalAmountTv.text = AmountHelper.amountFormat(
            "$", mTrx.total ?: 0.00
        )
        binding.statusTrxDetailTv.text = mTrx.walletTransactionStatus ?: "---"
        binding.dateTrxDetailTv.text = createDateStr
        binding.totalAmountTv.setTextColor(totalAmountColor)
        binding.typrTrxDetailTv.text = mTrx.type ?: "---"

    }
}