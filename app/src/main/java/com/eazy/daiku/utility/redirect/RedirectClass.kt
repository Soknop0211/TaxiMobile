package com.eazy.daiku.utility.redirect

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.net.Uri
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import com.eazy.daiku.SampleActivity
import com.eazy.daiku.data.model.UserDefaultSingleTon
import com.eazy.daiku.ui.MainActivity
import com.eazy.daiku.ui.about_us.AboutUsActivity
import com.eazy.daiku.ui.change_password.ChangePasswordActivity
import com.eazy.daiku.ui.customer.map.CustomerMapPreViewActivity
import com.eazy.daiku.ui.customer.map.PickUpLocationActivity
import com.eazy.daiku.ui.customer.map.SearchMapActivity
import com.eazy.daiku.ui.customer.step_booking.ListCarTaxiActivity
import com.eazy.daiku.ui.customer.step_booking.LocationKioskBookingTaxiActivity
import com.eazy.daiku.ui.customer.step_booking.PaymentCompleteActivity
import com.eazy.daiku.ui.customer.step_booking.PreviewCheckoutActivity
import com.eazy.daiku.ui.customer.web_payment.WebPayActivity
import com.eazy.daiku.ui.forget_password.ForgetPasswordActivity
import com.eazy.daiku.ui.forget_password.VerifyOtpCodeActivity
import com.eazy.daiku.ui.history.HistoryTripActivity
import com.eazy.daiku.ui.identity_verification.IdentityVerificationActivity
import com.eazy.daiku.ui.login.LoginActivity
import com.eazy.daiku.ui.map.MapPreviewActivity
import com.eazy.daiku.ui.profile.EditProfileActivity
import com.eazy.daiku.ui.profile.MyProfileActivity
import com.eazy.daiku.ui.scan_qr_code.ScanQRCodeActivity
import com.eazy.daiku.ui.signup.SignUpActivity
import com.eazy.daiku.ui.upload_doc.UploadDocActivity
import com.eazy.daiku.ui.verification_pin_code.VerificationPinActivity
import com.eazy.daiku.ui.wallet.MainWalletActivity
import com.eazy.daiku.ui.web_view.WebViewEazyTaxiActivity
import com.eazy.daiku.ui.withdraw.EazyTaxiWithdrawWebviewActivity
import com.eazy.daiku.ui.withdraw.WithdrawMoneyActivity
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.enumerable.VerifyPinEnum

object RedirectClass : BaseRedirect() {

    fun gotoLoginActivity(activity: Activity) {
        if (activity is BaseActivity) {
            activity.disableDisplayOnMapLiveUser(/*newUpdate*/true)
            activity.unSubscribeLocationForeground()
            activity.unRegisterLocationLiveUser()
        }
        EazyTaxiHelper.clearSession(activity)
        val intent = Intent(activity, LoginActivity::class.java)
        gotoActivity(activity, intent)
        activity.finishAffinity()
    }

    fun gotoMainActivity(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        gotoActivity(activity, intent)
        UserDefaultSingleTon.newInstance?.docKycTemporary?.clear()
        activity.finishAffinity()
    }

    fun gotoProfileActivity(activity: Activity) {
        val intent = Intent(activity, MyProfileActivity::class.java)
        gotoActivity(activity, intent)
    }

    fun gotoEditProfileActivity(
        activity: Activity,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>,
    ) {
        val intent = Intent(activity, EditProfileActivity::class.java)
        gotoActivity(activity, intent, activityResult)
    }

    fun gotoSignUpActivity(activity: Activity) {
        val intent = Intent(activity, SignUpActivity::class.java)
        gotoActivity(activity, intent)
    }

    fun gotoUploadDocsActivity(
        activity: Activity,
        bodyHm: HashMap<String, Any>,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>,
    ) {
        val intent = Intent(activity, UploadDocActivity::class.java)
        intent.putExtra(UploadDocActivity.RequestBodyKey, bodyHm)
        gotoActivity(activity, intent, activityResult)
    }

