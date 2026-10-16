package com.example.eventpulse.data.repository

import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.remote.EventPulseApi

class BookingRepository(
    private val api: EventPulseApi
) {
    
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
                Result.failure(Exception("Failed to fetch booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBooking(eventId: String, ticketCount: Int): Result<Booking> {
        return try {
            val body = mapOf(
                "eventId" to eventId,
                "ticketCount" to ticketCount
            )
            val response = api.createBooking(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyBooking(bookingId: String): Result<Map<String, Any>> {
        return try {
            val response = api.verifyBooking(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to verify booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
