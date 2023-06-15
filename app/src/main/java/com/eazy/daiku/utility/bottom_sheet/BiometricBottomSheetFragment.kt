package com.eazy.daiku.utility.bottom_sheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.R
import com.eazy.daiku.databinding.BiometricBottomSheetLayoutBinding
import com.eazy.daiku.databinding.CahngeStyleGoogleMapBtsLayoutBinding
import com.eazy.daiku.utility.BiometricSecurity
import com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import com.eazy.daiku.utility.base.BiometricCheck

class BiometricBottomSheetFragment : BaseBottomSheetDialogFragment() {

    private lateinit var binding: BiometricBottomSheetLayoutBinding
    private lateinit var fContext: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    companion object {
        @JvmStatic
        fun newInstance(

        ) =
            BiometricBottomSheetFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TopRoundCornerBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BiometricBottomSheetLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        doAction()
    }

    private fun initView() {
        val data = EazyTaxiHelper.getSharePreference(fContext, Constants.biometricKey)
        if (data.isNotEmpty()) {
            binding.enableBiometricSwitchmaterial.isChecked = data == "1"
        }
    }

    private fun doAction() {
        binding.actionCloseImg.setOnClickListener {
            dialog?.dismiss()
        }
        binding.enableBiometricSwitchmaterial.setOnClickListener {
            val hasBiometric = BiometricSecurity.checkBiometric(fContext)
            if (hasBiometric) {
                if (binding.enableBiometricSwitchmaterial.isChecked) {
                    EazyTaxiHelper.setSharePreference(fContext, Constants.biometricKey, "1")
                } else {
                    EazyTaxiHelper.setSharePreference(fContext, Constants.biometricKey, "0")
                }
            } else {
                binding.enableBiometricSwitchmaterial.isChecked = false
            }
        }

    }
}