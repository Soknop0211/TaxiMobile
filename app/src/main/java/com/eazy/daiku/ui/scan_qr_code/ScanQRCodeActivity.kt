package com.eazy.daiku.ui.scan_qr_code

import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import androidx.activity.viewModels
import com.budiyev.android.codescanner.CodeScanner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.QrCodeVm
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityScanQrcodeBinding
import com.google.zxing.Result
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.lang.Exception


class ScanQRCodeActivity : BaseActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: ActivityScanQrcodeBinding
    private val scanQrVm: QrCodeVm by viewModels {
        factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initObserved()
        doAction()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }

    override fun onDestroy() {
        super.onDestroy()
        codeScanner.stopPreview()
    }

    private fun initView() {
        EazyTaxiHelper.setUpTitleAppBar(
            self(),
            getString(R.string.scan_qr),
            true
        )
        startCameraQr()
    }

    private fun initObserved() {
        scanQrVm.loadingScanQrMutableLiveData.observe(this) { hasLoading ->
            binding.loadingView.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }

        scanQrVm.qrCodeDataMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let { qrCodeRespond ->
                    RedirectClass.gotoMapPreviewActivity(
                        self(),
                        GsonConverterHelper.convertGenericClassToJson(qrCodeRespond)
                    )
                    finish()
                } ?: showErrorCover(true, "Invalid data")
            } else {
                showErrorCover(true, respondState.message)
            }
        }
    }

    private fun doAction() {
        binding.uploadQrLinearLayout.setOnClickListener {
            if (PermissionHelper.hasExternalStoragePermission(self())) {
                chooseGalleryImage(easyCallBackImage)
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        chooseGalleryImage(easyCallBackImage)
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Write External Storage Deny"
                        )
                    }
                }
            }
        }

        binding.errorView.rescanMtcardView.setOnClickListener {
            showErrorCover(false)
        }
    }

    private fun startCameraQr() {
        codeScanner = CodeScanner(self(), binding.codeScannerView)
        codeScanner.setDecodeCallback { result: Result ->
            runOnUiThread {
                if (result.text.isEmpty()) {
                    showErrorCover(true, getString(R.string.invalid_chat_qr_code))
                } else {
                    invokeQrCode(result.text)
                }
            }
        }
    }

    private fun invokeQrCode(qrText: String) {

        if (PermissionHelper.hasCOARSEAndFINELocationPermission(self())) {
            codeScanner.stopPreview()
            try {
                if (URLUtil.isValidUrl(qrText)) {
                    val uri = Uri.parse(qrText)
                    val code = uri.getQueryParameter("code")
                    if (code == null) {
                        showErrorCover(true, getString(R.string.invalid_chat_qr_code))
                    } else {
                        scanQrVm.fetchDataQrCode(code)
                    }
                } else {
                    scanQrVm.fetchDataQrCode(qrText)
                }

            } catch (ex: Exception) {
                showErrorCover(true, getString(R.string.invalid_chat_qr_code))
            }
        } else {
            PermissionHelper.requestMultiPermission(
                self(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) { hasPermission ->
                if (hasPermission) {
                    try {
                        codeScanner.stopPreview()
                        val uri = Uri.parse(qrText)
                        val code = uri.getQueryParameter("code")
                        if (code.isNullOrEmpty()) {
                            if (qrText.isNullOrEmpty()) {
                                codeScanner.startPreview()
                            } else {
                                scanQrVm.fetchDataQrCode(qrText)
                            }
                        } else {
                            scanQrVm.fetchDataQrCode(code)
                        }
                    } catch (ex: Exception) {
                        showErrorCover(true, getString(R.string.invalid_chat_qr_code))
                    }
                } else {
                    MessageUtils.showError(
                        self(),
                        null,
                        "Permission Deny!"
                    )
                }
            }
        }

    }

    private fun onReturnUri(resultUri: Uri?) {
        binding.loadingView.root.visibility = View.VISIBLE
        codeScanner.stopPreview()
        resultUri?.let {
            Glide.with(this)
                .asBitmap()
                .load(resultUri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        binding.loadingView.root.visibility = View.GONE
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?,
                    ) {
                        binding.loadingView.root.visibility = View.GONE
                        val resultText = ImageHelper.detectQrImageToText(resource)
                        if (resultText?.equals("") == true || resultText == null) {
                            showErrorCover(true, getString(R.string.no_qr_detect))
                        } else {
                            invokeQrCode(resultText)
                        }
                    }

                })
        }
    }

    private fun showErrorCover(
        isShow: Boolean,
        errorMsg: String? = "---",
    ) {
        if (isShow) {
            codeScanner.stopPreview()
            binding.errorView.root.visibility = View.VISIBLE
            binding.uploadQrLinearLayout.visibility = View.GONE

            binding.errorView.errorMsgTv.text = errorMsg
        } else {
            codeScanner.startPreview()
            binding.uploadQrLinearLayout.visibility = View.VISIBLE
            binding.errorView.root.visibility = View.GONE

        }
    }

    private val easyCallBackImage: EasyImage.Callbacks = object : EasyImage.Callbacks {
        override fun onCanceled(source: MediaSource) {
        }

        override fun onImagePickerError(error: Throwable, source: MediaSource) {

        }

        override fun onMediaFilesPicked(
            imageFiles: Array<MediaFile>,
            source: MediaSource,
        ) {
            if (imageFiles.isNotEmpty()) {
                val resultUri = Uri.fromFile(imageFiles[0].file)
                onReturnUri(resultUri)
            }
        }

    }


}