    fun gotoUploadDocsActivity(
        activity: Activity,
        bodyHm: HashMap<String, Any>,
        kycDocTemporary: HashMap<String, Any>?,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>,
    ) {
        val intent = Intent(activity, UploadDocActivity::class.java)
        intent.putExtra(UploadDocActivity.RequestBodyKey, bodyHm)
        intent.putExtra(UploadDocActivity.hasKycDraftKey, kycDocTemporary)
        gotoActivity(activity, intent, activityResult)
    }

    fun gotoUploadDocsActivity(
        activity: Activity,
        reUploadKyc: Boolean,
    ) {
        val intent = Intent(activity, UploadDocActivity::class.java)
        intent.putExtra(UploadDocActivity.reUploadKycKey, reUploadKyc)
        gotoActivity(activity, intent)
    }


    fun gotoScanQRCodeActivity(activity: Activity) {
        val intent = Intent(activity, ScanQRCodeActivity::class.java)
        gotoActivity(activity, intent)
    }

    fun gotoMapPreviewActivity(
        activity: Activity,
        jsonData: String,
        isPreview: Boolean = false,
    ) {
        val intent = Intent(activity, MapPreviewActivity::class.java)
        intent.putExtra(MapPreviewActivity.QrCodeFieldKey, jsonData)
        intent.putExtra(MapPreviewActivity.PreviewMapOnly, isPreview)
        gotoActivity(activity, intent)
    }

    fun gotoCustomerMapPreView(
        activity: Activity,
        listKioskJsonData: String?,
    ) {
        val intent = Intent(activity, CustomerMapPreViewActivity::class.java)
        intent.putExtra(CustomerMapPreViewActivity.listKioskModelKey, listKioskJsonData)
        gotoActivity(activity, intent)
    }

    fun gotoLocationKioskBookingTaxi(activity: Activity, dataJson: String) {
        val intent = Intent(activity, LocationKioskBookingTaxiActivity::class.java)
        intent.putExtra(LocationKioskBookingTaxiActivity.dataListLocationKioskKey, dataJson)
        gotoActivity(activity, intent)
    }

    fun gotoListTaxiDriverActivity(
        activity: Activity,
        jsonData: String,
        lat: String,
        long: String,
        address: String,
        pathMapScreenShotPath: String,
        addressByIndex: Address,
        descriptionFromSearch: String,
        titleFromSearch: String,
    ) {
        val intent = Intent(activity, ListCarTaxiActivity::class.java)
        val adminArea =
            if (!TextUtils.isEmpty(titleFromSearch)) titleFromSearch else addressByIndex.adminArea
        val countryName =
            if (!TextUtils.isEmpty(titleFromSearch)) "" else addressByIndex.countryName
        val addressLine =
            if (!TextUtils.isEmpty(descriptionFromSearch)) descriptionFromSearch else addressByIndex.getAddressLine(
                0
            )
        intent.putExtra(ListCarTaxiActivity.dataListCarTaxiJsonObjectKey, jsonData)
        intent.putExtra(ListCarTaxiActivity.latKey, lat)
        intent.putExtra(ListCarTaxiActivity.longKey, long)
        intent.putExtra(ListCarTaxiActivity.addressKey, address)
        intent.putExtra(ListCarTaxiActivity.screenShotMapPathKey, pathMapScreenShotPath)
        intent.putExtra(ListCarTaxiActivity.addressByIndexKey, addressByIndex)
        intent.putExtra(ListCarTaxiActivity.adminAreaKey, adminArea)
        intent.putExtra(ListCarTaxiActivity.countryNameKey, countryName)
        intent.putExtra(ListCarTaxiActivity.getAddressLineKey, addressLine)
        gotoActivity(activity, intent)
    }

    fun gotoSearchMapActivity(
        activity: Activity,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>,
    ) {
        val intent = Intent(activity, SearchMapActivity::class.java)
        gotoActivity(activity, intent, activityResult)
    }

    fun gotoPickUpLocation(
        activity: Activity,
        jsonData: String?,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>
    ) {
        val intent = Intent(activity, PickUpLocationActivity::class.java)
        intent.putExtra(PickUpLocationActivity.searchLocationKioskNearByKey, jsonData)
        gotoActivity(activity, intent, activityResult)
    }

