package com.example.eventpulse.ui.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.model.OrderResponse
import com.example.eventpulse.data.repository.BookingRepository
import com.example.eventpulse.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CheckoutStep {
    ORDER_SUMMARY,
    PROCESSING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILURE
}

data class CheckoutUiState(
    val event: Event? = null,
    val quantity: Int = 1,
    val isProcessing: Boolean = false,
    val isGooglePayReady: Boolean = false,
    val checkoutStep: CheckoutStep = CheckoutStep.ORDER_SUMMARY,
    val orderResponse: OrderResponse? = null,
    val confirmedBooking: Booking? = null,
    val errorMessage: String? = null,
    val resultMessage: String? = null
) {
    val unitPrice: Double
        get() = event?.price ?: 0.0

    val totalPrice: Double
        get() = unitPrice * quantity

    val isFree: Boolean
        get() = unitPrice == 0.0
}

class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    companion object {
        private const val KEY_PENDING_BOOKING_ID = "pendingBookingId"
        private const val KEY_PENDING_PAYMENT_ID = "pendingPaymentId"
        private const val KEY_EVENT_ID = "eventId"
        private const val KEY_QUANTITY = "quantity"
    }

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun setGooglePayReady(ready: Boolean) {
        _uiState.update { it.copy(isGooglePayReady = ready) }
    }

    fun initEvent(eventId: String, initialQuantity: Int = 1) {
        val savedEventId: String? = savedStateHandle[KEY_EVENT_ID]
        val savedQuantity: Int? = savedStateHandle[KEY_QUANTITY]

        val targetEventId = savedEventId ?: eventId
        val targetQuantity = savedQuantity ?: initialQuantity

        savedStateHandle[KEY_EVENT_ID] = targetEventId
        savedStateHandle[KEY_QUANTITY] = targetQuantity

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = eventRepository.getEventById(targetEventId)
            result.fold(
                onSuccess = { event ->
                    val clampedQty = targetQuantity.coerceIn(1, maxOf(1, event.availableSeats))
                    savedStateHandle[KEY_QUANTITY] = clampedQty
                    _uiState.update {
                        it.copy(
                            event = event,
                            quantity = clampedQty,
                            isProcessing = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = err.message ?: "Failed to load event details"
                        )
                    }
                }
            )
        }
    }

    fun updateQuantity(newQuantity: Int) {
        val event = _uiState.value.event ?: return
        val clamped = newQuantity.coerceIn(1, maxOf(1, event.availableSeats))
        savedStateHandle[KEY_QUANTITY] = clamped
        _uiState.update { it.copy(quantity = clamped) }
    }

    /**
     * Immediately disables the Pay button and prepares the order.
     */
    fun processPayment(useGooglePay: Boolean = false, mockSuccess: Boolean = true, onLaunchGooglePay: ((Double) -> Unit)? = null) {
        val event = _uiState.value.event ?: return
        val qty = _uiState.value.quantity

        // Double-tap prevention guard
        if (_uiState.value.isProcessing) return
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

        viewModelScope.launch {
            // Check if we already have a pending payment in SavedStateHandle
            var paymentId: String? = savedStateHandle[KEY_PENDING_PAYMENT_ID]
            var bookingId: String? = savedStateHandle[KEY_PENDING_BOOKING_ID]

            if (paymentId == null || bookingId == null) {
                val orderResult = bookingRepository.createPaymentOrder(event.id, qty)
                if (orderResult.isFailure) {
                    val err = orderResult.exceptionOrNull()?.message ?: "Failed to create order"
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            checkoutStep = CheckoutStep.PAYMENT_FAILURE,
                            resultMessage = err
                        )
                    }
                    return@launch
                }

                val order = orderResult.getOrThrow()
                paymentId = order.paymentId
                bookingId = order.bookingId
                savedStateHandle[KEY_PENDING_PAYMENT_ID] = paymentId
                savedStateHandle[KEY_PENDING_BOOKING_ID] = bookingId
                _uiState.update { it.copy(orderResponse = order) }

                // If event is free, it is auto-confirmed by backend
                if (order.status == "CONFIRMED" || _uiState.value.isFree) {
                    val booking = bookingRepository.getBookingById(bookingId).getOrNull()
                    clearPendingState()
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            checkoutStep = CheckoutStep.PAYMENT_SUCCESS,
                            confirmedBooking = booking,
                            resultMessage = "Free registration confirmed successfully!"
                        )
                    }
                    return@launch
                }
            }

            // If user selected Google Pay and it's available, trigger native Google Pay sheet
            if (useGooglePay && onLaunchGooglePay != null) {
                _uiState.update { it.copy(isProcessing = false) }
                onLaunchGooglePay(_uiState.value.totalPrice)
                return@launch
            }

            // Otherwise, execute Mock checkout flow
            val result = if (mockSuccess) "SUCCESS" else "FAILURE"
            executeConfirmation(paymentId, result, gatewayToken = "mock_token")
        }
    }

    /**
     * Completes confirmation after Google Pay or Mock checkout.
     */
    fun confirmGooglePaySuccess(gatewayToken: String) {
        val paymentId: String? = savedStateHandle[KEY_PENDING_PAYMENT_ID]
        if (paymentId == null) {
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    checkoutStep = CheckoutStep.PAYMENT_FAILURE,
                    resultMessage = "Missing pending order details"
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            executeConfirmation(paymentId, "SUCCESS", gatewayToken)
        }
    }

    private suspend fun executeConfirmation(paymentId: String, result: String, gatewayToken: String?) {
        val confirmResult = bookingRepository.confirmPayment(
            paymentId = paymentId,
            result = result,
            gatewayToken = gatewayToken
        )

        confirmResult.fold(
            onSuccess = { response ->
                val booking = response.booking
                clearPendingState()
                if (result == "SUCCESS") {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            checkoutStep = CheckoutStep.PAYMENT_SUCCESS,
                            confirmedBooking = booking,
                            resultMessage = response.message.ifBlank { "Payment successful! Your ticket is confirmed." }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            checkoutStep = CheckoutStep.PAYMENT_FAILURE,
                            resultMessage = "Payment was not successful. Reserved seats have been released."
                        )
                    }
                }
            },
            onFailure = { err ->
                clearPendingState()
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        checkoutStep = CheckoutStep.PAYMENT_FAILURE,
                        resultMessage = err.message ?: "Payment confirmation failed"
                    )
                }
            }
        )
    }

    fun retryCheckout() {
        clearPendingState()
        _uiState.update {
            it.copy(
                checkoutStep = CheckoutStep.ORDER_SUMMARY,
                isProcessing = false,
                errorMessage = null,
                resultMessage = null
            )
        }
    }

    private fun clearPendingState() {
        savedStateHandle.remove<String>(KEY_PENDING_BOOKING_ID)
        savedStateHandle.remove<String>(KEY_PENDING_PAYMENT_ID)
    }
}
