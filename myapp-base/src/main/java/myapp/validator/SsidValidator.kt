package myapp.validator


object SsidValidator {
    private const val availCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_ "

    fun isValid(ssid: String): Boolean {
        if (ssid.length < 4 || ssid.length > 30) {
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
