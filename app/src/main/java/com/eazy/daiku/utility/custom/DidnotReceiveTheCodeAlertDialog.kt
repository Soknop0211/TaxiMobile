package com.eazy.daiku.utility.custom

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.databinding.DidNotReceiveCodeAlertLayoutBinding
import com.eazy.daiku.utility.ImageHelper
import com.google.android.material.button.MaterialButton

class DidnotReceiveTheCodeAlertDialog : DialogFragment() {

    private lateinit var binding: DidNotReceiveCodeAlertLayoutBinding
    private lateinit var fContext: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            DidnotReceiveTheCodeAlertDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DidNotReceiveCodeAlertLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.actionClose.setOnClickListener {
            dialog?.dismiss()
        }

        val logoEazy =
            R.drawable.code_not_receive
        context?.let {
            ImageHelper.loadImage(
                it.applicationContext,
                logoEazy,
                binding.eazyLogo
            )
        }

        if (BuildConfig.IS_WEGO_TAXI) {
            binding.actionClose.setTextColor(ContextCompat.getColor(fContext, R.color.white))
        }


    }

}