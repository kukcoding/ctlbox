@file:Suppress("NOTHING_TO_INLINE")

package myapp.extensions.resources


import android.content.Context
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.fragment.app.Fragment
import splitties.init.appCtx

inline fun Context.resBool(@BoolRes boolResId: Int): Boolean = resources.getBoolean(boolResId)
inline fun Fragment.resBool(@BoolRes boolResId: Int) = context!!.resBool(boolResId)
inline fun View.resBool(@BoolRes boolResId: Int) = context.resBool(boolResId)
inline fun appResBool(@BoolRes boolResId: Int) = appCtx.resBool(boolResId)


inline fun Context.resInt(@IntegerRes intResId: Int): Int = resources.getInteger(intResId)
inline fun Fragment.resInt(@IntegerRes intResId: Int) = context!!.resInt(intResId)
inline fun View.resInt(@IntegerRes intResId: Int) = context.resInt(intResId)
inline fun appResInt(@IntegerRes intResId: Int) = appCtx.resInt(intResId)

inline fun Context.resIntArray(
    @ArrayRes intArrayResId: Int
): IntArray = resources.getIntArray(intArrayResId)

inline fun Fragment.resIntArray(@ArrayRes intArrayResId: Int) = context!!.resIntArray(intArrayResId)
inline fun View.resIntArray(@ArrayRes intArrayResId: Int) = context.resIntArray(intArrayResId)
inline fun appResIntArray(@ArrayRes intArrayResId: Int) = appCtx.resIntArray(intArrayResId)


// Styled resources below

fun Context.styledBool(@AttrRes attr: Int): Boolean = resBool(resolveThemeAttribute(attr))

inline fun Fragment.styledBool(@AttrRes attr: Int) = context!!.styledBool(attr)
inline fun View.styledBool(@AttrRes attr: Int) = context.styledBool(attr)
inline fun appStyledBool(@AttrRes attr: Int) = appCtx.styledBool(attr)


fun Context.styledInt(@AttrRes attr: Int): Int = resInt(resolveThemeAttribute(attr))
inline fun Fragment.styledInt(@AttrRes attr: Int) = context!!.styledInt(attr)
inline fun View.styledInt(@AttrRes attr: Int) = context.styledInt(attr)
inline fun appStyledInt(@AttrRes attr: Int) = appCtx.styledInt(attr)

