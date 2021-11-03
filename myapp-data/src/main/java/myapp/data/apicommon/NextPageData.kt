package myapp.data.apicommon

import androidx.annotation.Keep

/**
 * 긴 리스트를 페이지로 받아서 분할해 볼 수 있게끔 해줌
 */
@Keep
data class NextPageData<T>(
    /**
     * 페이지 리스트
     */
    val list: List<T>,
    /**
     * 리스트의 끝인지 아닌지 판별
     */
    val hasMore: Boolean,
    /**
     * 다음 페이지 키값
     */
    val nextKey: String? = null,
    val append: Boolean = false// append는 앱내에서만 사용하는 필드다, 서버와 주고받는 데이터가 아님
) {
    // default constructor
    constructor() : this(list = emptyList(), hasMore = true, append = false, nextKey = null)

    companion object {
        fun <T : Any> createLastPageResult(list: List<T>): NextPageData<T> {
            return NextPageData(list = list, hasMore = false, nextKey = null, append = true)
        }
    }
}
