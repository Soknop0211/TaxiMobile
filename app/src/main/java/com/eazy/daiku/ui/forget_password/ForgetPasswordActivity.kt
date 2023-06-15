package com.eazy.daiku.ui.forget_password

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityForgetPasswordBinding
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.base.SimpleBaseActivity
import com.eazy.daiku.utility.extension.toPx

import java.util.HashMap

class ForgetPasswordActivity : SimpleBaseActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
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
            getString(R.string.forget_password),
            true
        )
        if (BuildConfig.IS_WEGO_TAXI) {
            binding.logoAppImg.setImageResource(R.drawable.wego_logo_icon)
        } else if (BuildConfig.IS_CUSTOMER) {
            binding.logoAppImg.setImageResource(R.drawable.eazy_logo_blue)
        } else {
            binding.logoAppImg.setImageResource(R.drawable.eazy_black_logo)
        }
    }

    private fun initObserved() {
        userInfoVM.loadingUserLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        userInfoVM.requestOtpMutableLiveData.observe(this) { it ->
            if (it.success) {
                it.data?.let {
                    val jsonObject = it.asJsonObject
                    val otpToken = jsonObject["otp_token"].asString
                    RedirectClass.gotoVerifyOtpCodeActivity(self(), otpToken)
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
        binding.actionSubmit.setOnClickListener {
            if (hasInternetConnection()) {
                val phoneNumber = binding.phoneNumberTf.text.toString()
                if (phoneNumber.isEmpty() ||
                    phoneNumber.length < Constants.PhoneConfig.PHONE_MIN_LENGTH ||
                    phoneNumber.length > Constants.PhoneConfig.PHONE_MAX_LENGTH ||
                    !EazyTaxiHelper.phoneNumberValidate(phoneNumber)
                ) {
                    binding.phoneNumberTfLayout.error =
                        getString(R.string.please_enter_a_valid_mobile_number)
                } else {
                    binding.phoneNumberTfLayout.error = null
                    val body = HashMap<String, Any>()
                    body["phone"] = binding.phoneNumberTf.text.toString().trim()
                    userInfoVM.submitRequestOtp(body)
                }
            }
        }

        binding.phoneNumberTf.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.phoneNumberTfLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
    }

}