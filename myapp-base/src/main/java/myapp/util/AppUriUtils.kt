package myapp.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

object AppUriUtils {
    fun parseUri(context: Context, file: File): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val authority = context.packageName + ".fileProvider"

            try {
                return FileProvider.getUriForFile(context, authority, file)
            } catch (ignore: Exception) {
                ignore.printStackTrace()
            }
        }

        return Uri.fromFile(file)
    }
}
