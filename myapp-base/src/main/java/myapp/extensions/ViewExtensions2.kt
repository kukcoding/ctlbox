package myapp.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.TextView


inline fun View.updateMarginLayoutParams(
    block: ViewGroup.MarginLayoutParams.() -> Unit
) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    block(params)
    layoutParams = params
}

fun TextView.trimOrEmpty(): String {
    return this.text?.trim()?.toString() ?: return ""
}


fun TextView.trimOrNull(): String? {
    val txt = this.text?.trim() ?: return null
    if (txt.isEmpty()) return null
    return txt.toString()
}