    fun gotoChangeNewPasswordActivity(
        activity: Activity,
        pinCode: String,
        verifyPinEnum: VerifyPinEnum,
    ) {
        val intent = Intent(activity, ChangePasswordActivity::class.java)
        intent.putExtra(ChangePasswordActivity.PinCodeKey, pinCode)
        intent.putExtra(ChangePasswordActivity.ScreenThatRequestKey, verifyPinEnum)
        gotoActivity(activity, intent)
    }

    fun gotoChangeNewPasswordActivity(
        activity: Activity,
        pinCode: String,
        otpToken: String,
        verifyPinEnum: VerifyPinEnum,
    ) {
        val intent = Intent(activity, ChangePasswordActivity::class.java)
        intent.putExtra(ChangePasswordActivity.PinCodeKey, pinCode)
        intent.putExtra(ChangePasswordActivity.Otp_token_key, otpToken)
        intent.putExtra(ChangePasswordActivity.ScreenThatRequestKey, verifyPinEnum)
        gotoActivity(activity, intent)
    }

    fun gotoChangeNewPasswordActivity(
        activity: Activity,
        verifyPinEnum: VerifyPinEnum,
    ) {
        val intent = Intent(activity, ChangePasswordActivity::class.java)
        intent.putExtra(ChangePasswordActivity.ScreenThatRequestKey, verifyPinEnum)
        gotoActivity(activity, intent)
    }


    fun gotoVerifyOtpCodeActivity(activity: Activity, otpToken: String) {
        val intent = Intent(activity, VerifyOtpCodeActivity::class.java)
        intent.putExtra(VerifyOtpCodeActivity.OTP_TOKEN_KEY, otpToken)
        gotoActivity(activity, intent)
    }

    fun gotoVerificationPinActivity(
        activity: Activity,
        title: String,
        verifyPinEnum: VerifyPinEnum,
    ) {
        val intent = Intent(activity, VerificationPinActivity::class.java)
        intent.putExtra(VerificationPinActivity.CHANGE_TITLE, title)
        intent.putExtra(VerificationPinActivity.whoCallActivity, verifyPinEnum)
        gotoActivity(activity, intent)
    }

    fun gotoVerificationPinActivity(
        activity: Activity,
        hasVerifyBiometric: Boolean = false,
        verifyPinEnum: VerifyPinEnum,
        bookingCode: String,
    ) {
        val intent = Intent(activity, VerificationPinActivity::class.java)
        intent.putExtra(VerificationPinActivity.whoCallActivity, verifyPinEnum)
        intent.putExtra(VerificationPinActivity.biometricKey, hasVerifyBiometric)
        intent.putExtra(VerificationPinActivity.bookingCode, bookingCode)
        gotoActivity(activity, intent)
        when (verifyPinEnum) {
            VerifyPinEnum.SplashScreen -> {
                activity.finish()
            }

            else -> {
            }
        }
    }

    fun gotoHistoryTripActivity(activity: Activity) {
        val intent = Intent(activity, HistoryTripActivity::class.java)
        gotoActivity(activity, intent)
    }

    fun gotoMainWalletActivity(activity: Activity) {
        val intent = Intent(activity, MainWalletActivity::class.java)
        gotoActivity(activity, intent)
    }


    fun gotoWebViewGoogleMapActivity(activity: Activity, googleMapData: String) {
        val intent = Intent(activity, WebViewEazyTaxiActivity::class.java)
        intent.putExtra(WebViewEazyTaxiActivity.DATA_GOOGLE, googleMapData)
        gotoActivity(activity, intent)
    }

    fun gotoIdentityVerificationActivity(activity: Activity, kycHm: HashMap<String, Any>) {
        val intent = Intent(activity, IdentityVerificationActivity::class.java)
        intent.putExtra(IdentityVerificationActivity.KycDataKey, kycHm)
        gotoActivity(activity, intent)
    }

