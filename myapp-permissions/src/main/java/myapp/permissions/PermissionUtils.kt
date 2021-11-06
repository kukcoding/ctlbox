package myapp.permissions


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

@SuppressLint("ObsoleteSdkInt")
object PermissionUtils {
    private val LOG_TAG = PermissionUtils::class.java.simpleName

    fun isGrantedAll(context: Context, vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        for (perm in perms) {
            val granted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return false
            }
        }
        return true
    }

    fun isGrantedAny(context: Context, vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        for (perm in perms) {
            val granted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                return true
            }
        }
        return false
    }

    fun isDeniedAny(context: Context, vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        for (perm in perms) {
            val granted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return true
            }
        }
        return false
    }


    fun getDeniedPermissions(contexts: Context, perms: Collection<String>): List<String> {
        val deniedList = mutableListOf<String>()
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(contexts, perm) == PackageManager.PERMISSION_DENIED) {
                deniedList.add(perm)
            }
        }

        return deniedList
    }

    fun getGrantedPermissions(contexts: Context, perms: Collection<String>): List<String> {
        val deniedList = mutableListOf<String>()
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(contexts, perm) == PackageManager.PERMISSION_GRANTED) {
                deniedList.add(perm)
            }
        }

        return deniedList
    }

    private fun resolveContext(obj: Any): Context? = when (obj) {
        is Activity -> obj
        is Fragment -> obj.context
        is Context -> obj
        else -> null
    }

    fun openAppSettingActivity(fragmentOrActivity: Any, requestCode: Int = 9876): Boolean {
        val context: Context = resolveContext(fragmentOrActivity)!!

        val packageName = context.packageName

        val intent = Intent()
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:$packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            when (fragmentOrActivity) {
                is Activity -> fragmentOrActivity.startActivityForResult(intent, requestCode)
                is Fragment -> fragmentOrActivity.startActivityForResult(intent, requestCode)
                else -> {
                    return false
                }
            }
            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }
}
