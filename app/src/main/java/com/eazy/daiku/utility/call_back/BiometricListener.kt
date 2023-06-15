package com.eazy.daiku.utility.call_back

interface BiometricListener {
    fun onSuccess()
    fun onFailed(msg: CharSequence)
}