package com.eazy.daiku.utility.bottom_sheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.R
import com.eazy.daiku.databinding.CahngeStyleGoogleMapBtsLayoutBinding
import com.eazy.daiku.databinding.LanguageMenuBottomSheetDialogLayoutBinding
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment


class ChangeStyleGoogleMapBottomSheetFragment : BaseBottomSheetDialogFragment() {

    private lateinit var binding: CahngeStyleGoogleMapBtsLayoutBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var selectedShowTraffic: (Boolean) -> Unit
    private lateinit var selectedRotateGesture: (Boolean) -> Unit
    private lateinit var selectedNightTheme: (Boolean) -> Unit
    private var isShowTraffic: Boolean = false
    private var isRotateGesture: Boolean = false
    private var isNightTheme: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    companion object {
        @JvmStatic
        fun newInstance(
            isShowTraffic: Boolean = false,
            isRotateGesture: Boolean = false,
            isNightTheme: Boolean = false,
            selectedShowTraffic: (Boolean) -> Unit,
            selectedRotateGesture: (Boolean) -> Unit,
            selectedNightTheme: (Boolean) -> Unit
        ) =
            ChangeStyleGoogleMapBottomSheetFragment().apply {
                this.isShowTraffic = isShowTraffic
                this.isRotateGesture = isRotateGesture
                this.isNightTheme = isNightTheme
                this.selectedShowTraffic = selectedShowTraffic
                this.selectedRotateGesture = selectedRotateGesture
                this.selectedNightTheme = selectedNightTheme
            }
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
        binding = CahngeStyleGoogleMapBtsLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        doAction()

    }

    private fun initView() {
        binding.showTrafficSwitchmaterial.isChecked = isShowTraffic
        binding.rotateGestureSwitchmaterial.isChecked = isRotateGesture
        binding.nightThemeSwitchmaterial.isChecked = isNightTheme
    }

    private fun doAction() {
        binding.showTrafficSwitchmaterial.setOnCheckedChangeListener { compoundButton, b ->
            isShowTraffic = !isShowTraffic
            selectedShowTraffic.invoke(isShowTraffic)
        }

        binding.rotateGestureSwitchmaterial.setOnCheckedChangeListener { compoundButton, b ->
            isRotateGesture = !isRotateGesture
            selectedRotateGesture.invoke(isRotateGesture)
        }

        binding.nightThemeSwitchmaterial.setOnCheckedChangeListener { compoundButton, b ->
            isNightTheme = !isNightTheme
            selectedNightTheme.invoke(isNightTheme)
        }

        binding.actionCloseImg.setOnClickListener {
            dialog?.dismiss()
        }
    }


}