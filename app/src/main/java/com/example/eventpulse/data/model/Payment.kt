package com.example.eventpulse.data.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    val eventId: String,
    val quantity: Int
)

data class OrderResponse(
    val paymentId: String = "",
    val bookingId: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val status: String = "PENDING",
    val expiresAt: String? = null,
    val message: String? = null
)

data class ConfirmPaymentRequest(
    val paymentId: String,
    val result: String, // "SUCCESS" | "FAILURE"
    val gatewayToken: String? = null
)

data class ConfirmPaymentResponse(
    val message: String = "",
    val booking: Booking? = null,
    val payment: PaymentInfo? = null
)

data class PaymentInfo(
    @SerializedName("_id") val id: String = "",
    val bookingId: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val status: String = "",
    val gatewayToken: String = "",
    val providerOrderId: String = "",
    val providerTransactionId: String = ""
)
