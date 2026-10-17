package com.example.eventpulse.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("_id") val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val date: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val totalSeats: Int = 0,
    val availableSeats: Int = 0,
    val imageUrl: String = "",
    val organizerId: String = "",
    /**
     * Backend status: `PENDING_APPROVAL`, `PUBLISHED`, `REJECTED`, or `CANCELLED`.
     * New events default to `PENDING_APPROVAL`.
     */
    val status: String = "PENDING_APPROVAL",
    val createdAt: String = ""
)
