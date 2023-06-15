package com.eazy.daiku.utility.custom

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.airbnb.lottie.utils.Utils
import com.bumptech.glide.util.Util
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.CustomerHistoryModel
import com.eazy.daiku.data.model.server_model.QrCodeRespond
import com.eazy.daiku.utility.QRCodeHelper
import com.eazy.daiku.utility.base.EazySweetAlert
import com.eazy.daiku.utility.call_back.MessageEditTextListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.internal.operators.flowable.FlowableSwitchIfEmpty

object MessageUtils {

    fun showError(context: Context, title: String?, text: String?) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(title ?: context.getString(R.string.oops))
                    .setContentText(String.format("%s", text))
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showError(
        context: Context,
        title: String?,
        text: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(title ?: "")
                    .setContentText(text)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showErrorDismiss(
        context: Context,
        title: String?,
        text: String?,
        onDismissListener: DialogInterface.OnDismissListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(title ?: "")
                    .setContentText(text)
            alertDialog.setOnDismissListener(onDismissListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showWarning(context: Context?, title: String?, text: String?, confirmText: String?) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setConfirmText(confirmText)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showWarning(
        context: Context?,
        title: String?,
        text: String?,
        confirmText: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setConfirmText(confirmText)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showWarning(
        context: Context?,
        title: String?,
        text: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showConfirm(
        context: Context?,
        text: String?,
        confirmStr: String?,
        showCancelButton: Boolean,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("")
                    .setContentText(text)
                    .setCancelText("")
                    .setConfirmText(confirmStr)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(showCancelButton)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
            alertDialog.findViewById<MaterialButton>(R.id.cancel_button).visibility = View.GONE
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showWarning(
        context: Context?,
        title: String?,
        text: String?,
        onCancelClickListener: OnSweetClickListener?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelClickListener(onCancelClickListener)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showSuccess(context: Context?, title: String?, text: String?) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showSuccess(
        context: Context?,
        title: String?,
        text: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun showNormal(context: Context?, title: String?, text: String?) {
        var text = text
        text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
        val alertDialog: SweetAlertDialog =
            EazySweetAlert(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(title)
                .setContentText(text)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    fun showSuccess(
        context: Context?,
        title: String?,
        text: String?,
        onSweetClickListener: OnSweetClickListener?,
        onDismissListener: DialogInterface.OnDismissListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setOnDismissListener(onDismissListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showSuccess(
        context: Context?,
        title: String?,
        text: String?,
        confirm: String?,
        cancel: String?,
        onSweetClickListener: OnSweetClickListener?,
        onCancelListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setConfirmText(confirm)
                    .setCancelText(cancel)
                    .setCancelClickListener(onCancelListener)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showSuccessDismiss(
        context: Context?,
        title: String?,
        text: String?,
        onDismissListener: DialogInterface.OnDismissListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
            alertDialog.setOnDismissListener(onDismissListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showConfirm(
        context: Context,
        title: String?,
        gender: Int,
        text: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(context.getString(R.string.no))
                    .setConfirmText(context.getString(if (gender == 1) R.string.yes_f else R.string.yes_m))
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }


    fun showConfirm(
        context: Context,
        title: String?,
        text: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(context.getString(R.string.no))
                    .setConfirmText(
                        context.getString(R.string.yes_m)
                    )
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showConfirm(
        context: Context?,
        title: String?,
        text: String?,
        cancelStr: String?,
        confirmStr: String?,
        onSweetClickListener: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(confirmStr)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onSweetClickListener)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showConfirm(
        context: Context?,
        title: String?,
        text: String?,
        cancelStr: String?,
        confirmStr: String?,
        onCancel: OnSweetClickListener?,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(confirmStr)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setCancelClickListener(onCancel)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }


    fun showConfirm(
        context: Context?,
        title: String?,
        text: String?,
        onCancel: OnSweetClickListener?,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText("No")
                    .setConfirmText("Yes")
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setCancelClickListener(onCancel)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }


    /**
     * show new icon alert
     */
    fun showConfirm(
        context: Context?,
        newLogo: Int,
        title: String?,
        text: String?,
        cancelStr: String?,
        confirmStr: String?,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(newLogo)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(confirmStr)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showConfirm(
        context: Context?,
        newLogo: Int,
        title: String?,
        text: String?,
        cancelStr: String?,
        confirmStr: String?,
        onCancelListener: OnSweetClickListener?,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(newLogo)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(confirmStr)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setCancelClickListener(onCancelListener)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showWarnigIcon(
        context: Context?,
        newLogo: Int,
        title: String?,
        text: String?,
        onCancelListener: OnSweetClickListener?,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(newLogo)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(null)
                    .setConfirmText(null)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setCancelClickListener(onCancelListener)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun showWarnigIcon(
        context: Context?,
        newLogo: Int,
        title: String?,
        text: String?,
        showCancelButton: Boolean,
        canceledOnTouchOutside: Boolean,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(newLogo)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(null)
                    .setConfirmText(null)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(showCancelButton)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    /**
     * show new icon alert with new color
     */
    fun showConfirm(
        context: Context?,
        newLogo: Int,
        color: Int,
        title: String?,
        text: String?,
        cancelStr: String?,
        confirmStr: String?,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val unwrappedDrawable = AppCompatResources.getDrawable(
                context!!, newLogo
            )
            var wrappeImagedDrawable: Drawable? = null
            if (unwrappedDrawable != null) {
                wrappeImagedDrawable = DrawableCompat.wrap(unwrappedDrawable)
                DrawableCompat.setTint(
                    wrappeImagedDrawable, ContextCompat.getColor(
                        context, color
                    )
                )
            }
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(wrappeImagedDrawable)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(confirmStr)
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    /**
     * show new icon alert with new color with change confirm yes boy or girl
     */
    fun showConfirm(
        context: Context,
        newLogo: Int,
        color: Int,
        title: String?,
        text: String?,
        cancelStr: String?,
        gender: Int,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val unwrappedDrawable = AppCompatResources.getDrawable(context, newLogo)
            var wrappeImagedDrawable: Drawable? = null
            if (unwrappedDrawable != null) {
                wrappeImagedDrawable = DrawableCompat.wrap(unwrappedDrawable)
                DrawableCompat.setTint(wrappeImagedDrawable, ContextCompat.getColor(context, color))
            }
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(wrappeImagedDrawable)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(context.getString(if (gender == 1) R.string.yes_f else R.string.yes_m))
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    /**
     * show new icon alert without color with change confirm yes boy or girl
     */
    fun showConfirm(
        context: Context,
        newLogo: Int,
        title: String?,
        text: String?,
        cancelStr: String?,
        gender: Int,
        onConfirm: OnSweetClickListener?,
    ) {
        var text = text
        try {
            text = text?.replace("\n\n".toRegex(), "\n")?.replace("\n".toRegex(), "<br />") ?: ""
            val alertDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(newLogo)
                    .setTitleText(title)
                    .setContentText(text)
                    .setCancelText(cancelStr)
                    .setConfirmText(context.getString(if (gender == 1) R.string.yes_f else R.string.yes_m))
                    .setCancelButtonTextColor(R.color.black)
                    .showCancelButton(true)
                    .setConfirmClickListener(onConfirm)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        } catch (ex: java.lang.RuntimeException) {
            ex.printStackTrace()
        }
    }

    fun sweetAlertDialog(context: Context, text: String?, isShow: Boolean): SweetAlertDialog? {
        var text = text
        return try {
            text = text?.replace("\r\n".toRegex(), "<br />") ?: context.getString(R.string.loading)
            val pDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = text
            pDialog.setCancelable(false)
            pDialog.setCanceledOnTouchOutside(false)
            if (isShow) pDialog.show()
            pDialog
        } catch (ex: java.lang.RuntimeException) {
            null
        }
    }

    fun loadingAlertDialog(
        context: Context, text: String?,
        isShow: Boolean,
    ): SweetAlertDialog? {
        return try {
            val pDialog: SweetAlertDialog =
                EazySweetAlert(context, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = text ?: context.getString(R.string.loading)
            pDialog.setCanceledOnTouchOutside(false)
            pDialog.setCancelable(true)
            if (isShow) pDialog.show()
            pDialog
        } catch (ex: java.lang.RuntimeException) {
            null
        }
    }

    fun showQrCodeDestination(
        context: Context, qrCodeUrl: String, title: String
    ) {
        val layout: View =
            LayoutInflater.from(context).inflate(R.layout.view_show_qr_code_layout, null)
        val actionClose: TextView = layout.findViewById(R.id.action_close_qr)
        val tvCarType: TextView = layout.findViewById(R.id.tv_type_car)
        val imQr: ImageView = layout.findViewById(R.id.im_qr)
        val bitmap = QRCodeHelper.generateQrImage(qrCodeUrl)
        imQr.setImageBitmap(bitmap)
        tvCarType.text = String.format(
            "%s: %s",
            context.getString(R.string.vehicle),
            title ?: ""
        )

        //implement code in material
        val materialAlertDialogBuilder =
            MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
        materialAlertDialogBuilder.setView(layout)
        val alertDialog = materialAlertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        actionClose.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}