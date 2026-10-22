package com.example.eventpulse.data.repository

import com.example.eventpulse.data.local.TicketDao
import com.example.eventpulse.data.local.entities.TicketEntity
import com.example.eventpulse.data.model.Attendee
import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.model.ConfirmPaymentRequest
import com.example.eventpulse.data.model.ConfirmPaymentResponse
import com.example.eventpulse.data.model.OrderRequest
import com.example.eventpulse.data.model.OrderResponse
import com.example.eventpulse.data.remote.EventPulseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository that mediates between [EventPulseApi] (remote) and [TicketDao] (local Room cache).
 *
 * Offline strategy: every successful `getMyTickets()` call persists results to Room.
 * If the server is unreachable, the repository falls back to the locally cached tickets
 * so the user can still view their QR codes without a network connection.
 */
class BookingRepository(
    private val api: EventPulseApi,
    private val ticketDao: TicketDao
) {

    // ── Ticket listing (with offline fallback) ─────────────────────────────────

    suspend fun getMyTickets(): Result<List<Booking>> {
        return try {
            val response = api.getMyTickets()
            if (response.isSuccessful && response.body() != null) {
                val bookings = response.body()!!
                // Persist to local cache
                val entities = bookings.map { it.toTicketEntity() }
                ticketDao.insertTickets(entities)
                Result.success(bookings)
            } else {
                // Server error – fall back to cache
                val cached = ticketDao.getAllTickets().first().map { it.toBooking() }
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(Exception("Failed to fetch tickets: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Network unavailable – fall back to cache
            val cached = ticketDao.getAllTickets().first().map { it.toBooking() }
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }

    /** Exposes the local Room cache as a reactive [Flow] for offline ticket display. */
    fun getCachedTickets(): Flow<List<TicketEntity>> = ticketDao.getAllTickets()

    // ── Individual booking ─────────────────────────────────────────────────────

    suspend fun getBookings(): Result<List<Booking>> {
        return try {
            val response = api.getBookings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch bookings: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookingById(bookingId: String): Result<Booking> {
        return try {
            val response = api.getBookingById(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val cached = ticketDao.getTicketById(bookingId)?.toBooking()
                if (cached != null) Result.success(cached)
                else Result.failure(Exception("Booking not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            val cached = ticketDao.getTicketById(bookingId)?.toBooking()
            if (cached != null) Result.success(cached)
            else Result.failure(e)
        }
    }

    // ── Payment & Order creation ───────────────────────────────────────────────

    /**
     * Creates a payment order and pending booking via `POST /api/payments/order`.
     */
    suspend fun createPaymentOrder(eventId: String, quantity: Int): Result<OrderResponse> {
        return try {
            val response = api.createPaymentOrder(OrderRequest(eventId = eventId, quantity = quantity))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Confirms payment via `POST /api/payments/confirm`.
     * On success, caches confirmed ticket in Room.
     */
    suspend fun confirmPayment(
        paymentId: String,
        result: String,
        gatewayToken: String? = null
    ): Result<ConfirmPaymentResponse> {
        return try {
            val response = api.confirmPayment(
                ConfirmPaymentRequest(paymentId = paymentId, result = result, gatewayToken = gatewayToken)
            )
            if (response.isSuccessful && response.body() != null) {
                val confirmResponse = response.body()!!
                confirmResponse.booking?.let { booking ->
                    ticketDao.insertTicket(booking.toTicketEntity())
                }
                Result.success(confirmResponse)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancels a booking via `PATCH /api/bookings/:id/cancel` (subject to 48hr policy).
     */
    suspend fun cancelBooking(bookingId: String): Result<Map<String, Any>> {
        return try {
            val response = api.cancelBooking(bookingId)
            if (response.isSuccessful && response.body() != null) {
                ticketDao.updateTicketStatus(bookingId, "CANCELLED")
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Legacy compat
    suspend fun createBooking(eventId: String, ticketCount: Int): Result<Booking> {
        return try {
            val orderResult = createPaymentOrder(eventId, ticketCount)
            if (orderResult.isFailure) return Result.failure(orderResult.exceptionOrNull()!!)
            val order = orderResult.getOrThrow()
            if (order.status == "CONFIRMED") {
                val b = getBookingById(order.bookingId).getOrThrow()
                return Result.success(b)
            }
            val confirm = confirmPayment(order.paymentId, "SUCCESS").getOrThrow()
            Result.success(confirm.booking ?: getBookingById(order.bookingId).getOrThrow())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Check-in (Organizer) ───────────────────────────────────────────────────

    /**
     * Organizer check-in via `POST /api/organizer/check-in`.
     * Supports both qrCodeHash and manual bookingId entry.
     */
    suspend fun checkIn(qrCodeHash: String? = null, bookingId: String? = null): Result<Map<String, Any>> {
        return try {
            val body = mutableMapOf<String, String>()
            if (!qrCodeHash.isNullOrBlank()) body["qrCodeHash"] = qrCodeHash
            if (!bookingId.isNullOrBlank()) body["bookingId"] = bookingId

            val response = api.checkIn(body)
            if (response.isSuccessful && response.body() != null) {
                if (!qrCodeHash.isNullOrBlank()) {
                    val cached = ticketDao.getTicketByQrHash(qrCodeHash)
                    cached?.let { ticketDao.markCheckedIn(it.id) }
                } else if (!bookingId.isNullOrBlank()) {
                    ticketDao.markCheckedIn(bookingId)
                }
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches attendees for the organizer via `GET /api/organizer/attendees`.
     */
    suspend fun getOrganizerAttendees(eventId: String? = null): Result<List<Attendee>> {
        return try {
            val response = api.getOrganizerAttendees(eventId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private fun Booking.toTicketEntity(): TicketEntity {
        return TicketEntity(
            id = this.id,
            eventId = this.eventIdString,
            eventName = this.eventTitle.ifBlank { "Event Ticket" },
            eventDate = this.eventDate,
            eventVenue = this.eventLocation,
            quantity = this.ticketCount,
            totalPrice = this.totalPrice,
            qrCodeHash = this.qrCodeHash.ifBlank { "TICKET_${this.id}" },
            bookingDate = parseIsoMillis(this.createdAt),
            status = this.status,
            isCheckedIn = this.isCheckedIn
        )
    }

    private fun TicketEntity.toBooking() = Booking(
        id = this.id,
        eventId = this.eventId,
        ticketCount = this.quantity,
        totalPrice = this.totalPrice,
        status = this.status,
        qrCodeHash = this.qrCodeHash,
        isCheckedIn = this.isCheckedIn,
        createdAt = this.bookingDate.toString()
    )

    private fun parseIsoMillis(iso: String): Long {
        return try {
            java.time.Instant.parse(iso).toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
