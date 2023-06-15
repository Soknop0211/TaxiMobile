package com.eazy.daiku.ui.payment_method

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.SaveBankAccount
import com.eazy.daiku.databinding.FragmentChangeCardAndBankAccountBinding
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.utility.bottom_sheet.PaymentMethodBtsFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.extension.navigateSafe

class ChangeCardAndBankAccountFragment : BaseFragment() {

    private lateinit var fContext: FragmentActivity
    private lateinit var navController: NavController
    private lateinit var binding: FragmentChangeCardAndBankAccountBinding
    private lateinit var saveBankAccountAdapter: SaveBankAccountAdapter
    private lateinit var mySaveBankAccounts: ArrayList<SaveBankAccount>
    private lateinit var _saveBankAccountListsListener: (Boolean) -> Unit
    private lateinit var saveBankAccount: SaveBankAccount
    private var idTickBankAccount: Int? = null

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddABAAccountNumberFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            saveAbaAccountListsListener: (Boolean) -> Unit,
        ) =
            ChangeCardAndBankAccountFragment().apply {
                this._saveBankAccountListsListener = saveAbaAccountListsListener
            }

        const val SaveBankAccountKey = "SaveBankAccountKey"
        const val firstSaveBankAccountKey = "firstSaveBankAccountKey"
        const val idTickBankAccountKey = "idTickBankAccountKey"

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySaveBankAccounts = ArrayList()
        arguments?.let { bundle ->
            mySaveBankAccounts =
                bundle.getSerializable(SaveBankAccountKey) as ArrayList<SaveBankAccount>
        }

        arguments?.let { bundle ->
            val bankAccount =
                bundle.getString(firstSaveBankAccountKey) as String
            saveBankAccount = GsonConverterHelper.getFirstBankAccount(bankAccount)
        }
        arguments?.let {
            idTickBankAccount = it.getInt(idTickBankAccountKey)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChangeCardAndBankAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initMySaveBankAccount()
        doAction()
    }

    private fun initView(view: View) {
        navController = Navigation.findNavController(view)
        saveBankAccountAdapter = SaveBankAccountAdapter()
    }

    private fun doAction() {
        binding.actionAddCardOtherBankTv.setOnClickListener {
            navController.navigateSafe(
                R.id.action_changeCardAndBankAccountFragment_to_selectBankFragment
            )
        }

        binding.actionCloseImg.setOnClickListener {
            accessParentFragment()?.dismiss()
        }

        saveBankAccountAdapter.selectedRow = { saveBankAccount, hasItemClick ->
            if (hasItemClick) {
                accessParentFragment()?.saveAbaAccountListsListener?.invoke(true, saveBankAccount)
            } else {
                val user = getUserUserToSharePreference()
                var userId = -100
                user.id?.let {
                    userId = it
                }
                val abaAccountJson = EazyTaxiHelper.getSaveListAccountNumber(fContext)
                val mySaveBankAccount: ArrayList<SaveBankAccount> =
                    GsonConverterHelper.getBankAccountLists(abaAccountJson)
                EazyTaxiHelper.removeListBankAccount(
                    fContext,
                    mySaveBankAccount,
                    saveBankAccount,
                    userId
                )
                accessParentFragment()?.saveAbaAccountListsListener?.invoke(false, saveBankAccount)
            }
            accessParentFragment()?.dismiss()
        }
    }

    private fun accessParentFragment(): PaymentMethodBtsFragment? {
        if (fContext.supportFragmentManager.findFragmentByTag("PaymentMethodBtsFragment") is PaymentMethodBtsFragment) {
            return fContext.supportFragmentManager.findFragmentByTag("PaymentMethodBtsFragment") as PaymentMethodBtsFragment
        }
        return null
    }

    private fun initMySaveBankAccount() {
        val user = getUserUserToSharePreference()
        var userId = -100
        user.id?.let {
            userId = it
        }
        if (mySaveBankAccounts.size > 0) {
            val myListBankAccount = ArrayList<SaveBankAccount>()
            for (myBankAccount in mySaveBankAccounts) {
                // TODO: doesn't delete code

                /*  if (myBankAccount.userId == userId) {
                      myListBankAccount.add(myBankAccount)
                  }*/

                myListBankAccount.add(myBankAccount)

            }
            saveBankAccountAdapter.add(myListBankAccount, saveBankAccount, idTickBankAccount ?: -1)
            binding.mySaveBankRecyclerView.apply {
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = saveBankAccountAdapter
            }
        }
    }

    /*custom adapter*/
    private class SaveBankAccountAdapter :
        RecyclerView.Adapter<SaveBankAccountAdapter.SaveBankAccountHolder>() {
        private var saveBankAccount: ArrayList<SaveBankAccount>? = null
        private lateinit var firstBankAccount: SaveBankAccount
        lateinit var selectedRow: (SaveBankAccount, Boolean) -> Unit
        private var idTickBankAccount = -100

        fun add(
            saveBankAccount: ArrayList<SaveBankAccount>,
            myBankAccount: SaveBankAccount,
            idTickBankAccount: Int,
        ) {
            clear()
            this.saveBankAccount = saveBankAccount
            this.firstBankAccount = myBankAccount
            this.idTickBankAccount = idTickBankAccount
        }

        fun clear() {
            this.saveBankAccount?.clear()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveBankAccountHolder {
            return SaveBankAccountHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.my_save_bank_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: SaveBankAccountHolder, position: Int) {
            val model = saveBankAccount?.get(position)
            if (model != null) {
                holder.bind(model)
            }
            holder.viewTickBankAccount.visibility =
                if (idTickBankAccount == model?.userId) View.VISIBLE else View.GONE
            holder.viewUnTickBankAccount.visibility =
                if (idTickBankAccount == model?.userId) View.GONE else View.VISIBLE
            /*if (model?.uuid == firstBankAccount.uuid) {
                holder.viewTickBankAccount.visibility = View.VISIBLE
                holder.viewUnTickBankAccount.visibility = View.GONE
            } else {
                holder.viewTickBankAccount.visibility = View.GONE
                holder.viewUnTickBankAccount.visibility = View.VISIBLE
            }*/
            holder.itemView.setOnClickListener {
                selectedRow.invoke(model ?: SaveBankAccount(), true)

            }
            holder.actionDelete.setOnClickListener {
                MessageUtils.showConfirm(holder.itemView.context,
                    "",
                    holder.itemView.context.getString(
                        R.string.are_you_sure_you_want_to_remove_bank_account
                    ),
                    holder.itemView.context.getString(R.string.action_cancel),
                    holder.itemView.context.getString(R.string.ok),
                    { onCancel ->
                        onCancel.dismiss()
                    },
                    { onConfirm ->
                        selectedRow.invoke(model!!, false)
                        onConfirm.dismiss()

                    })
            }
        }

        override fun getItemCount(): Int {
            return saveBankAccount?.size ?: 0
        }

        class SaveBankAccountHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val logoImg: ImageView = itemView.findViewById(R.id.logo_img)
            val accountNameTv: TextView = itemView.findViewById(R.id.account_name_tv)
            val accountNumberTv: TextView = itemView.findViewById(R.id.account_number_tv)
            val actionDelete: ImageView = itemView.findViewById(R.id.delete_img)
            val viewTickBankAccount: ImageView = itemView.findViewById(R.id.tick_img)
            val viewUnTickBankAccount: ImageView = itemView.findViewById(R.id.un_tick)

            fun bind(saveBankAccount: SaveBankAccount) {
                val accountNumber = if (saveBankAccount.bic == Config.BankBic.bicACL) {
                    NumberFormatHelper.acledaFormatBankAccount(saveBankAccount.number)
                } else {
                    NumberFormatHelper.abaFormatBankAccount(saveBankAccount.number)
                }
                accountNameTv.text = saveBankAccount.name
                accountNumberTv.text = accountNumber
                ImageHelper.loadImage(
                    itemView.context,
                    saveBankAccount.logo,
                    logoImg
                )

            }

        }

    }

}