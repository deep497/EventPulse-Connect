package com.example.eventpulse.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val date: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val imageUrl: String = "",
    val organizerId: String = "",
    val createdAt: String = ""
)
