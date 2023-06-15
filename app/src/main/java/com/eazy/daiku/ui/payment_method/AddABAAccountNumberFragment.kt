package com.eazy.daiku.ui.payment_method

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.eazy.daiku.data.model.SaveBankAccount
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.utility.bottom_sheet.PaymentMethodBtsFragment
import com.eazy.daiku.R
import com.eazy.daiku.databinding.FragmentAddABAAccountNumberBinding
import com.eazy.daiku.utility.KeyBoardHelper
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.view_model.withdraw.WithdrawViewModel
import java.util.*
import kotlin.collections.ArrayList

class AddABAAccountNumberFragment : BaseFragment() {
    val withdrawViewModel: WithdrawViewModel by viewModels {
        factory
    }
    private lateinit var binding: FragmentAddABAAccountNumberBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var navController: NavController
    private var typeBank: String = ""
    private var accountNumber: String = ""
    private var userId = -100

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddABAAccountNumberBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
        doAction()
    }


    private fun initView(view: View) {
        val user = getUserUserToSharePreference()
        user.id?.let {
            userId = it
        }
        binding.accountNumberTfEditext.requestFocus()
        KeyBoardHelper.showKeyboard(fContext, binding.root)
        navController = Navigation.findNavController(requireView())
        typeBank = getArguments()?.getString("type").toString();
        setViewBank(typeBank)
        ediTextFormatBankAccount(typeBank)
    }

    private fun initObserver() {
        // TODO: ========================== Start Verify BankAccount =============================
        withdrawViewModel.loadingVerifyBankAccount.observe(fContext) { haseLoadingVerifyBankAccount ->
            binding.loadingView.root.visibility =
                if (haseLoadingVerifyBankAccount) View.VISIBLE else View.GONE

        }
        withdrawViewModel.dataRespondVerifyBankAccount.observe(fContext) { respondVerifyBankAccount ->
            if (respondVerifyBankAccount != null && respondVerifyBankAccount.success) {
                var id = ""
                var hasAccountNumber = false
                val abaAccountJson = EazyTaxiHelper.getSaveListAccountNumber(fContext)
                val myBankAccountList: ArrayList<SaveBankAccount> =
                    GsonConverterHelper.getBankAccountLists(abaAccountJson)
                for (myBankAccount in myBankAccountList) {
                    val haseAccountNumberStr = myBankAccount.number.replace(" ", "")
                    val bic = myBankAccount.bic
                    val idUser = myBankAccount.userId
                    val haseAccountNumberEd = binding.accountNumberTfEditext.text.toString()
                        .replace(" ", "")
                    if (haseAccountNumberStr == haseAccountNumberEd && bic == typeBank && idUser == userId) {
                        id = myBankAccount.uuid
                        hasAccountNumber = true
                    }
                }
                if (!hasAccountNumber) {
                    id = UUID.randomUUID().toString()
                    respondVerifyBankAccount.data?.ownerName?.let {
                        respondVerifyBankAccount.data.accountNumber?.let { it1 ->
                            saveListBankAccount(id,
                                it, it1)
                        }
                    }
                    respondVerifyBankAccount.data?.ownerName?.let {
                        respondVerifyBankAccount.data.accountNumber?.let { it1 ->
                            saveFirstBankAccount(id,
                                it, it1)
                        }
                    }
                } else {
                    respondVerifyBankAccount.data?.ownerName?.let {
                        respondVerifyBankAccount.data.accountNumber?.let { it1 ->
                            saveFirstBankAccount(id,
                                it, it1)
                        }
                    }
                }
                accessParentFragment()?.saveAbaAccountListsListener?.invoke(true, SaveBankAccount())
                accessParentFragment()?.dismiss()
            } else {
                MessageUtils.showError(fContext, "", respondVerifyBankAccount.message)
            }
        }

        // TODO: ========================== End Verify BankAccount =============================
    }

    private fun doAction() {
        binding.actionBackImg.setOnClickListener {
            navController.popBackStack()
        }
        binding.actionConfirmMtb.setOnClickListener {
            accountNumber = binding.accountNumberTfEditext.text.toString()
            if (accountNumber.isEmpty()) {
                binding.accountNumberTfLayout.error =
                    getString(R.string.please_enter_account_number)
            } else {
                val bodyVerifyBankAccount = HashMap<String, Any>()
                bodyVerifyBankAccount["bic"] = typeBank
                bodyVerifyBankAccount["bank_account_number"] =
                    binding.accountNumberTfEditext.text.toString().replace(" ", "")
                withdrawViewModel.submitVerifyBankAccount(bodyVerifyBankAccount)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setViewBank(bic: String) {
        if (!TextUtils.isEmpty(bic) && bic.equals(Config.BankBic.bicACL)) {
            val drawable = resources.getDrawable(R.drawable.acleda_logo, requireContext().theme)
            binding.bankLogo.setImageDrawable(drawable)
            binding.bankName.text = context?.getText(R.string.acleda_bank_plc) ?: ""
        } else {
            val drawable = resources.getDrawable(R.drawable.aba_pay_icon, requireContext().theme)
            binding.bankLogo.setImageDrawable(drawable)
            binding.bankName.text = context?.getText(R.string.aba_pay) ?: ""

        }

    }

    private fun ediTextFormatBankAccount(bankType: String) {
        if (bankType == Config.BankBic.bicACL) {
            binding.accountNumberTfEditext.groupLength = 4
            binding.accountNumberTfEditext.groupSeparator = ' '
            binding.accountNumberTfEditext.inputLength = 19
        } else {
            binding.accountNumberTfEditext.groupLength = 3
            binding.accountNumberTfEditext.groupSeparator = ' '
            binding.accountNumberTfEditext.inputLength = 11
        }
    }

    private fun saveFirstBankAccount(id: String, ownerName: String, accountNumber: String) {
        val myBankAccountJson = EazyTaxiHelper.getFirstBankAccount(fContext)
        val myListBankAccount: ArrayList<SaveBankAccount> =
            GsonConverterHelper.getListMyBankAccount(myBankAccountJson)
        if (myListBankAccount.size > 0) {
            for (myBankAccount in myListBankAccount) {
                if (userId == myBankAccount.userId) {
                    myListBankAccount.remove(myBankAccount)
                }
            }
        }
        if (!TextUtils.isEmpty(typeBank) && typeBank == Config.BankBic.bicACL) {
            myListBankAccount.add(
                SaveBankAccount(
                    userId,
                    id,
                    Config.BankBic.bicACL,
                    ownerName,
                    accountNumber,
                    R.drawable.acleda_logo
                )
            )
        } else {
            myListBankAccount.add(
                SaveBankAccount(userId, id,
                    Config.BankBic.bicABA,
                    ownerName,
                    accountNumber,
                    R.drawable.aba_pay_icon)
            )
        }
        GsonConverterHelper.saveListMyBankAccount(fContext, myListBankAccount)
    }

    private fun saveListBankAccount(id: String, ownerName: String, accountNumber: String) {
        binding.accountNumberTfLayout.error = null
        val abaAccountJson = EazyTaxiHelper.getSaveListAccountNumber(fContext)
        val abaLists: ArrayList<SaveBankAccount> =
            GsonConverterHelper.getBankAccountLists(abaAccountJson)
        if (!TextUtils.isEmpty(typeBank) && typeBank.equals(Config.BankBic.bicACL)) {
            abaLists.add(
                SaveBankAccount(
                    userId,
                    id,
                    Config.BankBic.bicACL,
                    ownerName,
                    accountNumber,
                    R.drawable.acleda_logo
                )
            )
        } else {
            abaLists.add(
                SaveBankAccount(
                    userId,
                    id,
                    Config.BankBic.bicABA,
                    ownerName,
                    accountNumber,
                    R.drawable.aba_pay_icon
                )
            )
        }
        GsonConverterHelper.saveBankAccountJson(fContext, abaLists)
    }


    private fun accessParentFragment(): PaymentMethodBtsFragment? {
        if (fContext.supportFragmentManager.findFragmentByTag("PaymentMethodBtsFragment") is PaymentMethodBtsFragment) {
            return fContext.supportFragmentManager.findFragmentByTag("PaymentMethodBtsFragment") as PaymentMethodBtsFragment

        }
        return null
    }

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
        fun newInstance() =
            AddABAAccountNumberFragment()
    }
}