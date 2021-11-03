package myapp.extensions

import java.io.Closeable

fun Closeable.closeQuietly() {
    try {
        this.close()
    } catch (ignore: Throwable) {
    }
}
