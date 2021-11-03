// copy from https://github.com/LouisCAD/Splitties/blob/main/modules/resources/src/androidMain/kotlin/splitties/resources
@file:Suppress("NOTHING_TO_INLINE")

package myapp.extensions.resources

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment


/**
 * @see [androidx.core.content.ContextCompat.getColor]
 */
@ColorInt
fun Context.resColor(@ColorRes colorRes: Int): Int = if (SDK_INT >= 23) getColor(colorRes) else {
    @Suppress("DEPRECATION")
    resources.getColor(colorRes)
}

inline fun Fragment.resColor(@ColorRes colorRes: Int) = context!!.resColor(colorRes)
inline fun View.resColor(@ColorRes colorRes: Int) = context.resColor(colorRes)


/**
 * @see [androidx.core.content.ContextCompat.getColorStateList]
 */
fun Context.resColorSL(@ColorRes colorRes: Int): ColorStateList {
    return (if (SDK_INT >= 23) getColorStateList(colorRes) else {
        @Suppress("DEPRECATION")
        resources.getColorStateList(colorRes)
    })
}

inline fun Fragment.resColorSL(@ColorRes colorRes: Int) = context!!.resColorSL(colorRes)
inline fun View.resColorSL(@ColorRes colorRes: Int) = context.resColorSL(colorRes)

// Styled resources below

private inline val defaultColor get() = Color.RED

@ColorInt
fun Context.styledColor(@AttrRes attr: Int): Int = resColor(resolveThemeAttribute(attr))

inline fun Fragment.styledColor(@AttrRes attr: Int) = context!!.styledColor(attr)
inline fun View.styledColor(@AttrRes attr: Int) = context.styledColor(attr)

fun Context.styledColorSL(@AttrRes attr: Int): ColorStateList = resColorSL(resolveThemeAttribute(attr))

inline fun Fragment.styledColorSL(@AttrRes attr: Int) = context!!.styledColorSL(attr)
inline fun View.styledColorSL(@AttrRes attr: Int) = context.styledColorSL(attr)
