package kr.ohlab.android.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TRPermissions {

    private const val TAG = "TRPermissions"
    private const val PERMISSION_REQUEST_ID = 9999

    fun isGranted(context: Context, vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        for (perm in perms) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return false
            }
        }
        return true
    }


    fun existsDenied(context: Context, vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        for (perm in perms) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return true
            }
        }
        return false
    }

    fun existsGranted(context: Context, vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        for (perm in perms) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                return true
            }
        }
        return false
    }

    fun filterDenied(contexts: Context, perms: Collection<String>): List<String> {
        val deniedList = mutableListOf<String>()
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(
                    contexts,
                    perm
                ) == PackageManager.PERMISSION_DENIED
            ) {
                deniedList.add(perm)
            }
        }

        return deniedList
    }

    fun filterGranted(contexts: Context, perms: Collection<String>): List<String> {
        val deniedList = mutableListOf<String>()
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(
                    contexts,
                    perm
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                deniedList.add(perm)
            }
        }

        return deniedList
    }

    suspend fun suspendRequestPermission(
        activity: AppCompatActivity,
        vararg permissions: String
    ): TRPermissionResult {
        return withContext(Dispatchers.Main) {
            val result = TRPermissionManager.requestPermissions(
                activity,
                PERMISSION_REQUEST_ID,
                *permissions
            )
            Log.d(TAG, "suspendRequestPermission $result")
            result
        }
    }

    suspend fun suspendRequestPermission(
        activity: AppCompatActivity,
        permissions: Collection<String>
    ): TRPermissionResult {
        return suspendRequestPermission(activity, *permissions.toTypedArray())
    }

    suspend fun suspendRequestPermission(
        fragment: Fragment,
        vararg permissions: String
    ): TRPermissionResult {
        return withContext(Dispatchers.Main) {
            val result = TRPermissionManager.requestPermissions(
                fragment,
                PERMISSION_REQUEST_ID,
                *permissions
            )
            Log.d(TAG, "suspendRequestPermission $result")
            result
        }
    }


    suspend fun suspendRequestPermission(
        fragment: Fragment,
        permissions: Collection<String>
    ): TRPermissionResult {
        return suspendRequestPermission(fragment, *permissions.toTypedArray())
    }


    object UsageStats {
        @SuppressLint("ObsoleteSdkInt")
        fun isGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return true
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }

            return if (mode == AppOpsManager.MODE_DEFAULT) {
                context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        }

        fun openSettings(activityOrFragment: Any, requestCode: Int = 9999) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            when (activityOrFragment) {
                is Activity -> activityOrFragment.startActivityForResult(intent, requestCode)
                is Fragment -> activityOrFragment.startActivityForResult(intent, requestCode)
            }
        }
    }

    object DrawOverlays {
        fun isGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context)
            }
            return true
        }

        fun openSettings(activityOrFragment: Any, requestCode: Int = 9999) {
            val context = findContext(activityOrFragment)!!
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            when (activityOrFragment) {
                is Activity -> activityOrFragment.startActivityForResult(intent, requestCode)
                is Fragment -> activityOrFragment.startActivityForResult(intent, requestCode)
                else -> {
                    throw IllegalArgumentException("parameter is not activity or fragment")
                }
            }
        }

    }


    private fun findContext(obj: Any): Context? = when (obj) {
        is Activity -> obj
        is Fragment -> obj.context
        is Context -> obj
        else -> null
    }
}

