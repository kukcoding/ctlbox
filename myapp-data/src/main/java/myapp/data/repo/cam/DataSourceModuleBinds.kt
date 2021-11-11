package myapp.data.repo.cam

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import myapp.data.repo.cam.api.CamDataSource
import myapp.data.repo.cam.api.CamDataSourceImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DataSourceModuleBinds {
    @Singleton
    @Binds
    abstract fun provideCamDataSource(bind: CamDataSourceImpl): CamDataSource
}
