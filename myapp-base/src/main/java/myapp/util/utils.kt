package myapp.util

import java.util.concurrent.atomic.AtomicLong

fun generateNumericIdProvider(beginValue: Long = 0L): (Any) -> Long {
    val idCounter = AtomicLong(beginValue)
    val text = mutableMapOf<Any, Long>()
    return { key: Any ->
        val id = text[key]
        if (id != null) {
            id
        } else {
            val newId = idCounter.incrementAndGet()
            text[key] = newId
            newId
        }
    }
}
