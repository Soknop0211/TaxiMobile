package com.eazy.daiku.ui.about_us

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity

class AboutUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        initView()
        doAction()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.about_us),
            true
        )
        val imageLogo: ImageView = findViewById(R.id.img)
        if (BuildConfig.IS_WEGO_TAXI) {
            imageLogo.setImageResource(R.drawable.wego_logo_icon)
        } else if (BuildConfig.IS_CUSTOMER) {
            imageLogo.setImageResource(R.drawable.eazy_logo_blue)
        } else {
            imageLogo.setImageResource(R.drawable.eazy_black_logo)
        }

        val versionNameTv: TextView = findViewById(R.id.versionNameTv)
        val buildVersionTv: TextView = findViewById(R.id.buildVersionTv)
        versionNameTv.text =
            String.format("%s %s", "Version", BuildConfig.VERSION_NAME)
        buildVersionTv.text =
            String.format("(%s %s)", "Build", BuildConfig.BUILD_VERSION)
    }

    private fun doAction() {
        //term
        if (BuildConfig.IS_WEGO_TAXI) {
            findViewById<View>(R.id.action_terms_of_service_layout).setOnClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://eazybooking.asia/privacy-policy/wego_privacy_policy.html")
                )
                startActivity(browserIntent)
            }
            //privacy
            findViewById<View>(R.id.action_privacy_policy_layout).setOnClickListener {
                findViewById<View>(R.id.action_terms_of_service_layout).performClick()
            }
        }
    }
}