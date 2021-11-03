package myapp.preference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import splitties.preferences.StringOrNullPref
import kotlin.reflect.KProperty

// spilitties에서 enum을 지원하지 않아서 추가 했다
inline fun <reified T : Enum<T>> enumPref(realPref: StringOrNullPref, defaultValue: T): EnumPref<T> {
    return EnumPref(realPref = realPref, values = enumValues(), defaultValue = defaultValue)
}

class EnumPref<T : Enum<T>> constructor(
    private val realPref: StringOrNullPref,
    private val values: Array<T>,
    val defaultValue: T,
) {
    fun valueFlow(): Flow<T> = realPref.valueFlow().map { valueToEnum(it) }

    var value: T
        get() = valueToEnum(realPref.value)
        set(newValue) {
            realPref.value = newValue.name
        }

    private fun valueToEnum(text: String?): T {
        if (text.isNullOrBlank()) return defaultValue
        return try {
            values.first { it.name == text }
            // enumValues<T>()
            // enumValueOf<T>()
        } catch (e: Throwable) {
            defaultValue
        }
    }

    fun resetDefault() {
        this.value = defaultValue
    }

    operator fun getValue(thisRef: Any?, prop: KProperty<*>?): Enum<T> {
        return valueToEnum(realPref.value)
    }

    operator fun setValue(thisRef: Any?, prop: KProperty<*>?, value: Enum<T>) {
        realPref.value = value.name
    }
}
