package com.example.eventpulse.data.remote

import com.example.eventpulse.data.model.Attendee
import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.model.ChatRequest
import com.example.eventpulse.data.model.ChatResponse
import com.example.eventpulse.data.model.ConfirmPaymentRequest
import com.example.eventpulse.data.model.ConfirmPaymentResponse
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.model.OrderRequest
import com.example.eventpulse.data.model.OrderResponse
import com.example.eventpulse.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface EventPulseApi {

    // ── Auth endpoints ──────────────────────────────────────────────────────────
    /** POST /api/auth/register – returns { token, user } */
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<Map<String, Any>>

    /** POST /api/auth/login – returns { token, user: { id, name, email, role } } */
    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<Map<String, Any>>

    /** PUT /api/auth/profile – updates user's own profile */
    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<User>

    // ── Event endpoints ─────────────────────────────────────────────────────────
    /** GET /api/events?status=&category=&search= */
    @GET("events")
    suspend fun getEvents(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<Event>>

    /** GET /api/events/organizer – organizer's own events */
    @GET("events/organizer")
    suspend fun getOrganizerEvents(): Response<List<Event>>

    /** GET /api/events/:id */
    @GET("events/{id}")
    suspend fun getEventById(@Path("id") eventId: String): Response<Event>

    /** POST /api/events – creates with status=PENDING_APPROVAL */
    @POST("events")
    suspend fun createEvent(@Body event: Event): Response<Event>

    /** PUT /api/events/:id */
    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") eventId: String, @Body event: Event): Response<Event>

    /** DELETE /api/events/:id */
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String): Response<Map<String, Any>>

    // ── Payment & Booking endpoints ─────────────────────────────────────────────
    /** POST /api/payments/order – creates pending booking & payment order */
    @POST("payments/order")
    suspend fun createPaymentOrder(@Body request: OrderRequest): Response<OrderResponse>

    /** POST /api/payments/confirm – confirms payment result */
    @POST("payments/confirm")
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequest): Response<ConfirmPaymentResponse>

    /** GET /api/bookings/my-tickets */
    @GET("bookings/my-tickets")
    suspend fun getMyTickets(): Response<List<Booking>>

    /** GET /api/bookings */
    @GET("bookings")
    suspend fun getBookings(): Response<List<Booking>>

    /** GET /api/bookings/:id */
    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): Response<Booking>

    /** PATCH /api/bookings/:id/cancel – 48hr cancellation policy */
    @PATCH("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") bookingId: String): Response<Map<String, Any>>

    /** POST /api/organizer/check-in – body: { qrCodeHash? or bookingId? } */
    @POST("organizer/check-in")
    suspend fun checkIn(@Body body: Map<String, String>): Response<Map<String, Any>>

    /** GET /api/organizer/attendees */
    @GET("organizer/attendees")
    suspend fun getOrganizerAttendees(@Query("eventId") eventId: String? = null): Response<List<Attendee>>

    // ── Admin endpoints ─────────────────────────────────────────────────────────
    /** GET /api/admin/pending-events */
    @GET("admin/pending-events")
    suspend fun getPendingEvents(): Response<List<Event>>

    /** PATCH /api/admin/events/:id/status – body: { status: "PUBLISHED"|"REJECTED" } */
    @PATCH("admin/events/{id}/status")
    suspend fun updateEventStatus(
        @Path("id") eventId: String,
        @Body body: Map<String, String>
    ): Response<Event>

    /** GET /api/admin/metrics */
    @GET("admin/metrics")
    suspend fun getAdminMetrics(): Response<Map<String, Any>>

    /** GET /api/admin/users */
    @GET("admin/users")
    suspend fun getAdminUsers(): Response<List<User>>

    /** PATCH /api/admin/users/:id/block – body: { isBlocked: Boolean } */
    @PATCH("admin/users/{id}/block")
    suspend fun setUserBlockedStatus(
        @Path("id") userId: String,
        @Body body: Map<String, Boolean>
    ): Response<Map<String, Any>>

    // ── Chat / Gemini proxy ─────────────────────────────────────────────────────
    /** POST /api/chat – body: ChatRequest, returns ChatResponse */
    @POST("chat")
    suspend fun sendChatMessage(@Body request: ChatRequest): Response<ChatResponse>
}
