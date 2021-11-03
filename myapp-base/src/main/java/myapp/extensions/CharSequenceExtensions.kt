package myapp.extensions

fun CharSequence?.isLongerThan(other: CharSequence?) = lengthOrZero() > other.lengthOrZero()

fun CharSequence?.lengthOrZero() = this?.length ?: 0


private val KO_CHOSUNG = listOf(
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
    'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
    'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
    'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)

fun CharSequence.firstChosung(): Char? {
    val ch = this.firstOrNull() ?: return null
    if (ch >= 0xAC00.toChar()) {
        val uniVal = (ch - 0xAC00).toInt()
        val cho = (uniVal - uniVal % 28) / 28 / 21

        return KO_CHOSUNG.getOrNull(cho)
    }
    return null
}

fun CharSequence.firstChosungOrChar(): Char? {
    val cho = firstChosung()
    if (cho != null) return cho
    return this.firstOrNull()
}
