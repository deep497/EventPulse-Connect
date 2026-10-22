package com.example.eventpulse.data.model

data class Attendee(
    val bookingId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val ticketCount: Int = 1,
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val status: String = "CONFIRMED",
    val isCheckedIn: Boolean = false,
    val checkedIn: Boolean = false,
    val isExpired: Boolean = false,
    val checkedInAt: String? = null,
    val bookedAt: String? = null,
    val qrCodeHash: String = ""
)
