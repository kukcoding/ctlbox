package myapp.data.preferences

import splitties.preferences.PrefDelegate
import splitties.preferences.Preferences

object ApiPreference : Preferences("Api") {
    val accessToken = StringPref("accessToken", "")
    val cameraIp = StringPref("cameraIp", "")
    val cameraId = StringPref("cameraId", "")

    fun reset() {
        listOf<PrefDelegate<*>>(
            cameraIp,
            accessToken,
            cameraId,
        ).forEach { it.resetDefault() }
    }
}

