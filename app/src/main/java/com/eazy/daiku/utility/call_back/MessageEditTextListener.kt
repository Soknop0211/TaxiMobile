package com.eazy.daiku.utility.call_back

import android.content.DialogInterface

interface MessageEditTextListener<T> {
    fun onResult(data: T?, dialogInterface: DialogInterface?)
}