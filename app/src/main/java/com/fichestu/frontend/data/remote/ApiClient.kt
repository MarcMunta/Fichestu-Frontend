package com.fichestu.frontend.data.remote

import com.fichestu.frontend.BuildConfig
import com.fichestu.frontend.data.repository.SessionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private fun buildRetrofit(
        connectTimeoutSeconds: Long,
        readTimeoutSeconds: Long,
        writeTimeoutSeconds: Long,
        callTimeoutSeconds: Long
    ): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val path = chain.request().url.encodedPath
                val language = if (path.startsWith("/api/auth")) {
                    "en"
                } else {
                    SessionStore.languageCode()
                }
                val request = chain.request().newBuilder()
                    .header("Accept-Language", language)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
            .callTimeout(callTimeoutSeconds, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        buildRetrofit(
            connectTimeoutSeconds = 15,
            readTimeoutSeconds = 20,
            writeTimeoutSeconds = 20,
            callTimeoutSeconds = 25
        )
    }

    private val authRetrofit: Retrofit by lazy {
        buildRetrofit(
            connectTimeoutSeconds = 45,
            readTimeoutSeconds = 180,
            writeTimeoutSeconds = 45,
            callTimeoutSeconds = 180
        )
    }

    val authApi: AuthApi by lazy {
        authRetrofit.create(AuthApi::class.java)
    }

    val gameApi: GameApi by lazy {
        retrofit.create(GameApi::class.java)
    }

    val marketApi: MarketApi by lazy {
        retrofit.create(MarketApi::class.java)
    }

    val userApi: UserApi by lazy {
        authRetrofit.create(UserApi::class.java)
    }

    val profileApi: ProfileApi by lazy {
        retrofit.create(ProfileApi::class.java)
    }

    val notificationApi: NotificationApi by lazy {
        retrofit.create(NotificationApi::class.java)
    }
}
