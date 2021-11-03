@file:Suppress("NOTHING_TO_INLINE")

package myapp.extensions.resources


import android.content.Context
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import splitties.init.appCtx

inline fun Context.resTxt(@StringRes stringResId: Int): CharSequence = resources.getText(stringResId)
inline fun Fragment.resTxt(@StringRes stringResId: Int) = context!!.resTxt(stringResId)
inline fun View.resTxt(@StringRes stringResId: Int) = context.resTxt(stringResId)
inline fun appResTxt(@StringRes stringResId: Int) = appCtx.resTxt(stringResId)


inline fun Context.resStr(
    @StringRes stringResId: Int,
    vararg formatArgs: Any?
): String = resources.getString(stringResId, *formatArgs)

inline fun Fragment.resStr(
    @StringRes stringResId: Int,
    vararg formatArgs: Any?
) = context!!.resStr(stringResId, *formatArgs)

inline fun View.resStr(
    @StringRes stringResId: Int,
    vararg formatArgs: Any?
) = context.resStr(stringResId, *formatArgs)

inline fun appResStr(
    @StringRes stringResId: Int,
    vararg formatArgs: Any?
) = appCtx.resStr(stringResId, *formatArgs)


inline fun Context.resStr(@StringRes stringResId: Int): String = resources.getString(stringResId)
inline fun Fragment.resStr(@StringRes stringResId: Int) = context!!.resStr(stringResId)
inline fun View.resStr(@StringRes stringResId: Int) = context.resStr(stringResId)
inline fun appResStr(@StringRes stringResId: Int) = appCtx.resStr(stringResId)

inline fun Context.resQtyTxt(@PluralsRes stringResId: Int, quantity: Int): CharSequence {
    return resources.getQuantityText(stringResId, quantity)
}

inline fun Fragment.resQtyTxt(
    @PluralsRes stringResId: Int,
    quantity: Int
) = context!!.resQtyTxt(stringResId, quantity)

inline fun View.resQtyTxt(
    @PluralsRes stringResId: Int,
    quantity: Int
) = context.resQtyTxt(stringResId, quantity)

inline fun appResQtyTxt(
    @PluralsRes stringResId: Int,
    quantity: Int
) = appCtx.resQtyTxt(stringResId, quantity)


inline fun Context.resQtyStr(@PluralsRes stringResId: Int, quantity: Int): String {
    return resources.getQuantityString(stringResId, quantity)
}

inline fun Fragment.resQtyStr(
    @PluralsRes stringResId: Int,
    quantity: Int
) = context!!.resQtyStr(stringResId, quantity)

inline fun View.resQtyStr(
    @PluralsRes stringResId: Int,
    quantity: Int
) = context.resQtyStr(stringResId, quantity)


inline fun Context.resQtyStr(
    @PluralsRes stringResId: Int, quantity: Int,
    vararg formatArgs: Any?
): String = resources.getQuantityString(stringResId, quantity, *formatArgs)

inline fun Fragment.resQtyStr(
    @PluralsRes stringResId: Int,
    quantity: Int,
    vararg formatArgs: Any?
) = context!!.resQtyStr(stringResId, quantity, *formatArgs)

inline fun View.resQtyStr(
    @PluralsRes stringResId: Int,
    quantity: Int,
    vararg formatArgs: Any?
) = context.resQtyStr(stringResId, quantity, *formatArgs)

inline fun appResQtyStr(
    @PluralsRes stringResId: Int,
    quantity: Int
) = appCtx.resQtyStr(stringResId, quantity)


inline fun Context.resTxtArray(
    @ArrayRes stringResId: Int
): Array<out CharSequence> = resources.getTextArray(stringResId)

inline fun Fragment.resTxtArray(@ArrayRes stringResId: Int) = context!!.resTxtArray(stringResId)
inline fun View.resTxtArray(@ArrayRes stringResId: Int) = context.resTxtArray(stringResId)
inline fun appResTxtArray(@ArrayRes stringResId: Int) = appCtx.resTxtArray(stringResId)


inline fun Context.resStrArray(
    @ArrayRes stringResId: Int
): Array<String> = resources.getStringArray(stringResId)

inline fun Fragment.resStrArray(@ArrayRes stringResId: Int) = context!!.resStrArray(stringResId)
inline fun View.resStrArray(@ArrayRes stringResId: Int) = context.resStrArray(stringResId)
inline fun appResStrArray(@ArrayRes stringResId: Int) = appCtx.resStrArray(stringResId)



// Styled resources below

fun Context.styledTxt(@AttrRes attr: Int): CharSequence = resTxt(resolveThemeAttribute(attr))
inline fun Fragment.styledTxt(@AttrRes attr: Int) = context!!.styledTxt(attr)
inline fun View.styledTxt(@AttrRes attr: Int) = context.styledTxt(attr)


fun Context.styledStr(@AttrRes attr: Int): String = resStr(resolveThemeAttribute(attr))
inline fun Fragment.styledStr(@AttrRes attr: Int) = context!!.styledStr(attr)
inline fun View.styledStr(@AttrRes attr: Int) = context.styledStr(attr)
inline fun appStyledTxt(@AttrRes attr: Int) = appCtx.styledTxt(attr)


fun Context.styledStr(
    @AttrRes attr: Int,
    vararg formatArgs: Any?
): String = resStr(resolveThemeAttribute(attr), *formatArgs)

inline fun Fragment.styledStr(
    @AttrRes attr: Int,
    vararg formatArgs: Any?
) = context!!.styledStr(attr, *formatArgs)

inline fun View.styledStr(
    @AttrRes attr: Int,
    vararg formatArgs: Any?
) = context.styledStr(attr, *formatArgs)

inline fun appStyledStr(
    @AttrRes attr: Int,
    vararg formatArgs: Any?
) = appCtx.styledStr(attr, *formatArgs)

fun Context.styledTxtArray(
    @AttrRes attr: Int
): Array<out CharSequence> = resTxtArray(resolveThemeAttribute(attr))

inline fun Fragment.styledTxtArray(@AttrRes attr: Int) = context!!.styledTxtArray(attr)
inline fun View.styledTxtArray(@AttrRes attr: Int) = context.styledTxtArray(attr)

inline fun appStyledTxtArray(@AttrRes attr: Int) = appCtx.styledTxtArray(attr)
