package myapp

import kotlinx.coroutines.delay
import java.io.IOException

sealed class InvokeStatus
object InvokeStarted : InvokeStatus()
object InvokeSuccess : InvokeStatus()
data class InvokeError(val throwable: Throwable) : InvokeStatus()

sealed class FetchStatus<out T> {
    object Loading : FetchStatus<Nothing>()
    data class Data<T>(val value: T) : FetchStatus<T>()
    data class Error(val error: Throwable) : FetchStatus<Nothing>()
}


suspend fun <T> retryIO(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
            e.printStackTrace()
        }
        //Timber.d("재시도 sleep ${currentDelay}ms")
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)

    }
    return block() // last attempt
}
