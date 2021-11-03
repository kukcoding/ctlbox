package myapp.data.preferences

import splitties.preferences.*


fun PrefDelegate<*>.resetDefault() {
    when (val t = this) {
        is BoolPref -> t.value = t.defaultValue
        is IntPref -> t.value = t.defaultValue
        is FloatPref -> t.value = t.defaultValue
        is LongPref -> t.value = t.defaultValue
        is StringPref -> t.value = t.defaultValue
        is StringOrNullPref -> t.value = t.defaultValue
        is StringSetPref -> t.value = t.defaultValue
        is StringSetOrNullPref -> t.value = t.defaultValue
    }
}
