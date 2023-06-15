package com.eazy.daiku.ui.web_view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityWebViewEazyTaxiBinding
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity

class WebViewEazyTaxiActivity : BaseActivity() {

    private lateinit var binding: ActivityWebViewEazyTaxiBinding

    companion object {
        const val DATA_GOOGLE = "data_google"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewEazyTaxiBinding.inflate(layoutInflater)
        setContentView(binding.root)



        initView()
        initGoogleDataLoad()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onBackPressed() {
        // Pop the browser back stack or exit the activity
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.map),
            true
        )
        binding.webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            setGeolocationEnabled(true)
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.loadingView.root.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.loadingView.root.visibility = View.GONE

            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)

            }
        }
        binding.webView.webChromeClient = GeoWebChromeClient()

    }

    private fun initGoogleDataLoad() {
        if (intent.hasExtra(DATA_GOOGLE)) {
            val loadGoogle = intent.getStringExtra(DATA_GOOGLE) ?: ""
            binding.webView.loadUrl(loadGoogle)
        }
    }

    /**
     * WebChromeClient subclass handles UI-related calls
     * Note: think chrome as in decoration, not the Chrome browser
     */
    class GeoWebChromeClient : WebChromeClient() {
        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback
        ) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false)
        }
    }
}