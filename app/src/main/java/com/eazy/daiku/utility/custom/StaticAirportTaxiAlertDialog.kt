package com.eazy.daiku.utility.custom

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.databinding.StaticAirportTaxiLayoutBinding
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.QRCodeHelper
class StaticAirportTaxiAlertDialog : DialogFragment() {

    private lateinit var fContext: FragmentActivity
    private lateinit var binding: StaticAirportTaxiLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            StaticAirportTaxiAlertDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StaticAirportTaxiLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val qrCodeBitmap = QRCodeHelper.generateQrImage("12345678")
        binding.qrCodeImg.setImageBitmap(qrCodeBitmap)

    }


}