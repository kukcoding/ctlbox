package myapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.core.content.pm.PackageInfoCompat
import myapp.ui.common.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.math.ceil

object AndroidUtils {
    private var mStatusBarHeight = 0
    private var mDensity = 1f
    private var mDisplaySize = Point()

    fun getStatusBarHeight(): Int = mStatusBarHeight

    fun init(context: Context) {
        mDensity = context.resources.displayMetrics.density
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            mStatusBarHeight = context.resources.getDimensionPixelSize(resId)
        }

        checkDisplaySize(context)
    }


    private fun checkDisplaySize(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics.bounds
            mDisplaySize = Point(bounds.width(), bounds.height())
        } else {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            @Suppress("DEPRECATION")
            val display = wm.defaultDisplay
            @Suppress("DEPRECATION")
            display.getSize(mDisplaySize)
        }
    }

    fun actionBarSize(activity: Activity, defaultValueDp: Float = 48f): Int {
        val resources = activity.resources
        val density = resources.displayMetrics.density
        val attrs =
            activity.theme?.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
                ?: return (density * defaultValueDp).toInt()
        return attrs.getDimension(0, 0f).toInt().also { attrs.recycle() }
    }

    fun isTablet(context: Context) = isSmallTablet(context) || isLargeTablet(context)

    fun isSmallTablet(context: Context) = when (getScreenDensityString(context)) {
        "sw600dp", "sw600dp-land" -> true
        else -> false
    }

    fun isLargeTablet(context: Context) = when (getScreenDensityString(context)) {
        "sw720dp", "sw720dp-land", "sw820dp", "sw820dp-land" -> true
        else -> false
    }


    fun getScreenDensityString(context: Context): String {
        return context.resources.getString(R.string.screenDensity)
    }

    @SuppressLint("Deprecated")
    fun displayCompat(context: Context): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }
    }

    fun requiredDisplayCompat(context: Context) = displayCompat(context)!!

    @SuppressLint("ObsoleteSdkInt")
    fun screenSize(context: Context): Point {
        val display = requiredDisplayCompat(context)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics.bounds
            Point(bounds.width(), bounds.height())
        } else {
            val point = Point()
            @Suppress("DEPRECATION")
            display.getSize(point)
            point
        }
    }

    @Deprecated("Use screenSmallSide")
    fun screenMinSide(context: Context): Int {
        val p = screenSize(context)
        return minOf(p.x, p.y)
    }

    fun screenLargeSide(context: Context): Int {
        val p = screenSize(context)
        return maxOf(p.x, p.y)
    }

    fun screenSmallSide(context: Context): Int {
        val p = screenSize(context)
        return minOf(p.x, p.y)
    }

    fun screenWidth(context: Context) = screenSize(context).x
    fun screenHeight(context: Context) = screenSize(context).y


    fun hideKeyboard(vararg views: View) {
        if (views.isEmpty())
            return

        val imm = views[0].context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            ?: return
        for (v in views) {
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            v.clearFocus()
        }
    }

    fun hideKeyboard(activity: Activity?, vararg views: View) {
        if (activity != null) {
            activity.currentFocus?.windowToken?.let { windowToken ->
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    windowToken,
                    0
                )
            }
        }
        if (views.isNotEmpty())
            hideKeyboard(*views)
    }


    private var prevOrientation = -10

    fun isLandscape(context: Context): Boolean {
        return when (requiredDisplayCompat(context).rotation) {
            Surface.ROTATION_0 -> false
            Surface.ROTATION_90 -> true
            Surface.ROTATION_180 -> false
            else -> true
        }
    }

    fun isPhonePortrait(context: Context): Boolean = !isTablet(context) && isPortrait(context)
    fun isPhoneLandscape(context: Context): Boolean = !isTablet(context) && isLandscape(context)
    fun isSmallTabletLandscape(context: Context): Boolean = isSmallTablet(context) && isLandscape(context)
    fun isSmallTabletPortrait(context: Context): Boolean = isSmallTablet(context) && isPortrait(context)
    fun isLargeTabletLandscape(context: Context): Boolean = isLargeTablet(context) && isLandscape(context)
    fun isLargeTabletPortrait(context: Context): Boolean = isLargeTablet(context) && isPortrait(context)
    fun isTabletPortrait(context: Context): Boolean = isTablet(context) && isPortrait(context)
    fun isTabletLandscape(context: Context): Boolean = isTablet(context) && isLandscape(context)

    fun isPortrait(context: Context): Boolean = !isLandscape(context)

    @SuppressLint("SourceLockedOrientationActivity")
    fun lockOrientation(activity: Activity?) {
        if (activity == null || prevOrientation != -10) {
            return
        }
        try {
            prevOrientation = activity.requestedOrientation
            val display = displayCompat(activity)
            if (display != null) {
                val rotation = display.rotation
                val orientation = activity.resources.configuration.orientation
                if (rotation == Surface.ROTATION_270) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    }
                } else if (rotation == Surface.ROTATION_90) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    } else {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                } else if (rotation == Surface.ROTATION_0) {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                } else {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    } else {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AndroidUtils", e.message, e)
        }
    }

    @SuppressWarnings("ResourceType")
    fun unlockOrientation(activity: Activity?) {
        if (activity == null) {
            return
        }
        try {
            if (prevOrientation != -10) {
                activity.requestedOrientation = prevOrientation
                prevOrientation = -10
            }
        } catch (e: Exception) {
            Log.e("AndroidUtils", e.message, e)
        }

    }


    fun dp(value: Number): Int {
        return ceil((mDensity * value.toFloat()).toDouble()).toInt()
    }

    fun dpf(value: Number): Float {
        return mDensity * value.toFloat()
    }


    fun getDisplay(context: Context): Display? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display ?: return null
        } else {
            val wm = context.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay
        }
    }


    fun isKorean(): Boolean {
        return Locale.getDefault().language == Locale.KOREAN.language
    }


    private var cachedPackageSignature: String? = null
    fun getPackageSignature(context: Context): String? {
        if (cachedPackageSignature == null) {
//            cachedPackageSignature = loadPackageSignature(context)
            cachedPackageSignature = try {
                getSig(context, "SHA1")
            } catch (e: Throwable) {
                loadPackageSignature(context)
            }
        }
        return cachedPackageSignature
    }

    @Throws(PackageManager.NameNotFoundException::class, NoSuchAlgorithmException::class)
    fun loadPackageSignature(context: Context): String? {

        //Log.d("XXX", "packageName:" + context.getPackageName());
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
        } ?: return null

        val md = MessageDigest.getInstance("SHA1")
        val signatures: Array<Signature>
        signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo.signingCertificateHistory
        } else {
            packageInfo.signatures
        }
        for (sig in signatures) {
            md.update(sig.toByteArray())
        }

        val digest = md.digest()
        return if (digest.isEmpty()) {
            null
        } else Base64.encodeToString(digest, Base64.NO_WRAP)

