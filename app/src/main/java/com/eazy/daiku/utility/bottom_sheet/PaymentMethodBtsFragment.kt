package com.eazy.daiku.utility.bottom_sheet

import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.eazy.daiku.R
import com.eazy.daiku.data.model.SaveBankAccount
import com.eazy.daiku.databinding.PaymentMethodBtsFragmentBinding
import com.eazy.daiku.ui.payment_method.ChangeCardAndBankAccountFragment
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import com.eazy.daiku.utility.enumerable.ActionCard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import java.lang.Exception

class PaymentMethodBtsFragment : BaseBottomSheetDialogFragment() {

    private lateinit var fContext: FragmentActivity
    private lateinit var binding: PaymentMethodBtsFragmentBinding
    private lateinit var navController: NavController // don't forget to initialize
    private lateinit var _saveAbaAccountListsListener: (Boolean, SaveBankAccount) -> Unit
    private lateinit var actionCardEnum: ActionCard
    private var mySaveBankAccount: ArrayList<SaveBankAccount>? = null
    private lateinit var saveBankAccount: SaveBankAccount
    private var idTickBankAccount: Int? = null

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            saveBankAccount: SaveBankAccount,
            actionCardEnum: ActionCard,
            mySaveBankAccount: ArrayList<SaveBankAccount>? = null,
            idTickBankAccount: Int,
            saveAbaAccountListsListener: (Boolean, SaveBankAccount) -> Unit,
        ) =
            PaymentMethodBtsFragment().apply {
                this._saveAbaAccountListsListener = saveAbaAccountListsListener
                this.actionCardEnum = actionCardEnum
                this.mySaveBankAccount = mySaveBankAccount
                this.saveBankAccount = saveBankAccount
                this.idTickBankAccount = idTickBankAccount
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.payment_method_bts_fragment, container, false)
        // binding = PaymentMethodBtsFragmentBinding.inflate(layoutInflater)
        try {
            //Todo checked
            if (dialog != null && dialog!!.window != null) {
                dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            }
        } catch (ignored: Exception) {
        }
        if (activity != null) {
            val decorView = requireActivity().window.decorView
            decorView.viewTreeObserver.addOnGlobalLayoutListener {
                val displayFrame = Rect()
                decorView.getWindowVisibleDisplayFrame(displayFrame)
                val height = decorView.context.resources.displayMetrics.heightPixels
                val heightDifference = height - displayFrame.bottom
                if (heightDifference != 0) {
                    if (view.paddingBottom != heightDifference) {
                        view.setPadding(0, 0, 0, heightDifference)
                    }
                } else {
                    if (view.paddingBottom != 0) {
                        view.setPadding(0, 0, 0, 0)
                    }
                }
            }
        }
        if (dialog != null) {
            dialog!!.setOnShowListener { dialog: DialogInterface ->
                val d = dialog as BottomSheetDialog
                val bottomSheetInternal = d.findViewById<View>(R.id.design_bottom_sheet)
                if (bottomSheetInternal != null)
                    BottomSheetBehavior.from(bottomSheetInternal).state =
                        BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return view/*binding.root*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        doAction()
    }

    private fun initView() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment?
        navHostFragment?.let {
            navController = it.navController
            when (actionCardEnum) {
                ActionCard.AddCard -> {
                    navController.setGraph(R.navigation.payment_method_nav_graph)
                    val graph = navController.graph
                    graph.startDestination = R.id.selectBankFragment
                }
                ActionCard.ChangeCard -> {
                    mySaveBankAccount?.let { saveBanks ->
                        val bundle = Bundle()
                        bundle.putSerializable(
                            ChangeCardAndBankAccountFragment.SaveBankAccountKey,
                            saveBanks
                        )
                        val gson = Gson()
                        val bankAccount = gson.toJson(saveBankAccount)
                        bundle.putString(
                            ChangeCardAndBankAccountFragment.firstSaveBankAccountKey,
                            bankAccount
                        )
                        bundle.putInt(ChangeCardAndBankAccountFragment.idTickBankAccountKey,
                            idTickBankAccount ?: -1)
                        navController.setGraph(R.navigation.payment_method_save_nav_graph, bundle)
                        val graph = navController.graph
                        graph.startDestination = R.id.changeCardAndBankAccountFragment
                    }
                }
            }
        }
    }

    private fun doAction() {

    }

    override fun getTheme(): Int {
        return R.style.TopRoundCornerBottomSheetDialogTheme
    }

    val saveAbaAccountListsListener: (Boolean, SaveBankAccount) -> Unit get() = _saveAbaAccountListsListener
}