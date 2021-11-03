package myapp.data.entities

sealed class Result<T> {
    open fun get(): T? = null

    fun getOrThrow(): T = when (this) {
        is Success -> get()
        is ErrorResult -> throw throwable
    }
}

data class Success<T>(val data: T, val responseModified: Boolean = true) : Result<T>() {
    override fun get(): T = data
}

data class ErrorResult<T>(val throwable: Throwable) : Result<T>()


suspend fun <T, R> Result<T>.map(cb: suspend (T) -> R): Result<R> {
    if (this is ErrorResult) {
        return ErrorResult(this.throwable)
    }

    return try {
        val mappedData: R = cb((this as Success).data)
        Success(mappedData, this.responseModified)
    } catch (err: Throwable) {
        ErrorResult(err)
    }
}

suspend fun <T> Result<T>.onSuccess(cb: suspend (T) -> Unit): Result<T> {
    if (this is ErrorResult) {
        return ErrorResult(this.throwable)
    }

    cb((this as Success).data)
    return this
}
