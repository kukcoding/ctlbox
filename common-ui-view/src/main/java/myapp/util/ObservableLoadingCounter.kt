package myapp.util

import kotlinx.coroutines.flow.*
import myapp.InvokeError
import myapp.InvokeStarted
import myapp.InvokeStatus
import myapp.InvokeSuccess
import java.util.concurrent.atomic.AtomicInteger

class ObservableLoadingCounter {
    private val count = AtomicInteger()
    private val loadingState = MutableStateFlow(count.get())

    val observable: Flow<Boolean>
        get() = loadingState.map { it > 0 }.distinctUntilChanged()

    fun addLoader() {
        loadingState.value = count.incrementAndGet()
    }

    fun removeLoader() {
        loadingState.value = count.decrementAndGet()
    }
}


suspend fun Flow<InvokeStatus>.collectInto(counter: ObservableLoadingCounter) = collect {
    when (it) {
        InvokeStarted -> counter.addLoader()
        InvokeSuccess, is InvokeError -> counter.removeLoader()
    }
}
