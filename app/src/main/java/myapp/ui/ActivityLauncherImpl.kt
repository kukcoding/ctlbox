package myapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.ctlbox.DisconnectedMessageActivity
import com.google.ctlbox.LaunchActivity
import myapp.ActivityLauncher
import javax.inject.Inject

class ActivityLauncherImpl @Inject constructor() : ActivityLauncher {
    override fun startMain(activityOrFragment: Any) {
        val ctx = context(activityOrFragment)!!
        start(activityOrFragment, intent = LaunchActivity.createIntent(ctx).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }, 0)
    }

    override fun startDisconnectedMessage(activityOrFragment: Any) {
        val ctx = context(activityOrFragment)!!
        start(activityOrFragment, intent = DisconnectedMessageActivity.createIntent(ctx), 0)
    }

    private fun start(activityOrFragment: Any, intent: Intent, requestCode: Int) {
        when (activityOrFragment) {
            is Activity -> activityOrFragment.startActivityForResult(intent, requestCode)
            is Fragment -> activityOrFragment.startActivityForResult(intent, requestCode)
            is Context -> activityOrFragment.startActivity(intent)
            else -> {
                throw IllegalArgumentException("first argument is not activity or fragment")
            }
        }
    }

    private fun context(obj: Any): Context? = when (obj) {
        is Activity -> obj
        is Fragment -> obj.context
        is Context -> obj
        else -> null
    }
}
