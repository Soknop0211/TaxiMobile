package com.eazy.daiku.utility.redirect

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.BaseCoreActivity

open class BaseRedirect {

    protected open fun gotoActivity(activity: Activity, intent: Intent) {
        if (activity is BaseCoreActivity) {
            val baseActivity: BaseCoreActivity = activity
            baseActivity.activityLauncher.launch(intent)
            return
        }
        activity.startActivity(intent)
    }

    protected open fun gotoActivity(
        activity: Activity,
        intent: Intent,
        activityResult: BetterActivityResult.OnActivityResult<ActivityResult>
    ) {
        if (activity is BaseCoreActivity) {
            activityResult.let {
                activity.activityLauncher.launch(intent, activityResult)
            }
        }
    }

    protected open fun gotoActivity(context: Context, intent: Intent) {
        if (context is BaseCoreActivity) {
            val baseActivity: BaseCoreActivity = context
            baseActivity.activityLauncher.launch(intent)
            return
        }
        context.startActivity(intent)
    }
}