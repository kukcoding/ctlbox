package myapp.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

object BaseUtils {

    fun appVersionName(context: Context): String {
        val pkgManager: PackageManager = context.packageManager
        val pkgInfo = pkgManager.getPackageInfo(context.packageName, 0)
        return pkgInfo.versionName
    }

    fun appVersionCode(context: Context): Long {
        val pkgManager: PackageManager = context.packageManager
        val pkgInfo = pkgManager.getPackageInfo(context.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(pkgInfo)
    }

}
