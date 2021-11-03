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

    E1_NO_SUCH_DOC("해당 문서가 없습니다"),
    E1_INVALID_PARAM("파라미터가 유효하지 않습니다"),
    E1_NO_SUCH_DATA("해당 데이터가 없습니다"),
    E1_NO_SUCH_DOC_MENU("해당 메뉴가 없습니다"),
    E1_NO_SUCH_DOC_MENU_PAGE("해당 메뉴 페이지가 없습니다"),
    E1_NO_SUCH_TUBE_VIDEO("등록된 영상이 없습니다"),

    E1_NETWORK("네트워크 연결이 불안합니다"),
    E1_FACEBOOK_LOGIN_FAIL("Facebook 로그인이 실패했습니다"),
    E1_GOOGLE_LOGIN_FAIL("Google 로그인이 실패했습니다"),
    E1_FILE_OPEN_FAILURE("파일 Open failure"),
    E1_BIBLE_TODAY_SYNC_FAIL("오늘의 성경 동기화가 실패했습니다"),
    E1_FONT_LOAD_FAIL("폰트를 로드하지 못했습니다"),
    E1_NO_SUCH_FONT("해당 폰트가 존재하지 않습니다"),

    E1_NO_SUCH_JEBO("해당 요청이 없습니다"),
    E1_JEBO_DELETED("삭제된 요청입니다"),
    E1_JEBO_CANNOT_MODIFY("수정할 수 없는 상태입니다"),
    E1_JEBO_NOT_OWNER("다른 사람의 요청입니다"),
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
