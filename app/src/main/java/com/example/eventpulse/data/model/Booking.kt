package com.example.eventpulse.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    @SerializedName("_id") val id: String = "",
    val eventId: Any? = null,
    val userId: String = "",
    val ticketCount: Int = 0,
    val totalPrice: Double = 0.0,
    val status: String = "CONFIRMED",
    /** SHA-256 hash stored in the backend Booking document; also used as QR payload. */
    val qrCodeHash: String = "",
    val isCheckedIn: Boolean = false,
    val checkedInAt: String? = null,
    val expiresAt: String? = null,
    /** ISO-8601 timestamp from MongoDB `createdAt`. */
    val createdAt: String = ""
) {
    val eventIdString: String
        get() = when (eventId) {
            is String -> eventId
            is Map<*, *> -> (eventId["_id"] as? String) ?: ""
            else -> eventId?.toString() ?: ""
        }

    val eventTitle: String
        get() = when (eventId) {
            is Map<*, *> -> (eventId["title"] as? String) ?: ""
            else -> ""
        }

    val eventDate: String
        get() = when (eventId) {
            is Map<*, *> -> (eventId["date"] as? String) ?: ""
            else -> ""
        }

    val eventLocation: String
        get() = when (eventId) {
            is Map<*, *> -> (eventId["location"] as? String) ?: ""
            else -> ""
        }
}
