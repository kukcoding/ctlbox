package myapp.data.repo.cam.api

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import myapp.TRApi
import retrofit2.Retrofit

@InstallIn(SingletonComponent::class)
@Module
object CamModule {
    @JvmStatic
    @Provides
    @Reusable
    fun provideCamApi(@TRApi builder: Retrofit.Builder) = builder.build().create(CamApi::class.java)
}
