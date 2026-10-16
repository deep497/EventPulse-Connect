package com.example.eventpulse.data.model

data class Booking(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val ticketCount: Int = 0,
    val totalPrice: Double = 0.0,
    val status: String = "",
    val qrCode: String = "",
    val createdAt: String = ""
)
