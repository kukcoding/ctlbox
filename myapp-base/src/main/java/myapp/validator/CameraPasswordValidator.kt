package myapp.validator

import myapp.BuildVars


object CameraPasswordValidator {
    private const val availCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_!@#$%^&*()";

    fun isValid(pw: String): Boolean {
        if (pw.length < BuildVars.cameraPwMinLength || pw.length > BuildVars.cameraPwMaxLength) {
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