//Log.d("AndroidUtils", "keyHash:" + str);
    }

    private var cachedUuid: String? = null
    fun generateUUIDOnce(): String {
        if (cachedUuid == null) {
            cachedUuid = generateUUID()
        }
        return cachedUuid!!
    }

    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * 디바이스 일련 번호
     */
//    private var cachedDeviceSerialNumber: String? = null
//
//    @Deprecated("use ")
//    fun getDeviceSerialNumber(context: Context): String {
//        if (cachedDeviceSerialNumber == null) {
//            cachedDeviceSerialNumber = loadDeviceSerialNumber(context)
//        }
//        return cachedDeviceSerialNumber!!
//    }
//
//    @SuppressLint("HardwareIds")
//    private fun loadDeviceSerialNumber(context: Context): String {
//        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)!!
//    }

    fun isDarkModeOn(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }


    fun getSig(context: Context, key: String): String {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance(key)
                md.update(signature.toByteArray())
                val digest = md.digest()
                val toRet = StringBuilder()
                for (i in digest.indices) {
                    if (i != 0) toRet.append(":")
                    val b = digest[i].toInt() and 0xff
                    val hex = Integer.toHexString(b)
                    if (hex.length == 1) toRet.append("0")
                    toRet.append(hex)
                }
                return toRet.toString()
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            Log.e("name not found", e1.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("no such an algorithm", e.toString())
        } catch (e: Exception) {
            Log.e("exception", e.toString())
        }
        return ""
    }

    fun appVersionSummary(context: Context): String {
        val pkgManager: PackageManager = context.packageManager
        val pkgInfo = pkgManager.getPackageInfo(context.packageName, 0)
        return context.getString(
            R.string.settings_app_version_summary,
            pkgInfo.versionName,
            PackageInfoCompat.getLongVersionCode(pkgInfo)
        )
    }

    fun clipboard(context: Context, label: String, text: String) {
        val mgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        mgr.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun resourceToUrl(context: Context, @DrawableRes resId: Int): String {
        return "android.resource://${context.packageName}/${resId}"
    }

    fun resourceToUri(context: Context, @DrawableRes resId: Int): Uri {
        return Uri.parse(resourceToUrl(context = context, resId = resId))
    }

    fun assetToUrl(assetPath: String): String {
        return "file:///android_asset/${assetPath}"
    }
}
