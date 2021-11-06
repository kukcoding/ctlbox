package myapp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow


fun flowInterval(
    initialDelayMillis: Long,
    delayMillis: Long
) = flow {
    if (initialDelayMillis > 0) {
        delay(initialDelayMillis)
    }
    emit(System.currentTimeMillis())
    while (true) {
        delay(delayMillis)
        emit(System.currentTimeMillis())
    }
}.cancellable().buffer()
