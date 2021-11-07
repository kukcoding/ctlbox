package myapp.validator

import myapp.BuildVars


object CameraNameValidator {
    private val REGEX by lazy {
        val range = String.format("{%d,%d}$", BuildVars.cameraNameMinLength, BuildVars.cameraNameMaxLength)
        val txt = """[a-zA-Z 0-9가-힣\-_!@#$%^&*()]${range}"""
        txt.toRegex()
    }

    fun isValid(value: String): Boolean {
        if (value.length < BuildVars.cameraNameMinLength || value.length > BuildVars.cameraNameMaxLength) return false
        if (value.isBlank()) return false

        return REGEX.matches(value)
    }
}
