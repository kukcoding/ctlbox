package myapp.settings

interface UiPreference {
    fun setup()

    var themePreference: Theme

    enum class Theme {
        LIGHT,
        DARK,
        SYSTEM
    }
}