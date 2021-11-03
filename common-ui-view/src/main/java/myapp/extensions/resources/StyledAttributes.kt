package myapp.extensions.resources


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import splitties.mainthread.isMainThread

@AnyRes
fun Context.resolveThemeAttribute(
    @AttrRes attrRes: Int,
    resolveRefs: Boolean = true
): Int = if (isMainThread) {
    if (theme.resolveAttribute(attrRes, uiThreadConfinedCachedTypeValue, resolveRefs).not()) {
        throw Resources.NotFoundException(
            "Couldn't resolve attribute resource #0x" + Integer.toHexString(attrRes)
                + " from the theme of this Context."
        )
    }
    uiThreadConfinedCachedTypeValue.resourceId
} else synchronized(cachedTypeValue) {
    if (theme.resolveAttribute(attrRes, cachedTypeValue, resolveRefs).not()) {
        throw Resources.NotFoundException(
            "Couldn't resolve attribute resource #0x" + Integer.toHexString(attrRes)
                + " from the theme of this Context."
        )
    }
    cachedTypeValue.resourceId
}

private val uiThreadConfinedCachedTypeValue = TypedValue()
private val cachedTypeValue = TypedValue()


//TODO: Remove when withStyledAttributes is removed from API.
@PublishedApi
@SuppressLint("Recycle") // Recycled in function above.
internal fun Context.obtainStyledAttr(@AttrRes attrRes: Int): TypedArray = if (isMainThread) {
    uiThreadConfinedCachedAttrArray[0] = attrRes
    obtainStyledAttributes(uiThreadConfinedCachedAttrArray)
} else synchronized(cachedAttrArray) {
    cachedAttrArray[0] = attrRes
    obtainStyledAttributes(cachedAttrArray)
}

private val uiThreadConfinedCachedAttrArray = IntArray(1)
private val cachedAttrArray = IntArray(1)
