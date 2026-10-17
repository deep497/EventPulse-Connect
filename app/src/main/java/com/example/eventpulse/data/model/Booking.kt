package com.example.eventpulse.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    @SerializedName("_id") val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val ticketCount: Int = 0,
    val totalPrice: Double = 0.0,
    val status: String = "CONFIRMED",
    /** SHA-256 hash stored in the backend Booking document; also used as QR payload. */
    val qrCodeHash: String = "",
    val isCheckedIn: Boolean = false,
    val checkedInAt: String? = null,
    /** ISO-8601 timestamp from MongoDB `createdAt`. */
    val createdAt: String = ""
)
