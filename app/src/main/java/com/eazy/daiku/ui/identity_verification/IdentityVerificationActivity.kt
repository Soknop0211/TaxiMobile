package com.eazy.daiku.ui.identity_verification

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.eazy.daiku.BuildConfig
import  com.eazy.daiku.utility.Config
import  com.eazy.daiku.utility.EazyTaxiHelper
import  com.eazy.daiku.utility.base.BaseActivity
import  com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityIdentityVerificationBinding


import java.util.HashMap

class IdentityVerificationActivity : BaseActivity() {

    lateinit var binding: ActivityIdentityVerificationBinding

    companion object {
        const val KycDataKey = "kyc_data_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIdentityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initCurrentStatusUser()
        doAction()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            this,
            getString(R.string.identity_verification),
            true
        )

        val kycTitle = String.format(
            getString(R.string.as_per_guidelines_of_s_all_customers_of_kyc),
            BuildConfig.APP_NAME + ""
        )
        binding.kycTv.text = kycTitle
    }

    private fun initCurrentStatusUser() {
        if (intent.hasExtra(KycDataKey)) {
            val kycHm: HashMap<String, Any> =
                intent.getSerializableExtra(KycDataKey) as HashMap<String, Any>

            val refusedMsg = kycHm["refused_msg"].toString()
            val verifiedStatus = kycHm["status"].toString() == Config.KycDocStatus.Verified
            val pendingStatus = kycHm["status"].toString() == Config.KycDocStatus.Pending
            val refusedStatus = kycHm["status"].toString() == Config.KycDocStatus.Refused

            if (verifiedStatus) {
                binding.kycImg.setBackgroundResource(
                    R.drawable.ic_smart_success
                )
                binding.kycTextTv.text = getString(R.string.kyc_verified)
                binding.actionReSubmitAgain.visibility = View.GONE
            } else if (pendingStatus) {
                binding.kycImg.setBackgroundResource(
                    R.drawable.watting_icon
                )
                binding.kycTextTv.text = getString(R.string.kyc_pending)
                binding.actionReSubmitAgain.visibility = View.GONE
            } else if (refusedStatus) {
                binding.kycImg.setBackgroundResource(
                    R.drawable.close_img
                )
                binding.kycTextTv.text = refusedMsg.ifEmpty { "KYC Refused." }
                binding.actionReSubmitAgain.visibility = View.VISIBLE
            } else {
                binding.kycImg.visibility = View.GONE
                binding.kycTextTv.visibility = View.GONE
                binding.actionReSubmitAgain.visibility = View.GONE
            }

        }
    }

    private fun doAction() {
        binding.actionReSubmitAgain.setOnClickListener {
            RedirectClass.gotoUploadDocsActivity(
                self(),
                true
            )
        }

    }

}