package com.eazy.daiku.ui.verification_pin_code

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.compose.ui.unit.TextUnit
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.databinding.ActivityVerificationPinBinding
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.BiometricCheck
import com.eazy.daiku.utility.base.SimpleBaseActivity
import com.eazy.daiku.utility.call_back.BiometricListener
import com.eazy.daiku.utility.enumerable.VerifyPinEnum
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.eazy.daiku.utility.view_model.user_case.LoginViewModel
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.util.*


class VerificationPinActivity : SimpleBaseActivity() {

    private lateinit var binding: ActivityVerificationPinBinding
    private val loginViewModel: LoginViewModel by viewModels {
        factory
    }
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }
    private var verifyPinEnum: VerifyPinEnum = VerifyPinEnum.Other
    private var pinCode: String = ""
    private var code = ""

    companion object {
        const val CHANGE_TITLE = "change_title"
        const val whoCallActivity = "who_call_activity"
        const val biometricKey = "biometricKey"
        const val bookingCode = "bookingCode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initObserved()
        doAction()
    }

    private fun initObserved() {
        loginViewModel.loadingLoginLiveData.observe(this) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        loginViewModel.dataLoginLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let {
                    val jsonObject = respondState.data.asJsonObject
                    val accessToken = jsonObject["access_token"].asString
                    accessToken?.let { token ->
                        EazyTaxiHelper.setSharePreference(this, Constants.Token.API_TOKEN, token)
                        //new code
                        getUserFromGson(jsonObject)?.let {
                            saveUserToSharePreference(it)
                        }
                    }

                    when (verifyPinEnum) {
                        VerifyPinEnum.SplashScreen -> {
                            if (!TextUtils.isEmpty(code)) {
                                val intentAcceptOrder =
                                    Intent(MyBroadcastReceiver.notificationAcceptOrder)
                                intentAcceptOrder.putExtra(
                                    MyBroadcastReceiver.notificationAcceptOrder, code
                                )
                                sendBroadcast(intentAcceptOrder)

                                BaseActivity.bookingCode = code
                            }
                            RedirectClass.gotoMainActivity(self())
                        }

                        VerifyPinEnum.ChangePinScreen -> {
                            RedirectClass.gotoChangeNewPasswordActivity(
                                self(),
                                pinCode,
                                VerifyPinEnum.VerificationPinScreen
                            )
                        }

                        else -> {
                            RedirectClass.gotoLoginActivity(self())
                        }
                    }
                    finish()
                }
            } else {
                binding.otpView.text = null
                globalShowError(respondState.message)
            }
        }

        userInfoVM.loadingUserLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }

        userInfoVM.dataUserLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let {
                    saveUserToSharePreference(it)
                    val user = it
                    user.phone?.let { phoneNumber ->
                        val requestBodyMap = HashMap<String, Any>()
                        requestBodyMap["phone"] = phoneNumber.trim()
                        requestBodyMap["password"] = pinCode
                        loginViewModel.login(requestBodyMap)
                    } ?: globalShowError("Can not verify your password please login again.")
                }
            } else {
                globalShowError(respondState.message)
            }
        }

        userInfoVM.requestOtpMutableLiveData.observe(this) {
            if (it.success) {
                it.data?.let { json ->
                    val jsonObject = json.asJsonObject
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

    private fun getUserFromGson(jsonObject: JsonObject): User? {
        try {
            val userObject = jsonObject["user"].asJsonObject
            return GsonConverterHelper.getJsonObjectToGenericClass<User>(userObject.toString())
        } catch (jsonSyntax: JsonSyntaxException) {

        }
        return null
    }

    private fun initView() {
        var title = getString(R.string.verification)
        if (intent.hasExtra(CHANGE_TITLE)) {
            title = intent.getStringExtra(CHANGE_TITLE) ?: title
        }
        binding.titleAppBarTv.text = title + ""

        binding.versionNameTv.text = String.format(
            "%s %s(%s)",
            getString(R.string.version),
            BuildConfig.VERSION_NAME,
            BuildConfig.BUILD_VERSION
        )

        if (intent.hasExtra(whoCallActivity)) {
            verifyPinEnum = intent.getSerializableExtra(whoCallActivity) as VerifyPinEnum
        }

        if (intent.hasExtra(biometricKey) && intent.getBooleanExtra(biometricKey, false)) {
            getBiometric()?.let {
                if (it == "1") {
                    BiometricCheck.newInstance(
                        this,
                        faceBiometricListener
                    )
                }
            }
        }
        if (intent.hasExtra(bookingCode)) {
            code = intent.getStringExtra(bookingCode).toString()
            AppLOGG.d("DATABOOKINGCODE", "bookingCode-> " + code)
        }

    }

    private fun doAction() {
        binding.actionForgotPin.setOnClickListener {
            if (hasInternetConnection()) {
                val user = getUserUserToSharePreference()
                if (user != null) {
                    user.phone?.let {
                        val body = HashMap<String, Any>()
                        body["phone"] = it.trim()
                        userInfoVM.submitRequestOtp(body)
                    }
                }
            }
        }

        binding.otpView.setOtpCompletionListener { code ->
            savePassWordUser(code)
            if (hasInternetConnection()) {
                val user = getUserUserToSharePreference()
                if (user == null) {
                    userInfoVM.fetchUserInfo()
                } else {
                    user.phone?.let { phoneNumber ->
                        if (code.length == 6) {
                            pinCode = code.toString().trim()
                            val requestBodyMap = HashMap<String, Any>()
                            requestBodyMap["phone"] = phoneNumber.trim()
                            requestBodyMap["password"] = code.toString().trim()
                            loginViewModel.login(requestBodyMap)
                        }
                    } ?: globalShowError("Can not verify your password please login again.")
                }
            }
        }

        binding.loginTv.setOnClickListener {
            RedirectClass.gotoLoginActivity(self())
        }

        binding.numericKeyboard.keySpecialListener = View.OnClickListener { v: View? ->
            binding.numericKeyboard.field?.postDelayed(
                Runnable {
                    binding.numericKeyboard.let {
                        it.field?.let { editText ->
                            editText.setText("")
                        }
                    }
                }, 1
            )
        }
    }

    private var faceBiometricListener: BiometricListener = object : BiometricListener {
        override fun onSuccess() {
            if (!TextUtils.isEmpty(code)) {
                val intentAcceptOrder = Intent(MyBroadcastReceiver.notificationAcceptOrder)
                intentAcceptOrder.putExtra(
                    MyBroadcastReceiver.notificationAcceptOrder, code
                )
                sendBroadcast(intentAcceptOrder)
            }
            RedirectClass.gotoMainActivity(self())
        }

        override fun onFailed(msg: CharSequence) {

        }

    }
}