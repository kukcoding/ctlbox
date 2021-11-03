package myapp.validator


object WifiPasswordValidator {
    private const val availCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_!@#$%^&*()";

    fun isValid(pw: String): Boolean {
        if (pw.length < 4 || pw.length > 30) {
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
