package myapp.ui.common.databinding


import android.content.res.Resources
import android.graphics.Outline
import android.graphics.Typeface
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.textfield.TextInputLayout
import myapp.extensions.resolveThemeReferenceResId
import kotlin.math.roundToInt

@BindingAdapter("visibleIfNotNull")
fun visibleIfNotNull(view: View, target: Any?) {
    view.isVisible = target != null
}

@BindingAdapter("visibleIfNotEmpty")
fun visibleIfNotEmpty(view: View, target: String?) {
    view.isVisible = target.isNullOrBlank() == false
}

@BindingAdapter("visibleIfNull")
fun visibleIfNull(view: View, target: Any?) {
    view.isVisible = target == null
}

@BindingAdapter("visible")
fun visible(view: View, value: Boolean) {
    view.isVisible = value
}


@BindingAdapter("invisible")
fun invisible(view: View, value: Boolean) {
    view.isInvisible = value
}


@BindingAdapter("goneIfEmpty")
fun goneIfEmpty(view: View, s: CharSequence?) {
    view.isGone = s.isNullOrEmpty()
}

@BindingAdapter("invisibleIfEmpty")
fun invisibleIfEmpty(view: View, s: CharSequence?) {
    view.isInvisible = s.isNullOrEmpty()
}

@BindingAdapter("invisibleIfNotEmpty")
fun invisibleIfNotEmpty(view: View, s: CharSequence?) {
    view.isInvisible = !s.isNullOrEmpty()
}


@BindingAdapter("textOrGoneIfEmpty")
fun textOrGoneIfEmpty(view: TextView, s: CharSequence?) {
    view.text = s
    view.isGone = s.isNullOrEmpty()
}

@BindingAdapter("goneIfNull")
fun goneIfNull(view: View, value: Any?) {
    view.isGone = value == null
}

@BindingAdapter("srcRes")
fun imageViewSrcRes(view: ImageView, drawableRes: Int) {
    if (drawableRes != 0) {
        view.setImageResource(drawableRes)
    } else {
        view.setImageDrawable(null)
    }
}

@BindingAdapter("startIconRes")
fun startIconRes(view: TextInputLayout, drawableRes: Int) {
    if (drawableRes != 0) {
        view.setStartIconDrawable(drawableRes)
    } else {
        view.startIconDrawable = null
    }
}


@BindingAdapter("endIconRes")
fun endIconRes(view: TextInputLayout, drawableRes: Int) {
    if (drawableRes != 0) {
        view.setEndIconDrawable(drawableRes)
    } else {
        view.endIconDrawable = null
    }
}


@BindingAdapter("topCornerOutlineProvider")
fun topCornerOutlineProvider(view: View, oldRadius: Float, radius: Float) {
    view.clipToOutline = true
    if (oldRadius != radius) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height + radius.roundToInt(), radius)
            }
        }
    }
}

@BindingAdapter("roundedCornerOutlineProvider")
fun roundedCornerOutlineProvider(view: View, oldRadius: Float, radius: Float) {
    view.clipToOutline = true
    if (oldRadius != radius) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
    }
}

@BindingAdapter("textAppearanceAttr")
fun textAppearanceAttr(
    view: TextView,
    oldTextAppearanceStyleAttr: Int,
    textAppearanceStyleAttr: Int
) {
    if (oldTextAppearanceStyleAttr != textAppearanceStyleAttr) {
        view.setTextAppearance(view.context.resolveThemeReferenceResId(textAppearanceStyleAttr))
    }
}

@BindingAdapter("fontFamily")
fun fontFamily(view: TextView, oldFontFamily: Int, fontFamily: Int) {
    if (oldFontFamily != fontFamily) {
        view.typeface = try {
            ResourcesCompat.getFont(view.context, fontFamily)
        } catch (nfe: Resources.NotFoundException) {
            null
        } ?: Typeface.DEFAULT
    }
}


@BindingAdapter("selected")
fun viewSelected(view: View, oldSelected: Boolean, selected: Boolean) {
    if (oldSelected != selected) {
        view.isSelected = selected
    }
}


@BindingAdapter("enabled")
fun viewEnabled(view: View, oldEnabled: Boolean, enabled: Boolean) {
    if (oldEnabled != enabled) {
        view.isEnabled = enabled
    }
}

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

