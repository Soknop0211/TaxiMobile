package com.eazy.daiku.ui.customer.web_payment

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.webkit.*
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityWebPayBinding
import com.eazy.daiku.ui.customer.model.WebPayRespondModel
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.other.AppLOGG
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.service.MyBroadcastReceiver
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson

class WebPayActivity : BaseActivity() {
    private lateinit var binding: ActivityWebPayBinding
    private var dataWebViewRespond: WebPayRespondModel? = null
    private lateinit var linearProgressIndicator: LinearProgressIndicator
    private var hasKessChatInstall: Boolean = false
    private var hasAcledaInstall: Boolean = false
    private var hasAbaInstall: Boolean = false
    private var hasSpnInstall: Boolean = false
    private val JAVASCRIPT_OBJ = "javascript_obj"
    private val intentFilter = IntentFilter()



    init {
        intentFilter.addAction(MyBroadcastReceiver.customBroadcastKey)
    }

    companion object {
        const val dataJsonWebViewKey = "dataJsonWebViewKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebPayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.web_pay),
            true
        )
        hasKessChatInstall =
/* isAppInstalled(Config.TypeDeepLink.kessChatDeepLink, packageManager) ==*/ true
        hasAbaInstall = isAppInstalled(Config.TypeDeepLink.abaDeepLink, packageManager) == true

        hasAcledaInstall =
            isAppInstalled(Config.TypeDeepLink.acledaDeepLink, packageManager) == true
        hasSpnInstall =
            isAppInstalled(Config.TypeDeepLink.sathapanaDeepLink, packageManager) == true


        linearProgressIndicator = findViewById(R.id.progress_bar_web_view)
        dataWebViewRespond = WebPayRespondModel()
        if (intent != null && intent.hasExtra(dataJsonWebViewKey)) {
            val dataJson = intent.getStringExtra(dataJsonWebViewKey)
            dataWebViewRespond =
                GsonConverterHelper.getJsonObjectToGenericClass<WebPayRespondModel>(dataJson)
        }

        binding.appBarLayout.qrCode.visibility = View.GONE


        binding.webPay.addJavascriptInterface(JavaScriptInterface(),
            JAVASCRIPT_OBJ)
        val webSettings: WebSettings = binding.webPay.settings
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webPay.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.setAppCacheEnabled(true)
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.pluginState = WebSettings.PluginState.ON
        webSettings.allowContentAccess = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.loadsImagesAutomatically = true

        binding.webPay.webChromeClient = webChromeClient
        binding.webPay.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.loadingLogo.root.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                injectJavaScriptFunction()
                binding.loadingLogo.root.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse,
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (errorResponse.statusCode != 200) {
                    binding.viewError.root.visibility = View.VISIBLE
                } else {
                    binding.viewError.root.visibility = View.GONE
                }
            }

//            override fun onReceivedSslError(
//                view: WebView,
//                handler: SslErrorHandler,
//                error: SslError,
//            ) {
//                handler.proceed()
//            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val authority =
                    if (!TextUtils.isEmpty(request.url.toString()) && !TextUtils.isEmpty(request.url.authority)) request.url.authority else ""
                val scheme =
                    if (!TextUtils.isEmpty(request.url.toString()) && !TextUtils.isEmpty(request.url.scheme)) request.url.scheme else ""
                if (authority == Config.TypeScheme.kessChatScheme) {
                    if (hasKessChatInstall) {
                        RedirectClass.openDeepLink(self(), request.url)
                    } else {
                        RedirectClass.gotoPlayStore(self(), Config.TypeDeepLink.kessChatDeepLink)
                    }
                    return true
                } else if (scheme == Config.TypeScheme.abaScheme) {
                    if (hasAbaInstall) {
                        RedirectClass.openDeepLink(self(), request.url)
                    } else {
                        RedirectClass.gotoPlayStore(self(), Config.TypeDeepLink.abaDeepLink)
                    }
                } else if (scheme == Config.TypeScheme.acledaScheme) {
                    if (hasAcledaInstall) {
                        RedirectClass.openDeepLink(self(), request.url)
                    } else {
                        RedirectClass.gotoPlayStore(self(), Config.TypeDeepLink.acledaDeepLink)
                    }
                } else if (scheme == Config.TypeScheme.spnScheme) {
                    if (hasSpnInstall) {
                        RedirectClass.openDeepLink(self(), request.url)
                    } else {
                        RedirectClass.gotoPlayStore(self(), Config.TypeDeepLink.sathapanaDeepLink)
                    }
                }
                val mUrl: String = request.url.toString()
                return !(mUrl.startsWith("http:") || mUrl.startsWith("https://"))
            }
        }

        dataWebViewRespond?.paymentLink?.let { binding.webPay.loadUrl(it) }
    }

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            linearProgressIndicator.setProgress(newProgress, true)
        }
    }

    private fun isAppInstalled(packageName: String, packageManager: PackageManager): Boolean? {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun textFromWeb(method: String?, data: String?) {
            if (!TextUtils.isEmpty(method) && method.equals("onPaymentSuccess")) {
                dataWebViewRespond?.qrCodeUrl?.let {
                    RedirectClass.gotoPaymentCompleteActivity(self(),
                        it)
                }
            }
        }
    }

    private fun injectJavaScriptFunction() {
        binding.webPay.loadUrl("javascript: window.WebPayJSBride.invoke = function(method, data) { $JAVASCRIPT_OBJ.textFromWeb(method, data) }")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(myBroadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadcastReceiver)
    }

    private var myBroadcastReceiver = object : MyBroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            super.onReceive(p0, p1)
            if (p1 != null) {
                when {
                    p1.hasExtra(finishWhenPaymentCompletedKey) -> {
                        startBroadcastData(finishWhenPaymentCompletedKey)
                        finish()
                    }
                }
            }
        }
    }


}
