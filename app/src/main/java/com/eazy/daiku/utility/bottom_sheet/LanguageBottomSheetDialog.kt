package com.eazy.daiku.utility.bottom_sheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import  com.eazy.daiku.utility.other.LocaleManager
import  com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import  com.eazy.daiku.utility.enumerable.LanguageSettings
import com.eazy.daiku.databinding.LanguageMenuBottomSheetDialogLayoutBinding
import com.eazy.daiku.utility.extension.toPx

class LanguageBottomSheetDialog : BaseBottomSheetDialogFragment() {

    private lateinit var binding: LanguageMenuBottomSheetDialogLayoutBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var chooseLanguage: (LanguageSettings) -> Unit

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            chooseLanguage: (LanguageSettings) -> Unit
        ) =
            LanguageBottomSheetDialog().apply {
                this.chooseLanguage = chooseLanguage
            }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
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
        binding = LanguageMenuBottomSheetDialogLayoutBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        doAction()
        initConfigure()
    }

    private fun initView() {
        if (BuildConfig.IS_WEGO_TAXI) {
            binding.imKh.setImageResource(R.drawable.tick_icon_app_green)
            binding.imEn.setImageResource(R.drawable.tick_icon_app_green)
            binding.imKh.layoutParams.width = 20.toPx()
            binding.imEn.layoutParams.height = 20.toPx()

            binding.chinaLineView.visibility = View.GONE //line horizontal
            binding.chinaLineView.visibility = View.GONE //view china language
        } else {
            binding.imKh.setImageResource(R.drawable.tick_icon_app_color)
            binding.imEn.setImageResource(R.drawable.tick_icon_app_color)
            binding.imCh.setImageResource(R.drawable.tick_icon_app_color)
        }
    }

    private fun doAction() {
        binding.actionKhmerMenu.setOnClickListener {
            chooseLanguage.invoke(LanguageSettings.Khmer)
            dismiss()
        }

        binding.actionEnglishMenu.setOnClickListener {
            chooseLanguage.invoke(LanguageSettings.English)
            dismiss()
        }

        binding.actionChinaMenu.setOnClickListener {
            chooseLanguage.invoke(LanguageSettings.China)
            dismiss()
        }

        binding.actionCloseImg.setOnClickListener {
            dismiss()
        }
    }

    private fun initConfigure() {
        val khmerLanguage = LocaleManager.getLanguagePref(fContext).equals("km")
        val englishLanguage = LocaleManager.getLanguagePref(fContext).equals("en")
        val chinaLanguage = LocaleManager.getLanguagePref(fContext).equals("zh")
        binding.imKh.visibility = if (khmerLanguage) View.VISIBLE else View.GONE
        binding.imEn.visibility = if (englishLanguage) View.VISIBLE else View.GONE
        binding.imCh.visibility = if (chinaLanguage) View.VISIBLE else View.GONE

    }


}