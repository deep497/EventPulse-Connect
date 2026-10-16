package com.example.eventpulse.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role = Role.USER,
    val createdAt: String = ""
)
