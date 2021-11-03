package myapp.data.preferences
import splitties.preferences.PrefDelegate
import splitties.preferences.Preferences

object AppPreference : Preferences("App") {

    /**
     * 관리자 여부
     */
    val isAdmin = BoolPref("isAdmin", false)

    /**
     * 인트로 봤는지 여부
     */
    var introSeen = BoolPref("introSeen", false)

    /**
     * 왼쪽 메뉴 봤는지 여부
     */
    val seenLeftMenu = BoolPref("seenLeftMenu", false)

    /**
     * 화면 켜짐 유지 여부
     */
    val useKeepScreenOn = BoolPref("useKeepScreenOn", true)


    fun dummy() {}

    fun reset() {
        listOf<PrefDelegate<*>>(
            isAdmin,
            seenLeftMenu,
            useKeepScreenOn,
        ).forEach { it.resetDefault() }
    }

}

