package com.eazy.daiku.utility.base

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.Constants
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.custom.MessageUtils
import dagger.android.support.DaggerFragment
import javax.inject.Inject

open class BaseFragment : DaggerFragment() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    protected fun <T : BaseFragment?> self(): T {
        return this as T
    }


    @SuppressLint("RestrictedApi")
    protected fun hideShowActionBar(fContext: FragmentActivity, showActionBar: Boolean = true) {
        val baseActivity: BaseCoreActivity = (fContext as BaseCoreActivity)
        if (showActionBar) {
            baseActivity.supportActionBar?.show()
        } else {
            baseActivity.supportActionBar?.hide()
        }
        baseActivity.supportActionBar?.setShowHideAnimationEnabled(false)
    }

    open fun getUserUserToSharePreference(): User {
        val jsonStr =
            context?.let {
                EazyTaxiHelper.getSharePreference(it.applicationContext,
                    Constants.userDetailKey)
            }
        return GsonConverterHelper.getJsonObjectToGenericClass(jsonStr)
    }

}