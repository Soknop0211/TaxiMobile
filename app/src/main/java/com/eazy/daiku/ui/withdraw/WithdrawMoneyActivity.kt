package com.eazy.daiku.ui.withdraw

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import com.eazy.daiku.R
import com.eazy.daiku.data.model.ListAllBankAccountModel
import com.eazy.daiku.data.model.SaveBankAccount
import com.eazy.daiku.data.model.server_model.TransactionRespond
import com.eazy.daiku.databinding.ActivityWithdrawMoneyBinding

import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.BaseListenerAutoFillNumber
import com.eazy.daiku.utility.bottom_sheet.PaymentMethodBtsFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.enumerable.ActionCard
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.withdraw.WithdrawViewModel
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WithdrawMoneyActivity : BaseActivity() {
    private val withdrawViewModel: WithdrawViewModel by viewModels {
        factory
    }
    private lateinit var binding: ActivityWithdrawMoneyBinding
    private lateinit var _saveBankAccount: SaveBankAccount
    private var haseBankAccount: Boolean = false
    private lateinit var transactionRespond: TransactionRespond
    private var totalBalanceAmount: Double = 0.00
    private var pendingBalance: Double = 0.00
    private var totalAmount: Double = 0.00
    private val currencyUSD: Boolean = true
    private val intentFilter = IntentFilter()
    private var userId = -100
    private var listAllBank: ArrayList<ListAllBankAccountModel>? = null
    private var idTickBankAccount: Int? = null

    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    companion object {
        const val TransactionFileRespondKey = "TransactionFileRespondKey"
        const val listAllBankAccountModelKey = "listAllBankAccountModelKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initTransactionRespond()
        initBankAccount(false)
        initObserved()
        doAction()
    }


    private fun initView() {
        val user = getUserUserToSharePreference()
        user?.let {
            it.id?.let {
                userId = it
            }
        }
        listAllBank = ArrayList()
        if (intent != null && intent.hasExtra(listAllBankAccountModelKey)) {
            val jsonDataListBank = intent.getStringExtra(listAllBankAccountModelKey)
            val gson = Gson()
            val bankAccountTypeToken =
                object : TypeToken<ArrayList<ListAllBankAccountModel>>() {}.type
            listAllBank = gson.fromJson(jsonDataListBank, bankAccountTypeToken)
        }

        _saveBankAccount = SaveBankAccount()
        AmountHelper.decimalFilter(binding.amountEdt, if (currencyUSD) 2 else 0)
    }

    private fun initObserved() {
        // TODO: ========================== Start Withdraw Money =============================
        withdrawViewModel.loadingWithdrawLiveData.observe(this) { hasLoadingWithdraw ->
            binding.loadingView.root.visibility =
                if (hasLoadingWithdraw) View.VISIBLE else View.GONE

        }
        withdrawViewModel.dataWithdrawLiveData.observe(this) { responeWithdraw ->
            responeWithdraw?.let {
                if (responeWithdraw.success) {
                    var verifyOtpUrl: String
                    responeWithdraw.data.let {
                        responeWithdraw.data?.verifyOtpUrl.let {
                            verifyOtpUrl = responeWithdraw.data?.verifyOtpUrl.toString()
                        }
                    }
                    RedirectClass.gotoWithdrawMoneyWebViewActivity(self(),
                        GsonConverterHelper.convertGenericClassToJson(responeWithdraw),
                        verifyOtpUrl)
                } else {
                    MessageUtils.showError(self(), "", responeWithdraw.message)
                }
            }
        }

        // TODO: ========================== End Withdraw Money =============================


        withdrawViewModel.loadingUserWithdrawLiveData.observe(self()) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        withdrawViewModel.dataUserWithdrawLiveData.observe(self()) { it ->
            if (it != null && it.success) {
                var verifyOtpUrl: String
                it.data.let { it ->
                    it?.verifyOtpUrl.let {
                        verifyOtpUrl = it.toString()
                    }
                }
                RedirectClass.gotoWithdrawMoneyWebViewActivity(self(),
                    GsonConverterHelper.convertGenericClassToJson(it),
                    verifyOtpUrl)
            } else {
                MessageUtils.showError(self(), "", it.message)
            }
        }
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun doAction() {
        binding.actionAddCardOtherBankTv.setOnClickListener {
            PaymentMethodBtsFragment.newInstance(
                _saveBankAccount,
                ActionCard.AddCard,
                null, -1
            ) { hasChanged, saveBankAccount ->
                if (hasChanged) {
                    this._saveBankAccount = saveBankAccount
                    initBankAccount(false)
                }
            }.apply {
                show(supportFragmentManager, "PaymentMethodBtsFragment")
            }
        }

        binding.actionChangeCardBankAccountTv.setOnClickListener {

            // TODO: doesn't delete code

            /*  val abaAccountJson = EazyTaxiHelper.getSaveListAccountNumber(self())
              val mySaveBankAccount: ArrayList<SaveBankAccount> =
                  GsonConverterHelper.getBankAccountLists(abaAccountJson)*/


            val myBankAccount: ArrayList<SaveBankAccount> = ArrayList()
            listAllBank?.let { it ->
                for (bankAccount in it) {
                    val bankAccountId = bankAccount.id
                    val bankBic = bankAccount.bic
                    val ownerName = bankAccount.meta?.ownerName
                    val accountNumber = bankAccount.accountNumber
                    val logo =
                        if (bankAccount.bic == Config.BankBic.bicABA) R.drawable.aba_pay_icon else R.drawable.acleda_logo
                    bankAccountId?.let {
                        myBankAccount.add(SaveBankAccount(it,
                            "",
                            bankBic.toString(),
                            ownerName.toString(),
                            accountNumber.toString(),
                            logo))
                    }

                }
            }
            PaymentMethodBtsFragment.newInstance(
                _saveBankAccount,
                ActionCard.ChangeCard,
                myBankAccount,
                idTickBankAccount ?: -1,
            ) { hasChanged, saveBankAccount ->
                if (hasChanged && !TextUtils.isEmpty(saveBankAccount.name) && !TextUtils.isEmpty(
                        saveBankAccount.number)
                ) {
                    this._saveBankAccount = saveBankAccount
                    /* val myBankAccountJson = EazyTaxiHelper.getFirstBankAccount(self())
                     val myListBankAccount: ArrayList<SaveBankAccount> =
                         GsonConverterHelper.getListMyBankAccount(myBankAccountJson)
                     if (myListBankAccount.size > 0) {
                         for (myBankAccount in myListBankAccount) {
                             if (userId == myBankAccount.userId) {
                                 myListBankAccount.remove(myBankAccount)
                             }
                         }
                     }
                     myListBankAccount.add(saveBankAccount)
                     GsonConverterHelper.saveListMyBankAccount(self(), myListBankAccount)*/
                }
                initBankAccount(hasChanged)
            }.apply {
                show(supportFragmentManager, "PaymentMethodBtsFragment")
            }
        }

        binding.actionBackImage.setOnClickListener {
            finish()
        }
        binding.autoFillNumberRecyclerView.setBaseListener(baseListenerAutoFillNumber)

        binding.actionPay.setOnClickListener {
            KeyBoardHelper.hideKeyboard(self(), it)
            val amount = binding.amountEdt.text.toString()
            val amountDb = amount.toDoubleOrNull()
            amountDb?.let {
                if (amountDb > totalAmount) {
                    with(MessageUtils) {
                        showError(self(),
                            "",
                            getString(R.string.you_have_insufficient_balance_in_your_wallet))
                    }
                } else {
                    val bodyWithdraw = HashMap<String, Any>()
                    bodyWithdraw["bank_account_id"] = _saveBankAccount.userId
                    bodyWithdraw["total_amount"] = amountDb
                    withdrawViewModel.userSubmitWithdraw(bodyWithdraw)

                }
            }
        }
        binding.amountEdt.addTextChangedListener(textWatcher)
    }

    private fun initTransactionRespond() {
        transactionRespond = TransactionRespond()
        if (intent.hasExtra(TransactionFileRespondKey)) {
            val gsonStr = intent.getStringExtra(TransactionFileRespondKey)
            gsonStr?.let {
                transactionRespond =
                    GsonConverterHelper.getJsonObjectToGenericClass<TransactionRespond>(
                        gsonStr
                    )
            }
        }
        transactionRespond.let { transactionRespond ->
            transactionRespond.totalBalance?.let {
                totalBalanceAmount = transactionRespond.totalBalance!!
            }
            transactionRespond.blockedBalance?.let {
                pendingBalance = transactionRespond.blockedBalance!!
            }
            totalAmount = totalBalanceAmount - pendingBalance
        }
        binding.amountTv.text = AmountHelper.amountFormat(
            "$",
            totalAmount
        )
        binding.pendingAmount.text = AmountHelper.amountFormat(
            "$",
            pendingBalance
        )

    }

    private fun initBankAccount(hasClickChangeBankAccount: Boolean) {
        // TODO: doesn't delete code
        /*val myBankAccountJson = EazyTaxiHelper.getFirstBankAccount(self())
        val myListBankAccount: ArrayList<SaveBankAccount> =
            GsonConverterHelper.getListMyBankAccount(myBankAccountJson)
        for (myBankAccount in myListBankAccount) {
            if (myBankAccount.userId == userId) {
                this._saveBankAccount = myBankAccount
            }
        }*/
        if (!hasClickChangeBankAccount) {
            listAllBank?.let {
                binding.actionChangeCardBankAccountTv.visibility =
                    if (it.size > 1) View.VISIBLE else View.GONE
                haseBankAccount = it.size > 0
                if (it.size > 0) {
                    binding.containerBankAccount.visibility = View.VISIBLE
                    for (bankAccount in it) {
                        val bankAccountId = bankAccount.id
                        val bankBic = bankAccount.bic
                        val ownerName = bankAccount.meta?.ownerName
                        val accountNumber = bankAccount.accountNumber
                        val logo =
                            if (bankAccount.bic == Config.BankBic.bicABA) R.drawable.aba_pay_icon else R.drawable.acleda_logo
                        if (bankAccountId != null) {
                            _saveBankAccount = SaveBankAccount(bankAccountId,
                                "",
                                bankBic.toString(),
                                ownerName.toString(),
                                accountNumber.toString(),
                                logo)
                        }
                    }
                } else {
                    binding.containerBankAccount.visibility = View.GONE
                }
            }
        }
        // TODO: doesn't delete code
        /* if (!TextUtils.isEmpty(_saveBankAccount.number) && !TextUtils.isEmpty(_saveBankAccount.name) && _saveBankAccount.userId == userId) {
             haseBankAccount = true
             visibleView(true)
             containerViewBankAccount(_saveBankAccount)
         } else {
             haseBankAccount = false
             visibleView(false)
         }*/

        containerViewBankAccount(_saveBankAccount)


        val amountStr: String = binding.amountEdt.text.toString()
        val amountDb: Double? = amountStr.toDoubleOrNull()
        amountDb?.let { checkEnableButtonWithdraw(amountDb, haseBankAccount) }
            ?: checkEnableButtonWithdraw(0.0, haseBankAccount)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun containerViewBankAccount(saveBankAccount: SaveBankAccount) {
        idTickBankAccount = saveBankAccount.userId
        val linearLayout = LinearLayout(this)
        val bankLogo: ImageView
        val bankAccount: TextView
        val bankName: TextView
        val view = LinearLayout.inflate(self(), R.layout.save_bank_withdraw_layout, linearLayout)
        bankLogo = view.findViewById(R.id.bank_logo)
        bankName = view.findViewById(R.id.bank_name)
        bankAccount = view.findViewById(R.id.bank_account)
        val accountNumber =
            if (saveBankAccount.bic == Config.BankBic.bicACL) NumberFormatHelper.acledaFormatBankAccount(
                saveBankAccount.number)
            else NumberFormatHelper.abaFormatBankAccount(saveBankAccount.number)
        ImageHelper.loadImage(self(), saveBankAccount.logo, bankLogo)
        bankAccount.text = accountNumber
        bankName.text = saveBankAccount.name
        binding.containerBankAccount.addView(view)
    }

    private fun visibleView(haseBankAccount: Boolean) {
        if (haseBankAccount) {
            binding.containerBankAccount.visibility = View.VISIBLE
            binding.actionAddCardOtherBankTv.visibility = View.GONE
            binding.actionChangeCardBankAccountTv.visibility = View.VISIBLE
        } else {
            binding.containerBankAccount.visibility = View.VISIBLE
            binding.actionAddCardOtherBankTv.visibility = View.GONE
            binding.actionChangeCardBankAccountTv.visibility = View.GONE
        }
    }

    private val baseListenerAutoFillNumber: BaseListenerAutoFillNumber<AutoFillNumberRecyclerView.Item?> =
        object :
            BaseListenerAutoFillNumber<AutoFillNumberRecyclerView.Item?> {
            override fun onResult(data: AutoFillNumberRecyclerView.Item?) {
                if (data != null) {
                    binding.amountEdt.setText(data.number.toString())
                    val amountStr: String = binding.amountEdt.text.toString()
                    val amountDb: Double? = amountStr.toDoubleOrNull()
                    if (amountDb != null && amountDb > 0 && haseBankAccount) {
                        binding.actionPay.isEnabled = true
                    }
                }
            }
        }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val amountTextChangeStr: String = p0.toString()
            val amountTextChangeDb: Double? = amountTextChangeStr.toDoubleOrNull()
            amountTextChangeDb?.let {
                checkEnableButtonWithdraw(amountTextChangeDb, haseBankAccount)
            } ?: checkEnableButtonWithdraw(0.0, haseBankAccount)
        }

        override fun afterTextChanged(p0: Editable?) {

        }

    }

    private fun checkEnableButtonWithdraw(amount: Double, haseBankAccount: Boolean) {
        if (amount > 0 && haseBankAccount) {
            binding.actionPay.setEnabled(true)
        } else {
            binding.actionPay.setEnabled(false)
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
                    p1.hasExtra(whenWithdrawMoneySuccessFinishScreenWithdrawMoneyKey) -> {
                        startBroadcastData(reloadMainWalletWhenWithdrawSuccessKey)
                        finish()
                    }
                }
            }
        }

    }

}