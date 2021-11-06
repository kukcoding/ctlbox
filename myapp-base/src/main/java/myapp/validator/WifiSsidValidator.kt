package myapp.validator

import myapp.BuildVars


object WifiSsidValidator {
    private const val availCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_ "

    fun isValid(ssid: String): Boolean {
        if (ssid.length < BuildVars.wifiSsidMinLength || ssid.length > BuildVars.wifiSsidMaxLength) {
            return false
        }
        for (ch in ssid) {
            if (availCharacters.indexOf(ch) < 0) {
                return false
            }
        }
        return true
    }
}
