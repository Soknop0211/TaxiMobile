package com.eazy.daiku.utility.extension

import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.disableCopyPaste() {
    this.setOnLongClickListener {
        return@setOnLongClickListener false
    }
}