package com.eazy.daiku.ui.withdraw

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.eazy.daiku.data.model.WithdrawMoneyRespondModel
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.databinding.ActivityEazyTaxiWithdrawWebviewBinding
import com.eazy.daiku.utility.service.MyBroadcastReceiver.Companion.whenWithdrawMoneySuccessFinishScreenWithdrawMoneyKey

class EazyTaxiWithdrawWebviewActivity : BaseActivity() {

    private val JAVASCRIPT_OBJ = "javascript_obj"
    private lateinit var binding: ActivityEazyTaxiWithdrawWebviewBinding
    private lateinit var withdrawMoneyRespondModel: WithdrawMoneyRespondModel
    private var verifyOtpUrl: String = ""
    private val onPaymentSuccess = "onPaymentSuccess"


    companion object {
        const val WithdrawMoneyRespondModelKey = "WithdrawMoneyRespondModelKey"
        const val WithdrawVerifyOtpUrlKey = "WithdrawVerifyOtpUrlKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEazyTaxiWithdrawWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initWebView()
        initWithdrawMoneyRespondModel()
        initLoadWebView()

    }


    fun configWebSetting() {
        val webSettings = binding.webView.settings
        webSettings.setJavaScriptEnabled(true)
        webSettings.setUseWideViewPort(true)
        webSettings.setAppCacheEnabled(false)
        webSettings.setDomStorageEnabled(true)
        webSettings.setLoadWithOverviewMode(true)
        webSettings.setAllowFileAccess(true)
        binding.webView.settings.pluginState = WebSettings.PluginState.ON
        webSettings.setAllowContentAccess(true)
        webSettings.setAllowUniversalAccessFromFileURLs(true)
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true)
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
    }

    private fun initWithdrawMoneyRespondModel() {
        withdrawMoneyRespondModel = WithdrawMoneyRespondModel()
        if (intent.hasExtra(WithdrawVerifyOtpUrlKey)) {
            verifyOtpUrl = intent.getStringExtra(WithdrawVerifyOtpUrlKey).toString()
        }
        if (intent.hasExtra(WithdrawMoneyRespondModelKey)) {
            val gsonStr = intent.getStringExtra(WithdrawMoneyRespondModelKey)
            gsonStr?.let {
                withdrawMoneyRespondModel =
                    GsonConverterHelper.getJsonObjectToGenericClass<WithdrawMoneyRespondModel>(
                        gsonStr
                    )
            }
        }
    }


    private fun initLoadWebView() {
        binding.webView.loadUrl(verifyOtpUrl)
        binding.webView.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)
    }

    private fun initWebView() {
        val webSettings: WebSettings = binding.webView.settings
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        configWebSetting()
        binding.webView.webChromeClient = webChromeClient
        binding.webView.webViewClient = webViewClient

    }

    val webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

        }

        override fun onShowFileChooser(
            mWebView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams,
        ): Boolean {

            return true
        }
    }


    val webViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.containerLoadingWeb.visibility=View.VISIBLE
            binding.progressBarWebView.isIndeterminate=true


        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            injectJavaScriptFunction()
            binding.containerLoadingWeb.visibility=View.GONE

        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?,
        ) {
            super.onReceivedError(view, request, error)

        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?,
        ) {
            super.onReceivedHttpError(view, request, errorResponse)

        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?,
        ) {
            super.onReceivedSslError(view, handler, error)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    private fun injectJavaScriptFunction() {
        binding.webView.loadUrl("javascript: window.WebPayJSBride.invoke = function(data) { $JAVASCRIPT_OBJ.onPaymentSuccess(data) }")

    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            "Withdraw",
            true
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun onPaymentSuccess(data: String?) {
            if (data == onPaymentSuccess){
                startBroadcastData(whenWithdrawMoneySuccessFinishScreenWithdrawMoneyKey)
                finish()
            }
        }
    }


}