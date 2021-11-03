package myapp.data.code

enum class FileType constructor(val prefix: String) {
    IMAGE("i"),
    VIDEO("v"),
    AUDIO("a"),
    BINARY("b");


    companion object {

        fun findByPrefix(prefix: String) = values().singleOrNull { it.prefix == prefix }

        fun parse(str: String?, defaultValue: FileType? = null): FileType? {
            if (str.isNullOrBlank())
                return defaultValue

            return try {
                valueOf(str)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
}
