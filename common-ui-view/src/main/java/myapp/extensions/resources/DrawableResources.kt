@file:Suppress("NOTHING_TO_INLINE")

package myapp.extensions.resources


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

private val tmpValue by lazy { TypedValue() }

/**
 * @see [androidx.core.content.ContextCompat.getDrawable]
 */
@SuppressLint("UseCompatLoadingForDrawables", "ObsoleteSdkInt")
fun Context.resDrawable(@DrawableRes drawableResId: Int): Drawable? {
    @Suppress("CascadeIf")
    return if (SDK_INT >= 21) getDrawable(drawableResId)
    else if (SDK_INT >= 16) {
        @Suppress("DEPRECATION")
        resources.getDrawable(drawableResId)
    } else {
        // Prior to API 16, Resources.getDrawable() would not correctly
        // retrieve the final configuration density when the resource ID
        // is a reference another Drawable resource. As a workaround, try
        // to resolve the drawable reference manually.
        val resolvedId = synchronized(tmpValue) {
            resources.getValue(drawableResId, tmpValue, true)
            tmpValue.resourceId
        }
        @Suppress("DEPRECATION")
        resources.getDrawable(resolvedId)
    }
}

inline fun Fragment.resDrawable(@DrawableRes drawableResId: Int) = context!!.resDrawable(drawableResId)
inline fun View.resDrawable(@DrawableRes drawableResId: Int) = context.resDrawable(drawableResId)

// Styled resources below

fun Context.styledDrawable(@AttrRes attr: Int): Drawable? = resDrawable(resolveThemeAttribute(attr))

inline fun Fragment.styledDrawable(@AttrRes attr: Int) = context!!.styledDrawable(attr)
inline fun View.styledDrawable(@AttrRes attr: Int) = context.styledDrawable(attr)

