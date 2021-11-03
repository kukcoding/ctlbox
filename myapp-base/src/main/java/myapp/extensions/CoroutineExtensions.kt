package myapp.extensions

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

fun <T, R> Flow<T?>.flatMapLatestNullable(transform: suspend (value: T) -> Flow<R>): Flow<R?> {
    return flatMapLatest { if (it != null) transform(it) else flowOf(null) }
}

fun <T, R> Flow<T?>.mapNullable(transform: suspend (value: T) -> R): Flow<R?> {
    return map { if (it != null) transform(it) else null }
}

val deferreds = ConcurrentHashMap<Any, Deferred<*>>()
val deferredsCleanLaunched = AtomicBoolean()

suspend inline fun <T> asyncOrAwait(
    key: Any,
    crossinline action: suspend CoroutineScope.() -> T
): T = coroutineScope {
    val deferred = deferreds[key]?.takeIf { it.isActive }
        ?: async { action() }.also { deferreds[key] = it }

    if (deferreds.size > 100 && !deferredsCleanLaunched.getAndSet(true)) {
        launch {
            // Remove any complete entries
            deferreds.entries.removeAll { it.value.isCompleted }
            deferredsCleanLaunched.set(false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    deferred.await() as T
}

val jobs = ConcurrentHashMap<Any, Job>()
val jobsCleanLaunched = AtomicBoolean()

suspend inline fun launchOrJoin(
    key: Any,
    crossinline action: suspend CoroutineScope.() -> Unit
) = coroutineScope {
    val job = jobs[key]?.takeIf { it.isActive }
        ?: launch { action() }
            .also { jobs[key] = it }

    if (jobs.size > 100 && !jobsCleanLaunched.getAndSet(true)) {
        launch {
            // Remove any complete entries
            jobs.entries.removeAll { it.value.isCompleted }
            jobsCleanLaunched.set(false)
        }
    }

    job.join()
}

fun <T> delayFlow(timeout: Long, value: T): Flow<T> = flow {
    delay(timeout)
    emit(value)
}
