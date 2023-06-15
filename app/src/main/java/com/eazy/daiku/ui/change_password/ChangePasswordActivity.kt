package com.eazy.daiku.ui.change_password

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.enumerable.VerifyPinEnum
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityChangePasswordBinding


class ChangePasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private var pinCodeOrOtp: String = ""
    private var otpToken: String = ""
    private var verifyPinEnum: VerifyPinEnum = VerifyPinEnum.Other
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }

    companion object {
        const val PinCodeKey = "pin_code_key"
        const val Otp_token_key = "otp_token_key"
        const val ScreenThatRequestKey = "screen_that_request_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intiView()
        initObserved()
        doAction()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun intiView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.create_new_password),
            true
        )
        if (intent.hasExtra(PinCodeKey)) {
            pinCodeOrOtp = intent.getStringExtra(PinCodeKey) ?: ""
        }

        if (intent.hasExtra(Otp_token_key)) {
            otpToken = intent.getStringExtra(Otp_token_key) ?: ""
        }

        if (intent.hasExtra(ScreenThatRequestKey)) {
            verifyPinEnum = intent.getSerializableExtra(ScreenThatRequestKey) as VerifyPinEnum
        }

    }

    private fun initObserved() {
        userInfoVM.changePwdValidate.observe(this) {
            if (it.hasDoneValidate == true) {
                val requestBody = HashMap<String, Any>()
                requestBody["password"] =
                    binding.newPasswordTextInputEditText.text.toString().trim()
                requestBody["password_confirmation"] =
                    binding.confirmNewPasswordTextInputEditText.text.toString().trim()

                when (verifyPinEnum) {
                    VerifyPinEnum.VerificationPinScreen -> {
                        requestBody["current_password"] = pinCodeOrOtp
                        userInfoVM.submitChangePwd(
                            requestBody
                        )
                    }
                    VerifyPinEnum.ForgetPasswordScreen -> {
                        requestBody["otp_token"] = otpToken
                        requestBody["otp"] = pinCodeOrOtp
                        userInfoVM.submitChangePasswordByOtp(
                            requestBody
                        )
                    }
                    else -> {
                        RedirectClass.gotoMainActivity(self())
                    }
                }

            } else {
                when {
                    it.newPwd != null -> {
                        binding.newPasswordTextInputLayout.error = getString(it.newPwd ?: -1)
                    }
                    it.confirmPwd != null -> {
                        binding.confirmNewPasswordTextInputLayout.error =
                            getString(it.confirmPwd)
                    }
                    it.pwdNotMatch != null -> {
                        globalShowError(getString(it.pwdNotMatch))
                        binding.confirmNewPasswordTextInputLayout.error = null
                        binding.newPasswordTextInputLayout.error = null
                    }
                    else -> {
                        binding.confirmNewPasswordTextInputLayout.error = null
                        binding.newPasswordTextInputLayout.error = null
                    }
                }
            }
        }

        userInfoVM.submitChangePasswordLoadingMutableLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }

        userInfoVM.submitChangePasswordMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                globalShowSuccessWithDismiss(
                    self(),
                    getString(R.string.chang_successfully)
                ) {
                    RedirectClass.gotoMainActivity(self())
                }
            }
        }

        userInfoVM.submitChangePasswordByOtpMutableLiveData.observe(this) {
            if (it.success) {
                globalShowSuccessWithDismiss(
                    self(),
                    getString(R.string.chang_successfully)
                ) {
                    RedirectClass.gotoLoginActivity(self())
                }
            }
        }

        userInfoVM.globalErrorMutableLiveData.observe(this) {
            if (!it.success) {
                globalShowError(it.message)
            }
        }
    }

    private fun doAction() {
        binding.actionSubmitPassword.setOnClickListener {
            userInfoVM.validateField(
                binding.newPasswordTextInputEditText.text.toString(),
                binding.confirmNewPasswordTextInputEditText.text.toString()
            )
        }
        //new
        binding.newPasswordTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.newPasswordTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        //old
        binding.confirmNewPasswordTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.confirmNewPasswordTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }

    }

}