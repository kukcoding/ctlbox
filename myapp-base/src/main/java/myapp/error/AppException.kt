package myapp.error

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class AppException(
    val errorCode: AppErrorCode,
    val customMessage: String? = null,
    val exception: Exception? = null
) : RuntimeException(errorCode.name) {

    override val message: String
        get() = displayMessage()

    companion object {
        fun translate(e: Exception): Exception {
            if (e is AppException) {
                return e
            }

            if (e is CancellationException) {
                return e
            }

            if (e is HttpException) {
                e.printStackTrace()
                return when (e.code()) {
                    401 -> AppErrorCode.E1_UNAUTHORIZED.toException()
                    403 -> AppErrorCode.E1_FORBIDDEN.toException()
                    404 -> AppErrorCode.E1_NOTFOUND.toException()
                    else -> {
                        AppErrorCode.E1_HTTP_ERROR.toException("${e.code()}")
                    }
                }
            }

            if (e is SocketTimeoutException) {
                return AppException(AppErrorCode.E1_NETWORK)
            }

            if (e is IOException) {
                return AppErrorCode.E1_NETWORK.toException(e.message)
            }

            return AppException(e)
        }
    }

    constructor(msg: String) : this(AppErrorCode.E1_UNKNOWN, msg)

    constructor(cause: Exception) : this(AppErrorCode.E1_UNKNOWN, null, cause)

    fun displayMessage(): String {
        if (!this.customMessage.isNullOrBlank()) return this.customMessage

        if (this.errorCode == AppErrorCode.E1_UNKNOWN) {
            val ex = this.exception
            if (ex != null && !ex.message.isNullOrBlank()) {
                return ex.message!!
            }
        }
        return this.errorCode.message
    }
}
