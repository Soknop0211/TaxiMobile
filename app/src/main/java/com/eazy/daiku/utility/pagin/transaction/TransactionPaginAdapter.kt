package com.eazy.daiku.utility.pagin.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.MyTransaction
import com.eazy.daiku.utility.AmountHelper
import com.eazy.daiku.utility.DateTimeHelper
import com.eazy.daiku.utility.StatusColor
import com.eazy.daiku.utility.other.AppLOGG
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class TransactionPaginAdapter :
    PagedListAdapter<
            MyTransaction,
            TransactionPaginAdapter.MyTransactionPaginViewHolder
            >(USER_COMPARATOR) {

    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<MyTransaction>() {
            override fun areItemsTheSame(oldItem: MyTransaction, newItem: MyTransaction): Boolean {
                return false
            }

            override fun areContentsTheSame(
                oldItem: MyTransaction,
                newItem: MyTransaction,
            ): Boolean =
                false
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyTransactionPaginViewHolder {
        return MyTransactionPaginViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.transactions_row_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyTransactionPaginViewHolder, position: Int) {
        val transaction: MyTransaction? = getItem(position)
        transaction?.let {
            holder.bind(transaction)
            holder.itemView.setOnClickListener {
                selectRowTrx.invoke(transaction)
            }

        }
    }

    lateinit var selectRowTrx: (MyTransaction) -> Unit

    class MyTransactionPaginViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.transfer_name_tv)
        private val remarkTv: TextView = itemView.findViewById(R.id.remark_tv)
        private val timeTv: TextView = itemView.findViewById(R.id.time_tv)
        private val amountTv: TextView = itemView.findViewById(R.id.amount_tv)
        private val titleHeaderTv: TextView = itemView.findViewById(R.id.title_date_tv)
        private val headerContainerView: LinearLayout =
            itemView.findViewById(R.id.header_container_view)
        private val totalAmountDailyTv: TextView =
            itemView.findViewById(R.id.daily_total_amount_tv)
        private val statusTrxImg: ImageView =
            itemView.findViewById(R.id.status_img)
        private val totalByDateCardView: MaterialCardView =
            itemView.findViewById(R.id.total_by_date_card_view)

        fun bind(transaction: MyTransaction) {
            var title = "N/A"
            var remark = "---"
            val dateTime = DateTimeHelper.localDateTimeFm(transaction.createdAt ?: "")
            dateTime?.let {
                val time: String = DateTimeHelper.dateFm(it, "hh:mm a")
                timeTv.text = time
            }
            var amountColor = StatusColor.colorStatus(
                itemView.context, transaction.walletTransactionStatus ?: ""
            )
            val amount = AmountHelper.amountFormat(
                "$", transaction.total ?: 0.00
            )

            val trxStatusColor = StatusColor.colorStatusTrx(
                itemView.context, transaction.walletTransactionStatus ?: ""
            )

            val createDate = transaction.createdAt ?: ""
            val createDateStr = DateTimeHelper.dateFm(
                createDate,
                "yyyy-MM-dd HH:mm:ss",
                "EEEE, dd MMMM yyyy"
            )
            val totalAmountByDate: Double =
                transaction.totalAmountByDate?.get(createDateStr) ?: 0.00

            if (transaction.type?.lowercase().equals("withdraw")) {
                val bic = transaction.mUserWithdraw?.bic ?: ""
                if (bic == "ABAAKHPP") {
                    title = "Withdraw to ABA PAY"
                } else if (bic == "ACLBKPP") {
                    title = "Withdraw to ACLEDA BANK Plc"
                }
                remark =
                    if (transaction.mUserWithdraw != null && transaction.mUserWithdraw!!.remark != null) transaction.mUserWithdraw!!.remark.toString() else "No Remark"
                remarkTv.alpha = 0.5f

                amountColor = if (transaction.total ?: 0.00 < 0) {
                    ContextCompat.getColor(itemView.context, R.color.red_300)
                } else {
                    ContextCompat.getColor(itemView.context, R.color.green_600)
                }

            } else {
                title = transaction.bookingCode ?: "N/A"
                remark = String.format("%s %s", "Pay by:", transaction.payBy ?: "---")
                remarkTv.alpha = 1f
            }

            if (totalAmountByDate < 0.0) {
                totalByDateCardView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.red_300
                    )
                )
            } else {
                totalByDateCardView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.green_600
                    )
                )
            }

            titleTv.text = title
            remarkTv.text = remark
            amountTv.text = amount
            amountTv.setTextColor(amountColor)
            statusTrxImg.setImageDrawable(trxStatusColor)

            headerContainerView.visibility =
                if (transaction.showHeaderDate == true) View.VISIBLE else View.GONE
            titleHeaderTv.text = createDateStr
            totalAmountDailyTv.text = AmountHelper.amountFormat(
                "$", totalAmountByDate
            )

        }
    }

}