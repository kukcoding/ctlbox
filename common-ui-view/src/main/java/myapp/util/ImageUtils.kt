package myapp.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageUtils {
    suspend fun getImageSize(context: Context, uri: Uri): Size {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                Size(options.outWidth, options.outHeight)
            }
        }
    }

    fun tempImageDir(context: Context): File {
        return File(context.cacheDir, "temp-image")
    }

    fun tempImageFile(context: Context, suffix: String): File {
        val dir = tempImageDir(context).also { if (!it.exists()) it.mkdirs() }
        return File.createTempFile("temp", suffix, dir)
    }
}
