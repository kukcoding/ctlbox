package myapp.util

import androidx.annotation.DrawableRes
import myapp.ui.common.R

object UiVars {
    private var noDataIconSeq = 0
    private val noDataIcons = listOf(
        R.drawable.emo1,
        R.drawable.emo2,
        R.drawable.emo3,
        R.drawable.emo4,
        R.drawable.emo5,
        R.drawable.emo6,
        R.drawable.emo7,
        R.drawable.emo8
    ).shuffled()

    @DrawableRes
    fun nextNoDataIcon(): Int {
        val index = noDataIconSeq % noDataIcons.size
        noDataIconSeq += 1
        if (noDataIconSeq >= noDataIcons.size) {
            noDataIconSeq = 0
        }
        return noDataIcons[index]
    }
}
