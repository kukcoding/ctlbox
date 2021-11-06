package myapp.validator


object CameraNameValidator {
    private val REGEX =
        """[a-zA-Z 0-9가-힣\-_!@#$%^&*()]{2,30}$""".toRegex()


    fun isValid(value: String): Boolean {
        if (value.length < 2 || value.length > 30) return false
        if (value.isBlank()) return false

        return REGEX.matches(value)
    }
}
