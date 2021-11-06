package myapp.inject

import com.google.ctlbox.AppLifecycleObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import myapp.ActivityLauncher
import myapp.BuildConstantsImpl
import myapp.appinitializers.AppInitializer
import myapp.appinitializers.ThreeTenBpInitializer
import myapp.appinitializers.TimberInitializer
import myapp.settings.BuildConstants
import myapp.sys.AppLifecycleObserverImpl
import myapp.ui.ActivityLauncherImpl
import myapp.util.Logger
import myapp.util.MyLogger
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppModuleBinds {
    @Singleton
    @Binds
    abstract fun provideLogger(bind: MyLogger): Logger

    @Singleton
    @Binds
    abstract fun provideBuildConstants(bind: BuildConstantsImpl): BuildConstants

//    @Singleton
//    @Binds
//    abstract fun provideKeepScreenOnManager(bind: KeepScreenOnManagerImpl): KeepScreenOnManager

    @Singleton
    @Binds
    abstract fun provideActivityLauncher(bind: ActivityLauncherImpl): ActivityLauncher

    @Singleton
    @Binds
    abstract fun provideAppLifecycleObserver(bind: AppLifecycleObserverImpl): AppLifecycleObserver

    @Binds
    @IntoSet
    abstract fun provideThreeTenAbpInitializer(bind: ThreeTenBpInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun provideTimberInitializer(bind: TimberInitializer): AppInitializer

//    @Binds
//    @IntoSet
//    abstract fun providePreferencesInitializer(bind: UiPreferencesInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideClearGlideInitializer(bind: ClearGlideCacheInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideSplittiesInitializer(bind: SplittiesInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideNotificationInitializer(bind: NotificationInitializer): AppInitializer

}
