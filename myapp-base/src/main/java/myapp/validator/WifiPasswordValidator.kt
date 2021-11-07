package myapp.validator

import myapp.BuildVars


object WifiPasswordValidator {
    private const val availCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_!@#$%^&*()";

    fun isValid(pw: String): Boolean {
        if (pw.length < BuildVars.wifiPwMinLength || pw.length > BuildVars.wifiPwMaxLength) {
            return false
        }

        for (ch in pw) {
            if (availCharacters.indexOf(ch) < 0) {
                return false
            }
        }
        return true
    }
}
