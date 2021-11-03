package myapp.inject

import com.google.ctlbox.BuildConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import myapp.TRApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitModule {

    @Provides
    @Named("tr-api-base-url")
    fun provideTRApiBaseUrl() = BuildConfig.API_BASE_URL

    @JvmStatic
    @Provides
    @Singleton
    @TRApi
    fun provideRetrofitBuilder(
        @TRApi okHttpClient: OkHttpClient,
        @Named("tr-api-base-url") apiBaseUrl: String,
        moshi: Moshi
    ): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
}
