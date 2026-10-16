package com.example.eventpulse.data.remote

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import com.example.eventpulse.data.local.SessionManager

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Get JWT token synchronously
        val token = runBlocking {
            sessionManager.getJwtToken().first()
        }

        // Add Authorization header if token exists
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}
