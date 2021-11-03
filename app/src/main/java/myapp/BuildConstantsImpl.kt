package myapp

import com.google.ctlbox.BuildConfig
import myapp.settings.BuildConstants
import javax.inject.Inject

class BuildConstantsImpl @Inject constructor() : BuildConstants {

    private fun ensureLastSlash(url: String): String {
        return if (url.endsWith("/")) url
        else "$url/"
    }

    override val marketUrl = BuildConfig.MARKET_URL
    override val playStoreUrl = BuildConfig.PLAYSTORE_URL
    override val appAdminEmail = BuildConfig.APP_ADMIN_EMAIL

    override val apiBaseUrl = ensureLastSlash(BuildConfig.API_BASE_URL)
    private val serverBaseUrl = ensureLastSlash(BuildConfig.SERVER_BASE_URL)

    fun serverUrl(path: String): String {
        if (path.startsWith('/'))
            return "$serverBaseUrl${path.substring(1)}"
        return "$serverBaseUrl$path"
    }

}
