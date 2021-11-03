package com.google.ctlbox

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import myapp.appinitializers.AppInitializers
import myapp.ui.player.setupApplication
import myapp.util.AndroidUtils
import myapp.util.Logger
import splitties.init.injectAsAppCtx
import javax.inject.Inject

@HiltAndroidApp
class ApplicationLoader : Application() {
    @Inject
    lateinit var initializers: AppInitializers

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()
        // setup appCtx for spilitties
        this.injectAsAppCtx()
        AndroidUtils.init(this)
        initializers.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        setupApplication(this)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            AndroidUtils.init(this)
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
    }
}
