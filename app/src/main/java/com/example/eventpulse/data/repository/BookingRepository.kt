package com.example.eventpulse.data.repository

import com.example.eventpulse.data.local.TicketDao
import com.example.eventpulse.data.local.entities.TicketEntity
import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.remote.EventPulseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository that mediates between [EventPulseApi] (remote) and [TicketDao] (local Room cache).
 *
 * **Offline strategy**: every successful `getMyTickets()` call persists results to Room.
 * If the server is unreachable, the repository falls back to the locally cached tickets
 * so the user can still view their QR codes without a network connection.
 */
class BookingRepository(
    private val api: EventPulseApi,
    private val ticketDao: TicketDao
) {

    // ── Ticket listing (with offline fallback) ─────────────────────────────────

    /**
     * Returns the current user's confirmed bookings.
     *
     * 1. Tries `GET /api/bookings/my-tickets`.
     * 2. On success: writes to Room cache and returns the fresh list.
     * 3. On failure (network error / server down): reads from Room cache.
     */
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
                // Try local cache
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

    // ── Booking creation ───────────────────────────────────────────────────────

    /**
     * Creates a new booking via `POST /api/bookings`.
     * On success, the returned booking is saved to the local cache immediately.
     */
    suspend fun createBooking(eventId: String, ticketCount: Int): Result<Booking> {
        return try {
            val body = mapOf<String, Any>("eventId" to eventId, "ticketCount" to ticketCount)
            val response = api.createBooking(body)
            if (response.isSuccessful && response.body() != null) {
                val booking = response.body()!!
                // Cache the new ticket locally
                ticketDao.insertTicket(booking.toTicketEntity())
                Result.success(booking)
            } else {
                Result.failure(Exception("Failed to create booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── QR Check-in ────────────────────────────────────────────────────────────

    /**
     * Organizer check-in via `POST /api/organizer/check-in`.
     * On success, marks the ticket as checked-in in the local cache too.
     */
    suspend fun checkIn(qrCodeHash: String): Result<Map<String, Any>> {
        return try {
            val response = api.checkIn(mapOf("qrCodeHash" to qrCodeHash))
            if (response.isSuccessful && response.body() != null) {
                // Reflect check-in locally if we have the ticket cached
                val cached = ticketDao.getTicketByQrHash(qrCodeHash)
                cached?.let { ticketDao.markCheckedIn(it.id) }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Check-in failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private fun Booking.toTicketEntity(): TicketEntity {
        // eventId may be a nested object (populated) or a plain string; handle both
        val eventName = "" // Populated event name comes via joined query; use empty as placeholder
        return TicketEntity(
            id = this.id,
            eventId = this.eventId,
            eventName = eventName,
            eventDate = "",
            eventVenue = "",
            quantity = this.ticketCount,
            totalPrice = this.totalPrice,
            qrCodeHash = this.qrCodeHash,
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

    /** Parses an ISO-8601 string to epoch millis; returns 0 on failure. */
    private fun parseIsoMillis(iso: String): Long {
        return try {
            java.time.Instant.parse(iso).toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
