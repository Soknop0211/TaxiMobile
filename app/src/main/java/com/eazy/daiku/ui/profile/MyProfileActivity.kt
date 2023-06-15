package com.eazy.daiku.ui.profile

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.eazy.daiku.BuildConfig
import  com.eazy.daiku.data.model.server_model.User
import  com.eazy.daiku.utility.*
import  com.eazy.daiku.utility.base.BaseActivity
import  com.eazy.daiku.utility.bottom_sheet.LanguageBottomSheetDialog
import  com.eazy.daiku.utility.custom.MessageUtils
import  com.eazy.daiku.utility.enumerable.LanguageSettings
import  com.eazy.daiku.utility.enumerable.VerifyPinEnum
import  com.eazy.daiku.utility.other.LocaleManager
import  com.eazy.daiku.utility.redirect.RedirectClass
import  com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.eazy.daiku.R
import com.eazy.daiku.data.model.HomeScreenModel
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.databinding.ActivityMprofileBinding
import com.eazy.daiku.utility.bottom_sheet.BiometricBottomSheetFragment
import com.eazy.daiku.utility.custom.StaticAirportTaxiAlertDialog
import com.eazy.daiku.utility.enumerable.HomeScreenActionEnum


class MyProfileActivity : BaseActivity() {

    internal val userInfoVM: UseCaseVm by viewModels {
        factory
    }
    internal lateinit var binding: ActivityMprofileBinding
    internal var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initObserved()
        drawDataInUI()
        doAction()
        doInitData()
        initViewTaxiWithCustomer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun doInitData() {
        if (hasUserInSharePreference().isEmpty()) {
            userInfoVM.fetchUserInfo()
        } else {
            reloadData()

        }
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            this,
            getString(R.string.profile),
            true
        )
    }

    private fun initViewTaxiWithCustomer() {
        if (BuildConfig.IS_CUSTOMER) {
            visibleViewTaxiWithCustomer(true)
        } else {
            visibleViewTaxiWithCustomer(false)
        }

    }

    private fun visibleViewTaxiWithCustomer(isCustomer: Boolean) {
        if (isCustomer) {
            binding.verifyContainer.visibility = View.GONE
            binding.containerAvailableStatus.visibility = View.GONE
            binding.actionIdentityKyc.visibility = View.GONE
        } else {
            binding.verifyContainer.visibility = View.VISIBLE
            binding.containerAvailableStatus.visibility = View.VISIBLE
            binding.actionIdentityKyc.visibility = View.VISIBLE


        }

    }

    private fun initUserVerified() {
        val isKycUser = user?.kycDocument?.status.equals(Config.KycDocStatus.Verified)
        val isKycPending = user?.kycDocument?.status.equals(Config.KycDocStatus.Pending)
        binding.verifyContainer.background = if (isKycUser) ContextCompat.getDrawable(
            this,
            R.drawable.verify_user_background_drawable
        ) else if (isKycPending) ContextCompat.getDrawable(
            this,
            R.drawable.pending_user_background_drawable
        )
        else ContextCompat.getDrawable(
            this,
            R.drawable.un_verify_user_background_drawable
        )
        binding.verifiedImg.setImageDrawable(
            when {
                isKycUser -> ContextCompat.getDrawable(
                    this,
                    R.drawable.verified
                )
                isKycPending -> ContextCompat.getDrawable(this, R.drawable.watting_icon)
                else -> ContextCompat.getDrawable(this, R.drawable.not_verified)
            }
        )
        binding.verifiedImg.setColorFilter(
            when {
                isKycUser -> ContextCompat.getColor(
                    this,
                    R.color.verified_color_text
                )
                isKycPending -> ContextCompat.getColor(this, R.color.orange_300)
                else -> ContextCompat.getColor(this, R.color.un_verified_color_text)
            }
        )
        binding.verifiedTv.setTextColor(
            when {
                isKycUser -> ContextCompat.getColor(
                    this,
                    R.color.verified_color_text
                )
                isKycPending -> ContextCompat.getColor(this, R.color.orange_300)
                else -> ContextCompat.getColor(this, R.color.un_verified_color_text)
            }
        )
        binding.verifiedTv.text = if (isKycUser) this.getString(R.string.verified)
        else getString(
            R.string.not_verified
        )

        if (isKycUser) binding.verifiedTv.text = getString(R.string.verified)
        else if (isKycPending) binding.verifiedTv.text = getString(R.string.pending)
        else binding.verifiedTv.text = getString(R.string.refused)
    }

    private fun initObserved() {
        userInfoVM.loadingUserLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        userInfoVM.dataUserLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let { saveUserToSharePreference(it) }
                user = respondState.data
                drawDataInUI()
                initUserVerified()
            } else {
                globalShowError(respondState.message)
                val available = 1
                binding.switchMtrAvailableStatus.isChecked = (user?.isAvailable ?: -1) == available
            }
        }
        userInfoVM.logoutUserInLiveData.observe(this) { respondState ->
            if (respondState.success) {
                RedirectClass.gotoLoginActivity(this)
            } else {
                globalShowError(respondState.message)
            }
        }

        binding.appBar.qrCode.visibility = View.GONE
    }

    private fun drawDataInUI() {
        binding.nameTv.text = user?.name ?: "N/A"
        binding.emailTv.text = user?.phone ?: "N/A"
        ImageHelper.loadImage(
            self(),
            user?.avatarUrl ?: "",
            R.drawable.default_profile,
            binding.profileImg
        )
        val available = 1
        if (user?.isAvailable != null) {
            binding.switchMtrAvailableStatus.visibility = View.VISIBLE
            binding.switchMtrAvailableStatus.isChecked = (user?.isAvailable ?: -1) == available
        } else {
            binding.switchMtrAvailableStatus.visibility = View.GONE
        }
    }

    private fun reloadData() {
        user = getUserUserToSharePreference()
        drawDataInUI()
        initUserVerified()
    }

    private fun doAction() {

        binding.profilePictureContainer.setOnClickListener {

//            StaticAirportTaxiAlertDialog.newInstance().show(
//                this.supportFragmentManager,
//                "StaticAirportTaxiAlertDialog"
//            )
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            if (hasInternetConnection()) {
                userInfoVM.fetchUserInfo()
            }
        }

        binding.actionChangePasswordLinearLayout.setOnClickListener {
            RedirectClass.gotoVerificationPinActivity(
                self(),
                getString(R.string.change_password),
                VerifyPinEnum.ChangePinScreen
            )
        }

        binding.signOutLinear.setOnClickListener {
            MessageUtils.showConfirm(
                self(),
                "",
                getString(R.string.confirm_to_sign_out)
            ) {
                it.dismiss()
                if (hasInternetConnection()) {
                    userInfoVM.logout()
                }
            }
        }

        binding.actionEditProfileLinearLayout.setOnClickListener {
            RedirectClass.gotoEditProfileActivity(this,
                object : BetterActivityResult.OnActivityResult<ActivityResult> {
                    override fun onActivityResult(result: ActivityResult) {
                        if (result.resultCode == RESULT_OK) {
                            userInfoVM.fetchUserInfo()
                        }
                    }
                })
        }

        binding.actionLanguageLinearLayout.setOnClickListener {
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

        binding.actionIdentityKyc.setOnClickListener {
            val kycHm = HashMap<String, Any>()
            kycHm["status"] = user?.kycDocument?.status.toString()
            kycHm["refused_msg"] = if (user?.kycDocument?.refuseMsg.toString() == "null"
            ) "" else user?.kycDocument?.refuseMsg.toString()
            RedirectClass.gotoIdentityVerificationActivity(
                self(),
                kycHm
            )
        }

        binding.verifyContainer.setOnClickListener {
            binding.actionIdentityKyc.performClick()
        }

        binding.switchMtrAvailableStatus.setOnClickListener { view ->
            if (hasInternetConnection()) {
                val bodyRequest = HashMap<String, Any>()
                val isAvailableKey = 1
                val notAvailableKey = 0
                bodyRequest["is_available"] =
                    if (user?.isAvailable == isAvailableKey) notAvailableKey else isAvailableKey
                userInfoVM.updateAvailableTaxi(bodyRequest)
            }
        }

        binding.actionEnableBiometricLinearLayout.setOnClickListener {
            BiometricBottomSheetFragment.newInstance()
                .show(supportFragmentManager, "BiometricBottomSheetFragment")
        }

        binding.aboutUsLinearLayout.setOnClickListener {
            RedirectClass.gotoAboutUsActivity(self())
        }
    }

}

