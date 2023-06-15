package com.eazy.daiku.utility.base

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.utility.call_back.BiometricListener
import java.util.concurrent.Executor

class BiometricCheck {

    private lateinit var listener: BiometricListener
    private lateinit var context: Context
    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null

    companion object {
        @JvmStatic
        fun newInstance(
            context: Context,
            listener: BiometricListener
        ) = BiometricCheck().apply {
            this.context = context
            this.listener = listener
            checkBiometric()
        }
    }

    private fun checkBiometric() {
        val biometricManager = BiometricManager.from(
            context
        )
        val error: String
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricDialog()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                error = "No biometric features available on this device"
                showToast(error)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                error = "Biometric features are currently unavailable."
                showToast(error)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                error = "The user hasn't associated any biometric credentials with their account."
                showToast(error)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                error = "Biometric features are currently security update required."
                showToast(error)
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                error = "Biometric features are currently unsupported."
                showToast(error)
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                error = "Biometric features are currently status unknown."
                showToast(error)
            }
        }
    }

    private fun showBiometricDialog() {
        //initialize everything needed for authentication
        initBiometrics()
        biometricPrompt!!.authenticate(promptInfo!!)
    }

    private fun initBiometrics() {
        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(
            (context as FragmentActivity?)!!,
            executor!!, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    listener.onFailed(errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    listener.onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("Authentication failed")
                    listener.onFailed("Authentication failed")
                }
            })

        var appName = ""
        if (BuildConfig.IS_WEGO_TAXI) {
            appName = "WeGo Driver"
        } else if (BuildConfig.IS_TAXI) {
            appName = "Eazy Taxi"
        } else if (BuildConfig.IS_CUSTOMER) {
            appName = "Eazy Customer"
        }

        //create prompt dialog
        promptInfo = PromptInfo.Builder()
            .setTitle(
                context.getString(R.string.biometric_title) + " " +
                        appName
            )
            .setSubtitle(context.getString(R.string.auth_with_biometric))
            .setNegativeButtonText(context.getString(R.string.action_cancel))
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }


    //show toast to user
    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}