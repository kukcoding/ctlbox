package myapp.inject

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.ctlbox.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import myapp.data.apicommon.ApiClientDeviceInfo
import myapp.data.cam.CamManager
import myapp.util.AndroidUtils
import java.io.File
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @ApplicationId
    @Provides
    fun provideApplicationId(application: Application): String = application.packageName

    @Provides
    @Singleton
    @Named("cache")
    fun provideCacheDir(
        @ApplicationContext context: Context
    ): File = context.cacheDir


    @Provides
    @Singleton
    @Named("fonts")
    fun provideFontsDir(
        @ApplicationContext context: Context
    ): File = File(context.filesDir, "fonts").also { it.mkdirs() }


    @Provides
    @Singleton
    fun provideApiClientDeviceInfo(
        @ApplicationContext context: Context
    ) = ApiClientDeviceInfo(
        certHash = AndroidUtils.getPackageSignature(context)!!,
        uuid = AndroidUtils.generateUUIDOnce(),
        appVersion = BuildConfig.VERSION_NAME,
        osVersion = Build.VERSION.SDK_INT,
        modelName = Build.MODEL
    )


    @Provides
    @Singleton
    fun provideCamManager(
        @ApplicationContext context: Context
    ) = CamManager(context)

}
