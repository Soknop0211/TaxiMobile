package com.eazy.daiku.ui.payment_method

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.eazy.daiku.data.model.SaveBankAccount
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.utility.bottom_sheet.PaymentMethodBtsFragment
import com.eazy.daiku.utility.extension.navigateSafe
import com.eazy.daiku.R

import com.eazy.daiku.databinding.FragmentSelectBankBinding


class SelectBankFragment : BaseFragment() {

    private lateinit var binding: FragmentSelectBankBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var navController: NavController
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectBankBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        doAction(view)
    }

    private fun initView(view: View) {
        navController = Navigation.findNavController(requireView())
        val abaAccountJson = EazyTaxiHelper.getSaveListAccountNumber(fContext)
        binding.actionBackImg.visibility = View.GONE
        abaAccountJson.let {
            val mySaveBankAccount: ArrayList<SaveBankAccount> =
                GsonConverterHelper.getBankAccountLists(abaAccountJson)
            binding.actionBackImg.visibility =
                if (mySaveBankAccount.size > 0) View.VISIBLE else View.GONE
        }
    }

    private fun doAction(view: View) {

        binding.actionAcledaRelative.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", Config.BankBic.bicACL)
            navController.navigate(
                R.id.action_selectBankFragment_to_addABAAccountNumberFragment,
                bundle)
        }

        binding.actionAbaRelative.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type", Config.BankBic.bicABA)
            navController.navigateSafe(
                R.id.action_selectBankFragment_to_addABAAccountNumberFragment, bundle
            )
        }

        binding.actionBackImg.setOnClickListener {
            navController.popBackStack()
        }

        binding.actionCloseImg.setOnClickListener {
            accessParentFragment()?.dismiss()
        }
    }

    private fun accessParentFragment(): PaymentMethodBtsFragment? {
        if (fContext.supportFragmentManager.findFragmentByTag("PaymentMethodBtsFragment") is PaymentMethodBtsFragment) {
            return fContext.supportFragmentManager.findFragmentByTag("PaymentMethodBtsFragment") as PaymentMethodBtsFragment

        }
        return null
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SelectBankFragment()
    }
}