    fun gotoWithdrawMoneyActivity(activity: Activity, jsonData: String, jsonDataListBank: String) {
        val intent = Intent(activity, WithdrawMoneyActivity::class.java)
        intent.putExtra(WithdrawMoneyActivity.TransactionFileRespondKey, jsonData)
        intent.putExtra(WithdrawMoneyActivity.listAllBankAccountModelKey, jsonDataListBank)
        gotoActivity(activity, intent)
    }

    fun gotoWithdrawMoneyWebViewActivity(
        activity: Activity,
        jsonData: String,
        verifyOtpUrl: String,
    ) {
        val intent = Intent(activity, EazyTaxiWithdrawWebviewActivity::class.java)
        intent.putExtra(EazyTaxiWithdrawWebviewActivity.WithdrawMoneyRespondModelKey, jsonData)
        intent.putExtra(EazyTaxiWithdrawWebviewActivity.WithdrawVerifyOtpUrlKey, verifyOtpUrl)
        gotoActivity(activity, intent)
    }

    fun gotoForgetPwdActivity(activity: Activity) {
        val intent = Intent(activity, ForgetPasswordActivity::class.java)
        gotoActivity(activity, intent)
    }

    fun gotoPreviewCheckoutActivity(
        activity: Activity,
        dataJsonPreview: String,
        carId: String,
        deviceId: String,
        pathMapScreenShot: String,
        adminArea: String,
        countryName: String,
        getAddressLine: String,
        lattitude: String,
        longtitude: String,
        vechicle: String,
    ) {
        val intent = Intent(activity, PreviewCheckoutActivity::class.java)
        intent.putExtra(PreviewCheckoutActivity.dataJsonPreviewCheckoutKey, dataJsonPreview)
        intent.putExtra(PreviewCheckoutActivity.carIdKey, carId)
        intent.putExtra(PreviewCheckoutActivity.deviceIdKey, deviceId)
        intent.putExtra(PreviewCheckoutActivity.pathMapScreenShotKey, pathMapScreenShot)
        intent.putExtra(PreviewCheckoutActivity.adminAreaKey, adminArea)
        intent.putExtra(PreviewCheckoutActivity.countryNameKey, countryName)
        intent.putExtra(PreviewCheckoutActivity.getAddressLineKey, getAddressLine)
        intent.putExtra(PreviewCheckoutActivity.lattitudeKey, lattitude)
        intent.putExtra(PreviewCheckoutActivity.longtitudeKey, longtitude)
        intent.putExtra(PreviewCheckoutActivity.vechicleKey, vechicle)
        gotoActivity(activity, intent)
    }

    fun gotoWebPayActivity(
        activity: Activity,
        dataJson: String,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>
    ) {
        val intent = Intent(activity, WebPayActivity::class.java)
        intent.putExtra(WebPayActivity.dataJsonWebViewKey, dataJson)
        gotoActivity(activity, intent, activityResult)

    }

    fun openDeepLink(activity: Activity, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        gotoActivity(activity, intent)
    }

    fun gotoPlayStore(activity: Activity, applicationId: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            intent.data = Uri.parse(String.format("%s%s", "market://details?id=", applicationId))
        } catch (e: Exception) {
            intent.data = Uri.parse(
                String.format(
                    "%s%s",
                    "https://play.google.com/store/apps/details?id=",
                    applicationId
                )
            )
        }
        gotoActivity(activity, intent)
    }

    fun gotoPaymentCompleteActivity(activity: Activity, qrCodeUrl: String) {
        val intent = Intent(activity, PaymentCompleteActivity::class.java)
        intent.putExtra(PaymentCompleteActivity.qrcodeUrlKey, qrCodeUrl)
        gotoActivity(activity, intent)
    }

    fun gotoAboutUsActivity(activity: Activity) {
        val intent = Intent(activity, AboutUsActivity::class.java)
        gotoActivity(activity, intent)
    }

    fun gotSampleActivity(activity: Activity) {
        val intent = Intent(activity, SampleActivity::class.java)
        gotoActivity(activity, intent)
    }
}