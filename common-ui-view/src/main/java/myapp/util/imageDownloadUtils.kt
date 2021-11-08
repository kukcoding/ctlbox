package myapp.util

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


suspend fun downloadOriginalImageGlide(requestManager: RequestManager, imageUrl: String): File? {
    // ex) file = /data/user/0/com.google.ctlbox/cache/image_manager_disk_cache/2657d7ad7f82e2.0
    return withContext(Dispatchers.IO) {
        requestManager.downloadOnly().load(imageUrl).submit().get()
    }
}

suspend fun downloadOriginalImageGlide(context: Context, imageUrl: String): File? {
    // ex) file = /data/user/0/com.google.ctlbox/cache/image_manager_disk_cache/2657d7ad7f82e2.0
    return withContext(Dispatchers.IO) {
        Glide.with(context).downloadOnly().load(imageUrl).submit().get()
    }
}

suspend fun downloadOriginalImageGlide(context: Context, imageSize: Int, imageUrl: String): File? {
    // ex) file = /data/user/0/com.google.ctlbox/cache/image_manager_disk_cache/2657d7ad7f82e2.0
    return withContext(Dispatchers.IO) {
        Glide.with(context).downloadOnly().load(imageUrl).submit(imageSize, imageSize).get()
    }
}

