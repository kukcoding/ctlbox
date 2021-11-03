package myapp.inject

import android.content.Context
import com.google.ctlbox.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import myapp.TRApi
import myapp.data.preferences.ApiPreference
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

val DEBUG = true

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor? {
        if (!BuildConfig.DEBUG) {
            return null
        }
// 임시로
//        if (BuildConfig.DEBUG) return null

        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    @Singleton
    @Provides
    fun provideHttpEventListener(): LoggingEventListener.Factory? {
        if (!BuildConfig.DEBUG) {
            return null
        }

        // 임시로
        if (BuildConfig.DEBUG) return null
        return LoggingEventListener.Factory()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor?,
        loggingEventListener: LoggingEventListener.Factory?,
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                if (httpLoggingInterceptor != null) {
                    addInterceptor(httpLoggingInterceptor)
                }
                if (loggingEventListener != null) {
                    eventListenerFactory(loggingEventListener)
                }
            }
            // Around 4¢ worth of storage in 2020
            .cache(Cache(File(context.cacheDir, "api_cache"), 50 * 1024 * 1024))
            // Adjust the Connection pool to account for historical use of 3 separate clients
            // but reduce the keepAlive to 2 minutes to avoid keeping radio open.
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            .dispatcher(
                Dispatcher().apply {
                    // Allow for high number of concurrent image fetches on same host.
                    maxRequestsPerHost = 15
                }
            )
            .build()
    }


    @Singleton
    @Provides
    @TRApi
    fun provideTROkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor?,
        loggingEventListener: LoggingEventListener.Factory?,
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                addInterceptor(AuthRequestInterceptor())
                if (httpLoggingInterceptor != null) {
                    addInterceptor(httpLoggingInterceptor)
                }
                if (loggingEventListener != null) {
                    eventListenerFactory(loggingEventListener)
                }
            }
            // Around 4¢ worth of storage in 2020
            .cache(Cache(File(context.cacheDir, "api_cache"), 50 * 1024 * 1024))
            // Adjust the Connection pool to account for historical use of 3 separate clients
            // but reduce the keepAlive to 2 minutes to avoid keeping radio open.
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            .dispatcher(
                Dispatcher().apply {
                    // Allow for high number of concurrent image fetches on same host.
                    maxRequestsPerHost = 15
                }
            )
            .build()
    }
}


class AuthRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val url = request.url
        if (isAuthRequired(url)) {
            val token = ApiPreference.accessToken.value
            if (token.isNotBlank()) {
                if (DEBUG) Timber.d("XXX intercept request => x-custom-auth: $token")
                val newRequest = request.newBuilder().header("x-custom-auth", token).build()
                return chain.proceed(newRequest)
            }
        }

        return chain.proceed(request)
    }

    private fun isAuthRequired(url: HttpUrl): Boolean {
        return !url.encodedPath.startsWith("/login")
    }
}


