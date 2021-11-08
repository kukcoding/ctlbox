package myapp.error

import androidx.annotation.Keep

@Keep
enum class AppErrorCode(val message: String) {
    E1_UNKNOWN("오류가 발생했습니다"),
    E1_AUTH_EXPIRED("인증이 만료되었습니다"),
    E1_UNAUTHORIZED("로그인이 필요합니다"),
    E1_CANCEL("취소"),
    E1_FORBIDDEN("접근 권한이 없습니다"),
    E1_AUTH_INVALID_TOKEN("인증 토큰이 유효하지 않습니다"),
    E1_NOTFOUND("해당 URL이 존재하지 않습니다"),
    E1_HTTP_ERROR("HTTP Unknown error"),

    E1_INVALID_PARAM("파라미터가 유효하지 않습니다"),
    E1_NO_SUCH_DATA("해당 데이터가 없습니다"),
    E1_NETWORK("네트워크 연결이 불안합니다"),

    // camera
    E_INVALID_PW("비밀번호가 일치하지 않습니다"),
    ;


    fun toException(msg: String? = null) = AppException(this, msg)

    fun raise(msg: String? = null): Nothing = throw toException(msg)

    companion object {
        fun parse(str: String?, defaultValue: AppErrorCode? = null): AppErrorCode? {
            if (str.isNullOrBlank())
                return defaultValue

            return try {
                valueOf(str)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
}

fun AppErrorCode.isMatched(throwable: Throwable): Boolean {
    return throwable is AppException && throwable.errorCode == this
}
