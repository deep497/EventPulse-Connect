package com.example.eventpulse.data.remote

import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface EventPulseApi {
    
    // Auth endpoints
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<User>

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<Map<String, Any>>

    // Event endpoints
    @GET("events")
    suspend fun getEvents(): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") eventId: String): Response<Event>

    @POST("events")
    suspend fun createEvent(@Body event: Event): Response<Event>

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") eventId: String, @Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String): Response<Unit>

    // Booking endpoints
    @GET("bookings")
    suspend fun getBookings(): Response<List<Booking>>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): Response<Booking>

    @POST("bookings")
    suspend fun createBooking(@Body booking: Map<String, Any>): Response<Booking>

    @POST("bookings/{id}/verify")
    suspend fun verifyBooking(@Path("id") bookingId: String): Response<Map<String, Any>>

    // Chat endpoints
    @POST("chat")
    suspend fun sendChatMessage(@Body message: Map<String, String>): Response<Map<String, String>>
}
