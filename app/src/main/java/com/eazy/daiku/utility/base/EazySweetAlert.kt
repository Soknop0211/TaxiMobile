package com.eazy.daiku.utility.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.google.android.material.button.MaterialButton

class EazySweetAlert(context: Context?, alertType: Int) :
    SweetAlertDialog(context, alertType) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.IS_WEGO_TAXI) {
            val confirmBtn = findViewById<MaterialButton>(R.id.confirm_button)
            confirmBtn.setTextColor(ContextCompat.getColor(context, R.color.white))
        }

    }



}