package com.eazy.daiku.utility.base

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.custom.MessageUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    var mContextBase: Context? = null
    var fContextBase: FragmentActivity? = null

    val activityLauncher: BetterActivityResult<Intent, ActivityResult> =
        BetterActivityResult.registerActivityForResult(this)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContextBase = context
        fContextBase = context as FragmentActivity
    }

    protected fun <T : BaseBottomSheetDialogFragment?> self(): T {
        return this as T
    }

    fun globalShowError(text: String) {
        MessageUtils.showError(
            self(),
            "",
            "$text"
        )
    }

}