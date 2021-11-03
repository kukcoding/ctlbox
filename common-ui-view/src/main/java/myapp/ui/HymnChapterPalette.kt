package myapp.ui

import android.graphics.Color
import androidx.annotation.ColorInt

enum class HymnChapterPalette(val beginChapter: Int, @ColorInt val color: Int) {
    P1(100, Color.parseColor("#8e24aa")),
    P2(200, Color.parseColor("#f4511e")),
    P3(300, Color.parseColor("#43a047")),
    P4(400, Color.parseColor("#00897b")),
    P5(500, Color.parseColor("#00acc1")),
    P6(600, Color.parseColor("#5e35b1")),
    P7(700, Color.parseColor("#3949ab"))
    ;

    companion object {
        fun paletteOfChapter(chapter: Int): HymnChapterPalette? {
            return when {
                chapter >= P7.beginChapter -> P7
                chapter >= P6.beginChapter -> P6
                chapter >= P5.beginChapter -> P5
                chapter >= P4.beginChapter -> P4
                chapter >= P3.beginChapter -> P3
                chapter >= P2.beginChapter -> P2
                chapter >= P1.beginChapter -> P1
                else -> null
            }
        }
    }
}
