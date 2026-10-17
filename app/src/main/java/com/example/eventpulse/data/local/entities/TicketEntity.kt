package com.example.eventpulse.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that caches confirmed [com.example.eventpulse.data.model.Booking] records locally.
 *
 * Stored whenever a booking is successfully fetched from the backend so that the user
 * can still view their tickets when the device is offline.
 */
@Entity(tableName = "tickets")
data class TicketEntity(
    /** Server-assigned booking _id (MongoDB ObjectId string). */
    @PrimaryKey
    val id: String,

    /** MongoDB ObjectId of the linked Event document. */
    val eventId: String,

    val eventName: String,
    val eventDate: String,
    val eventVenue: String,

    /** Number of seats booked. */
    val quantity: Int,

    val totalPrice: Double,

    /**
     * SHA-256 hash of the booking, used as the QR code payload.
     * The backend stores this as `qrCodeHash` in the Booking document.
     */
    val qrCodeHash: String,

    /** Epoch millis when the booking was created (`createdAt` from the server). */
    val bookingDate: Long,

    /**
     * Booking status mirrored from the server.
     * Possible values: `CONFIRMED`, `USED`, `CANCELLED`
     */
    val status: String,

    /**
     * Whether the attendee has been scanned/checked-in at the venue.
     * Mirrors `isCheckedIn` from the Booking document.
     */
    val isCheckedIn: Boolean = false
)
