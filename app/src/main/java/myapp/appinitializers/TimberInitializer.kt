package myapp.appinitializers

import android.app.Application
import com.google.ctlbox.BuildConfig
import myapp.util.MyLogger
import javax.inject.Inject

class TimberInitializer @Inject constructor(
    private val myLogger: MyLogger
) : AppInitializer {
    override fun init(application: Application) {
        myLogger.setup(BuildConfig.DEBUG)
    }
}

