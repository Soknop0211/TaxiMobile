package com.eazy.daiku.ui.forget_password

import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.enumerable.VerifyPinEnum
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityVerifyOtpCodeBinding
import com.eazy.daiku.utility.custom.DidnotReceiveTheCodeAlertDialog
import java.util.*
import java.util.concurrent.TimeUnit

class VerifyOtpCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityVerifyOtpCodeBinding
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }
    private var countDownTimer: CountDownTimer? = null
    private var globalOtpCodeView: String? = null
    private var otpToken: String? = ""

    companion object {
        const val OTP_TOKEN_KEY = "otp_token_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifyOtpCodeBinding.inflate(layoutInflater)
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
            getString(R.string.verify_otp),
            true
        )
        if (intent.hasExtra(OTP_TOKEN_KEY)) {
            otpToken = intent.getStringExtra(OTP_TOKEN_KEY)
        }
    }

    private fun initObserved() {
        userInfoVM.loadingUserLiveData.observe(this) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        userInfoVM.verifyOtpMutableLiveData.observe(this) {
            if (it.success) {
                RedirectClass.gotoChangeNewPasswordActivity(
                    self(),
                    globalOtpCodeView ?: "",
                    otpToken ?: "",
                    VerifyPinEnum.ForgetPasswordScreen
                )
            }
        }
        userInfoVM.requestOtpMutableLiveData.observe(this) {
            if (it.success) {
                startCountDownCode()
                Toast.makeText(self(), "Resend code success..!", Toast.LENGTH_SHORT).show()
            }
        }
        userInfoVM.globalErrorMutableLiveData.observe(this) {
            if (!it.success) {
                binding.codeOtpView.text = null
                globalShowError(it.message)
            }
        }
    }

    private fun doAction() {

        binding.actionSubmit.setOnClickListener {
            if (binding.codeOtpView.text?.length ?: 0 < 6) {
                globalShowError("Please fill the otp box")
            } else {
                if (hasInternetConnection()) {
                    val codeOtp: String = binding.codeOtpView.text.toString().trim()
                    if (codeOtp.length == 6) {
                        globalOtpCodeView = codeOtp.trim()
                        val requestBodyMap = HashMap<String, Any>()
                        requestBodyMap["otp"] = codeOtp.trim()
                        requestBodyMap["otp_token"] = otpToken ?: ""
                        userInfoVM.submitVerifyOtp(requestBodyMap)
                    }
                }
            }
        }

        binding.resentCodeTv.setOnClickListener {
            if (hasInternetConnection()) {
                val user = getUserUserToSharePreference()
                if (user != null) {
                    user.phone?.let {
                        binding.resentCodeTv.isEnabled = false
                        val body = HashMap<String, Any>()
                        body["phone"] = it.trim()
                        userInfoVM.submitRequestOtp(body)
                    }
                }
            }
        }

        binding.didNotReceiveCodeTv.setOnClickListener {
            DidnotReceiveTheCodeAlertDialog.newInstance()
                .show(supportFragmentManager, "DidnotReceiveTheCodeAlertDialog")
        }
    }

    private fun startCountDownCode() {
        try {
            binding.resentCodeTv.isEnabled = false
            countDownTimer = object : CountDownTimer(105000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val time = String.format(
                        Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                    )
                    binding.countDownTv.text = time
                    binding.resentCodeTv.post { binding.resentCodeTv.isEnabled = false }
                }

                override fun onFinish() {
                    runOnUiThread {
                        binding.countDownTv.text = getString(R.string.time_countdown)
                        binding.resentCodeTv.isEnabled = true
                    }
                }
            }.start()
        } catch (ignored: Exception) {
        }
    }


}