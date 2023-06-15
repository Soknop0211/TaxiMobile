package com.eazy.daiku.utility.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.eazy.daiku.R

class GlobalLoadingDialog(val context: Context) {

    private var dialog: Dialog? = null

    fun showLoadingDialog(show: Boolean) {
        if (dialog == null) {
            dialog = Dialog(context)
        }
        dialog?.setContentView(R.layout.loading_layout)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (show) {
            dialog?.create()
            dialog?.show()
        } else {
            dialog?.dismiss()
        }

    }
}