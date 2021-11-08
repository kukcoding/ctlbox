package myapp.ui.home.leftmenu

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import myapp.ui.home.R

enum class LeftMenu(
    @DrawableRes val iconResId: Int,
    @ColorInt val iconTint: Int,
    @StringRes val labelResId: Int
) {
    DUMMY(R.drawable.ic_outline_help_outline_24, Color.parseColor("#FFD2A6"), R.string.dummy)
}
