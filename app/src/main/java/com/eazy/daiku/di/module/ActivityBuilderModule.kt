package com.eazy.daiku.di.module

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
import com.eazy.daiku.ui.splash_screen.SplashScreenActivity
import com.eazy.daiku.ui.upload_doc.UploadDocActivity
import com.eazy.daiku.ui.verification_pin_code.VerificationPinActivity
import com.eazy.daiku.ui.wallet.MainWalletActivity
import com.eazy.daiku.ui.web_view.WebViewEazyTaxiActivity
import com.eazy.daiku.ui.withdraw.EazyTaxiWithdrawWebviewActivity
import com.eazy.daiku.ui.withdraw.WithdrawMoneyActivity
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.BaseCoreActivity
import com.eazy.daiku.utility.base.SimpleBaseActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector
    abstract fun splashScreenActivity(): SplashScreenActivity

    @ContributesAndroidInjector
    abstract fun baseActivity(): BaseActivity

    @ContributesAndroidInjector
    abstract fun baseCoreActivity(): BaseCoreActivity

    @ContributesAndroidInjector
    abstract fun simpleBaseActivity(): SimpleBaseActivity

    @ContributesAndroidInjector
    abstract fun signUpActivity(): SignUpActivity

    @ContributesAndroidInjector
    abstract fun loginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun verificationPinActivity(): VerificationPinActivity

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun mProfileActivity(): MyProfileActivity

    @ContributesAndroidInjector
    abstract fun editProfileActivity(): EditProfileActivity

    @ContributesAndroidInjector
    abstract fun scanQRCodeActivity(): ScanQRCodeActivity

    @ContributesAndroidInjector
    abstract fun uploadDocActivity(): UploadDocActivity

    @ContributesAndroidInjector
    abstract fun mapPreviewActivity(): MapPreviewActivity

    @ContributesAndroidInjector
    abstract fun forgotPasswordActivity(): VerifyOtpCodeActivity

    @ContributesAndroidInjector
    abstract fun changePasswordActivity(): ChangePasswordActivity

    @ContributesAndroidInjector
    abstract fun historyTripActivity(): HistoryTripActivity

    @ContributesAndroidInjector
    abstract fun mainWalletActivity(): MainWalletActivity

    @ContributesAndroidInjector
    abstract fun webViewEazyTaxiActivity(): WebViewEazyTaxiActivity

    @ContributesAndroidInjector
    abstract fun identityVerificationActivity(): IdentityVerificationActivity

    @ContributesAndroidInjector
    abstract fun withdrawMoneyActivity(): WithdrawMoneyActivity

    @ContributesAndroidInjector
    abstract fun eazyTaxiWithdrawWebViewActivity(): EazyTaxiWithdrawWebviewActivity

    @ContributesAndroidInjector
    abstract fun forgetPasswordActivity(): ForgetPasswordActivity

    @ContributesAndroidInjector
    abstract fun customerMapPreViewActivity(): CustomerMapPreViewActivity

    @ContributesAndroidInjector
    abstract fun prickUpLocationActivity(): PickUpLocationActivity

    @ContributesAndroidInjector
    abstract fun searchMapActivity(): SearchMapActivity

    @ContributesAndroidInjector
    abstract fun webPayActivity(): WebPayActivity

    @ContributesAndroidInjector
    abstract fun listCarTaxiActivity(): ListCarTaxiActivity

    @ContributesAndroidInjector
    abstract fun locationKioskBookingTaxiActivity(): LocationKioskBookingTaxiActivity

    @ContributesAndroidInjector
    abstract fun previewCheckoutActivity(): PreviewCheckoutActivity

    @ContributesAndroidInjector
    abstract fun paymentCompleteActivity(): PaymentCompleteActivity

    @ContributesAndroidInjector
    abstract fun aboutUsActivity(): AboutUsActivity
}