package myapp.base.mediastore

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import splitties.init.appCtx
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("ObsoleteSdkInt")
fun refreshVideoGalleryFolder(file: File) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            MediaScannerConnection.scanFile(appCtx, arrayOf(file.absolutePath), null, null)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
            val mediaScanIntent = Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
            )
            mediaScanIntent.data = Uri.fromFile(file)
            appCtx.sendBroadcast(mediaScanIntent)
        }
        else -> {
            appCtx.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())
                )
            )
        }
    }
}


object MediaStoreHelper {
    // app specific album StorageDir
    fun getAlbumStorageDir(albumName: String): File {
        val file = File(appCtx.getExternalFilesDir(Environment.DIRECTORY_MOVIES), albumName)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Timber.e("Directory not created")
            }
            refreshVideoGalleryFolder(file)
        }
        return file
    }


    fun renameIfExists(folder: File, fileName: String): File {
        var file = File(folder, fileName)
        if (!file.exists()) return file
        val namePart = file.nameWithoutExtension
        val extension = file.extension

        var cnt = 1
        do {
            val newFileName = "${namePart}_${cnt}.${extension}"
            file = File(folder, newFileName)
            if (!file.exists()) return file
            cnt++
        } while (true)
    }


    fun copyVideoToGallery(videoFile: File, fileName: String, albumName: String) {
        val context = appCtx
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/${albumName}/")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val resolver = context.contentResolver
            val fileUri = resolver.insert(collection, values)!!
            resolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                descriptor?.let {
                    FileOutputStream(descriptor.fileDescriptor).use { out ->
                        FileInputStream(videoFile).copyTo(out, 8192)
                    }
                }
            }
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(fileUri, values, null, null)
        } else {
            val albumFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), albumName)
                .also {
                    if (!it.exists()) it.mkdirs()
                }
            albumFolder.mkdirs()
            if (albumFolder.exists()) {
                Timber.d("XXX albumFolder exists = $albumFolder")
            } else {
                Timber.w("XXX albumFolder NOT_EXISTS= $albumFolder")
            }
            val savedFile = videoFile.copyTo(renameIfExists(albumFolder, fileName), true)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, savedFile.name)
                put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Images.Media.DATA, savedFile.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            // context.contentResolver.update(savedFile.toUri(), values, null, null)
        }
    }


}

