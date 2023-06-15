package com.eazy.daiku.utility.extension

import android.content.Context
import com.eazy.daiku.utility.EazyTaxiHelper
import com.google.android.material.button.MaterialButton

fun MaterialButton.test(context: Context) {
    if (EazyTaxiHelper.hasNotNetworkAvailable(context)) {
        setOnClickListener(null)
    } else {
        setOnClickListener { }
    }
}