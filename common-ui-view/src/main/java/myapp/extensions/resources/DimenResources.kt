@file:Suppress("NOTHING_TO_INLINE")
package myapp.extensions.resources


import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment

inline fun Context.resDimen(@DimenRes dimenResId: Int): Float = resources.getDimension(dimenResId)
inline fun Fragment.resDimen(@DimenRes dimenResId: Int) = context!!.resDimen(dimenResId)
inline fun View.resDimen(@DimenRes dimenResId: Int) = context.resDimen(dimenResId)


inline fun Context.resDimenPxSize(
    @DimenRes dimenResId: Int
): Int = resources.getDimensionPixelSize(dimenResId)

inline fun Fragment.resDimenPxSize(@DimenRes dimenResId: Int) = context!!.resDimenPxSize(dimenResId)
inline fun View.resDimenPxSize(@DimenRes dimenResId: Int) = context.resDimenPxSize(dimenResId)

inline fun Context.resDimenPxOffset(
    @DimenRes dimenResId: Int
): Int = resources.getDimensionPixelOffset(dimenResId)

inline fun Fragment.resDimenPxOffset(@DimenRes dimenResId: Int) = context!!.resDimenPxOffset(dimenResId)
inline fun View.resDimenPxOffset(@DimenRes dimenResId: Int) = context.resDimenPxOffset(dimenResId)


// Styled resources below

fun Context.styledDimen(@AttrRes attr: Int): Float = resDimen(resolveThemeAttribute(attr))
inline fun Fragment.styledDimen(@AttrRes attr: Int) = context!!.styledDimen(attr)
inline fun View.styledDimen(@AttrRes attr: Int) = context.styledDimen(attr)


fun Context.styledDimenPxSize(@AttrRes attr: Int): Int = resDimenPxSize(resolveThemeAttribute(attr))
inline fun Fragment.styledDimenPxSize(@AttrRes attr: Int) = context!!.styledDimenPxSize(attr)
inline fun View.styledDimenPxSize(@AttrRes attr: Int) = context.styledDimenPxSize(attr)


fun Context.styledDimenPxOffset(
    @AttrRes attr: Int
): Int = resDimenPxOffset(resolveThemeAttribute(attr))

inline fun Fragment.styledDimenPxOffset(@AttrRes attr: Int) = context!!.styledDimenPxOffset(attr)
inline fun View.styledDimenPxOffset(@AttrRes attr: Int) = context.styledDimenPxOffset(attr)

