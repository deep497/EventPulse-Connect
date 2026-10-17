package com.example.eventpulse.data.remote

import com.example.eventpulse.BuildConfig
import com.example.eventpulse.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Builds an OkHttpClient that:
     *  - Attaches `Authorization: Bearer <token>` via [AuthInterceptor]
     *  - Logs request/response bodies in DEBUG builds only
     *  - Applies 30-second timeouts
     */
    private fun getOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Creates the [EventPulseApi] Retrofit service.
     *
     * Base URL comes from [BuildConfig.BASE_URL]:
     *  - debug  → http://10.0.2.2:5000/api/  (local emulator)
     *  - release → https://eventpulse-api.onrender.com/api/
     */
    fun provideApi(sessionManager: SessionManager): EventPulseApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(getOkHttpClient(sessionManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventPulseApi::class.java)
    }
}
