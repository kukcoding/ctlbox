package myapp.data.apicommon

import androidx.annotation.Keep

@Keep
data class TRResult(val success: Boolean, val errorCode: String?, val errorMessage: String?)


@Keep
data class TRApiResponse<T>(val success: Boolean, val errorCode: String?, val errorMessage: String?, val data: T? = null)
