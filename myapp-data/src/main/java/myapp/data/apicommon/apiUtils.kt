package myapp.data.apicommon

import kotlinx.coroutines.delay
import myapp.data.entities.ErrorResult
import myapp.data.entities.Result
import myapp.data.entities.Success
import myapp.error.AppErrorCode
import myapp.error.AppException
import myapp.extensions.bodyOrThrow
import myapp.extensions.defaultShouldRetry
import myapp.extensions.isFromNetwork
import myapp.extensions.toException
import retrofit2.Response


fun <T> Response<TRApiResponse<T>>.toApiDataResult(): Result<T> = try {
    if (isSuccessful) {
        val apiResponse = bodyOrThrow()
        if (apiResponse.result.success) {
            Success(data = apiResponse.data!!, responseModified = isFromNetwork())
        } else {
            val exception = AppErrorCode.parse(apiResponse.result.errorCode)?.toException()
                ?: AppException(msg = apiResponse.result.errorCode!!)
            ErrorResult(exception)
        }
    } else {
        ErrorResult(AppException.translate(toException()))
    }
} catch (e: Exception) {
    ErrorResult(e)
}

fun <T> Response<T>.toResult(): Result<T> = try {
    if (isSuccessful) {
        val apiResponse: T = bodyOrThrow()
        Success(data = apiResponse!!, responseModified = isFromNetwork())
    } else {
        ErrorResult(AppException.translate(toException()))
    }
} catch (e: Exception) {
    ErrorResult(e)
}

fun Response<Unit>.toApiSuccessOrNot(): Result<Unit> = try {
    if (isSuccessful) {
        Success(data = Unit, responseModified = isFromNetwork())
    } else {
        ErrorResult(AppException.translate(toException()))
    }
} catch (e: Exception) {
    ErrorResult(e)
}

suspend inline fun <T> callApi(
    defaultDelay: Long = 100,
    maxAttempts: Int = 1,
    shouldRetry: (Exception) -> Boolean = ::defaultShouldRetry,
    retrofitCall: () -> Response<T>
): Result<T> {
    repeat(maxAttempts) { attempt ->
        val nextDelay = attempt * attempt * defaultDelay
        try {
            // Clone a new ready call if needed
            return retrofitCall().toResult()
        } catch (e: Exception) {
            // The response failed, so lets see if we should retry again
            if (attempt == (maxAttempts - 1) || !shouldRetry(e)) {
                return ErrorResult(AppException.translate(e))
            }
        }

        delay(nextDelay)
    }

    // We should never hit here
    throw IllegalStateException("Unknown exception from executeWithRetry")
}


suspend inline fun execApi(
    defaultDelay: Long = 100,
    maxAttempts: Int = 1,
    shouldRetry: (Exception) -> Boolean = ::defaultShouldRetry,
    retrofitCall: () -> Response<Unit>
): Result<Unit> {
    repeat(maxAttempts) { attempt ->
        val nextDelay = attempt * attempt * defaultDelay
        try {
            // Clone a new ready call if needed
            return retrofitCall().toApiSuccessOrNot()
        } catch (e: Exception) {
            // The response failed, so lets see if we should retry again
            if (attempt == (maxAttempts - 1) || !shouldRetry(e)) {
                return ErrorResult(AppException.translate(e))
            }
        }

        delay(nextDelay)
    }

    // We should never hit here
    throw IllegalStateException("Unknown exception from executeWithRetry")
}
