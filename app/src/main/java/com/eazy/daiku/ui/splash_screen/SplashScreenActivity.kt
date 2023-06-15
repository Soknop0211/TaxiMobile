package com.eazy.daiku.ui.splash_screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.enumerable.VerifyPinEnum
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.databinding.ActivitySplashScreenBinding
import com.eazy.daiku.ui.verification_pin_code.VerificationPinActivity
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.SimpleBaseActivity
import com.eazy.daiku.utility.extension.toPx
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.service.ExitAppService
import com.eazy.daiku.utility.view_model.user_case.LoginViewModel
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException

class
SplashScreenActivity : SimpleBaseActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }
    private val loginViewModel: LoginViewModel by viewModels {
        factory
    }
    private var pinCode: String = ""
    private var bookingCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserDefaultSingleTon.newInstance?.hasMainActivity = self()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra(VerificationPinActivity.bookingCode)) {
            bookingCode = intent.getStringExtra(VerificationPinActivity.bookingCode).toString()
        }
        //   binding.progressBarSplash.visibility = View.VISIBLE
        //binding.progressBarSplash.setProgress(binding.progressBarSplash.width/3,true)
        Handler(Looper.getMainLooper()).postDelayed({
            if (hasLogin()) {
                if (BuildConfig.IS_TAXI || BuildConfig.IS_WEGO_TAXI) {
                    RedirectClass.gotoVerificationPinActivity(
                        self(),
                        true,
                        VerifyPinEnum.SplashScreen,
                        bookingCode
                    )
                } else {
                    if (hasInternetConnection()) {
                        val passWord = getPassWordUser()
                        val user = getUserUserToSharePreference()
                        if (!TextUtils.isEmpty(passWord)) {
                            if (user == null) {
                                userInfoVM.fetchUserInfo()
                            } else {
                                user.phone?.let { phoneNumber ->
                                    if (passWord?.length == 6) {
                                        pinCode = passWord.toString().trim()
                                        val requestBodyMap = HashMap<String, Any>()
                                        requestBodyMap["phone"] = phoneNumber.trim()
                                        requestBodyMap["password"] = passWord.toString().trim()
                                        loginViewModel.login(requestBodyMap)
                                    }
                                }
                                    ?: globalShowError("Can not verify your password please login again.")
                            }
                        } else {
                            RedirectClass.gotoVerificationPinActivity(
                                self(),
                                true,
                                VerifyPinEnum.SplashScreen,
                                bookingCode
                            )
                        }

                    }
                }

            } else {
                RedirectClass.gotoLoginActivity(self())
            }
        }, 3000)

        if (BuildConfig.IS_WEGO_TAXI) {
            binding.splashScreenImg.setImageResource(R.drawable.wego_logo_icon)
//            binding.splashScreenImg.layoutParams.width = 180.toPx()
//            binding.splashScreenImg.layoutParams.height = 180.toPx()

        } else if (BuildConfig.IS_CUSTOMER) {
            binding.splashScreenImg.setImageResource(R.drawable.eazy_logo_blue)
            binding.splashScreenImg.layoutParams.width = 180.toPx()
            binding.splashScreenImg.layoutParams.height = 180.toPx()
        } else {
            binding.splashScreenImg.setImageResource(R.drawable.eazy_black_logo)
        }

        initObserver()
    }

    fun initObserver() {
        userInfoVM.dataUserLiveData.observe(this) { respondState ->
            if (respondState.success) {
                pinCode = getPassWordUser().toString()
                respondState.data?.let {
                    saveUserToSharePreference(it)
                    val user = it
                    user.phone?.let { phoneNumber ->
                        val requestBodyMap = java.util.HashMap<String, Any>()
                        requestBodyMap["phone"] = phoneNumber.trim()
                        requestBodyMap["password"] = pinCode
                        loginViewModel.login(requestBodyMap)
                    } ?: globalShowError("Can not verify your password please login again.")
                }
            } else {
                globalShowError(respondState.message)
            }
        }

        loginViewModel.dataLoginLiveData.observe(this) { respondState ->
            if (respondState.success) {
                Handler(Looper.getMainLooper()).postDelayed({

                    respondState.data?.let {
                        val jsonObject = respondState.data.asJsonObject
                        val accessToken = jsonObject["access_token"].asString
                        accessToken?.let { token ->
                            EazyTaxiHelper.setSharePreference(
                                this,
                                Constants.Token.API_TOKEN,
                                token
                            )
                            //new code
                            getUserFromGson(jsonObject)?.let {
                                saveUserToSharePreference(it)
                            }
                        }

                        RedirectClass.gotoMainActivity(self())
                        binding.progressBarSplash.visibility = View.GONE
                        finish()
                    }

                }, 5000)
            } else {
                globalShowError(respondState.message)
            }

        }

        loginViewModel.loadingLoginLiveData.observe(this) { hasLoading ->
            binding.progressBarSplash.visibility = View.VISIBLE
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

    private fun hasLogin(): Boolean {
        val hasData = EazyTaxiHelper.getSharePreference(self(), Constants.Token.API_TOKEN)
        return hasData.isNotEmpty()
    }


}