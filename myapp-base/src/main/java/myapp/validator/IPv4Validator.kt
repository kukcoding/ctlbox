package myapp.validator

import java.util.regex.Pattern


object IPv4Validator {
    private const val IPV4_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$"
    private val pattern = Pattern.compile(IPV4_PATTERN)

    fun isValid(str: String?): Boolean {
        val matcher = pattern.matcher(str)
        return matcher.matches()
    }
}
