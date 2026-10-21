package com.example.eventpulse.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id", alternate = ["id"]) val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role = Role.USER,
    val phoneNumber: String = "",
    val isBlocked: Boolean = false,
    val createdAt: String = ""
)
