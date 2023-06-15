package com.eazy.daiku.ui.login

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.databinding.ActivityLoginBinding
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.SimpleBaseActivity
import com.eazy.daiku.utility.bottom_sheet.LanguageBottomSheetDialog
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.enumerable.LanguageSettings
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.other.LocaleManager
import com.eazy.daiku.utility.permission_media.PermissionHelper

import com.eazy.daiku.utility.redirect.RedirectClass


import com.eazy.daiku.utility.view_model.user_case.LoginViewModel
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.parse.ParseObject
import com.parse.ParseUser

import kotlinx.coroutines.flow.collect

import kotlinx.coroutines.launch

import kotlin.collections.HashMap


class LoginActivity : SimpleBaseActivity() {
    val TAG = "LoginActivity__________"
    private val loginViewModel: LoginViewModel by viewModels {
        factory
    }
    private val userInfoVM: UseCaseVm by viewModels {
        factory
    }
    internal lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initConfigureLanguage()
        initObserved()
        doAction()
    }

    private fun initView() {
        UserDefaultSingleTon.newInstance?.hasMainActivity = self()
        val version = java.lang.String.format(
            "%s %s(%s)",
            getString(R.string.version),
            BuildConfig.VERSION_NAME,
            BuildConfig.BUILD_VERSION
        )
        binding.versionTv.text = version

        if (BuildConfig.IS_WEGO_TAXI) {
            binding.logoAppImg.setImageResource(R.drawable.wego_logo_icon)
            binding.appBarUi.setBackgroundColor(
                ContextCompat.getColor(
                    self(),
                    R.color.colorPrimary
                )
            )
            binding.logoAppImg.imageTintList =
                ContextCompat.getColorStateList(self(), R.color.white)
            binding.logoAppImg.scaleType = ImageView.ScaleType.CENTER_CROP
        } else if (BuildConfig.IS_CUSTOMER) {
            binding.logoAppImg.setImageResource(R.drawable.eazy_logo_white)
        } else {
            binding.logoAppImg.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.logoAppImg.setImageResource(R.drawable.eazy_location_black)
        }
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

                    //new code
                    accessToken?.let { token ->
                        EazyTaxiHelper.setSharePreference(this, Constants.Token.API_TOKEN, token)
                        getUserFromGson(jsonObject)?.let {
                            saveUserToSharePreference(it)
                            savePassWordUser(binding.passwordTf.text.toString())
                            RedirectClass.gotoMainActivity(self())
                        }
                    } ?: globalShowError("Token is null")
                    //for old code checking
                    /*accessToken?.let { token ->
                        EazyTaxiHelper.setSharePreference(this, Constants.Token.API_TOKEN, token)
                        val user = getUserUserToSharePreference()
                        if (user == null) {
                            userInfoVM.fetchUserInfo()
                        } else {
                            RedirectClass.gotoMainActivity(self())
                        }
                    } ?: globalShowError("Token is null")*/
                }
            } else {
                globalShowError(respondState.message)
            }
        }

        userInfoVM.dataUserLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let {
                    saveUserToSharePreference(it)
                    savePassWordUser(binding.passwordTf.text.toString())
                    RedirectClass.gotoMainActivity(self())
                } ?: globalShowError("Object user is null")
            } else {
                globalShowError(respondState.message)
            }
        }
        userInfoVM.loadingUserLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE

        }
        userInfoVM.requestOtpMutableLiveData.observe(this) {
            if (it.success) {
                it.data?.let { jsonElement ->
                    val jsonObject = jsonElement.asJsonObject
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

        binding.phoneNumberTf.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.phoneNumberTfLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }

        binding.passwordTf.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.passwordTfLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.actionLoginMtb.setOnClickListener {

            val phoneNumber = binding.phoneNumberTf.text.toString()
            if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length < Constants.PhoneConfig.PHONE_MIN_LENGTH ||
                phoneNumber.length > Constants.PhoneConfig.PHONE_MAX_LENGTH ||
                !EazyTaxiHelper.phoneNumberValidate(phoneNumber)
            ) {
                binding.phoneNumberTfLayout.error =
                    getString(R.string.please_enter_a_valid_mobile_number)
            } else if (TextUtils.isEmpty(
                    binding.passwordTf.text.toString().trim()
                ) || binding.passwordTf.text.toString().length < 6
            ) {
                binding.phoneNumberTfLayout.error = null
                binding.passwordTfLayout.error =
                    getString(R.string.please_enter_password)
            } else {
                binding.phoneNumberTfLayout.error = null
                binding.passwordTfLayout.error = null
                if (hasInternetConnection()) {
                    val requestBodyMap = HashMap<String, Any>()
                    requestBodyMap["phone"] = binding.phoneNumberTf.text.toString().trim()
                    requestBodyMap["password"] = binding.passwordTf.text.toString().trim()
                    loginViewModel.login(requestBodyMap)
                }
            }
        }

        binding.singUpTv.setOnClickListener {
            RedirectClass.gotoSignUpActivity(this)
        }

        binding.languageImg.setOnClickListener {
            LanguageBottomSheetDialog.newInstance {
                when (it) {
                    LanguageSettings.English -> {
                        setNewLocale(self(), LocaleManager.ENGLISH)
                    }
                    LanguageSettings.Khmer -> {
                        setNewLocale(self(), LocaleManager.KHMER)
                    }
                    LanguageSettings.China -> {
                        setNewLocale(self(), LocaleManager.CHINA)
                    }
                }

            }.show(supportFragmentManager, LanguageBottomSheetDialog::class.java.name)
        }

        binding.forgetPwdTv.setOnClickListener {
            RedirectClass.gotoForgetPwdActivity(self())
        }

        binding.logoAppImg.setOnClickListener {

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

    //configure language
    private fun initConfigureLanguage() {
        val flags: HashMap<String, Int> = object : HashMap<String, Int>() {
            init {
                put(LocaleManager.ENGLISH, R.drawable.flag_en)
                put(LocaleManager.KHMER, R.drawable.flag_kh)
                put(LocaleManager.CHINA, R.drawable.flag_china)
            }
        }
        val languageKey = LocaleManager.getLanguagePref(self())
        val resId = flags[languageKey]
        Glide.with(this)
            .load(resId)
            .error(R.drawable.language_white_drawable)
            .into(binding.languageImg)

    }

}


