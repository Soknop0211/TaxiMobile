package com.eazy.daiku.ui.customer.step_booking

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityPaymentCompleteBinding
import com.eazy.daiku.utility.QRCodeHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.service.MyBroadcastReceiver

class PaymentCompleteActivity : BaseActivity() {
    private lateinit var binding: ActivityPaymentCompleteBinding


    companion object {
        const val qrcodeUrlKey = "qrcodeUrlKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        if (intent != null && intent.hasExtra(qrcodeUrlKey)) {
            val qrCodeUrl = intent.getStringExtra(qrcodeUrlKey)
            val bitmap = QRCodeHelper.generateQrImage(qrCodeUrl)
            binding.imQrcode.setImageBitmap(bitmap)
        }
        binding.actionOk.setOnClickListener {
            startBroadcastData(MyBroadcastReceiver.finishWhenPaymentCompletedKey)
            finish()
        }
    }

}