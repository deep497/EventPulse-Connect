package com.example.eventpulse.ui.checkout

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.components.GradientButton
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.theme.*
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavHostController,
    eventId: String,
    initialQuantity: Int,
    viewModel: CheckoutViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = LocalThemeIsDark.current

    // Initialize Google Pay Test Launcher
    // TEST mode only (WalletConstants.ENVIRONMENT_TEST) - No real money is charged.
    val googlePayLauncher = remember { GooglePayTestLauncher(context) }

    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val paymentData = PaymentData.getFromIntent(result.data!!)
            if (paymentData != null) {
                val parsed = googlePayLauncher.parsePaymentResult(paymentData)
                if (parsed is PaymentResult.Success) {
                    viewModel.confirmGooglePaySuccess(parsed.gatewayToken)
                } else if (parsed is PaymentResult.Failure) {
                    viewModel.processPayment(mockSuccess = false)
                }
            }
        } else if (result.resultCode == AutoResolveHelper.RESULT_ERROR) {
            viewModel.processPayment(mockSuccess = false)
        }
    }

    LaunchedEffect(eventId) {
        viewModel.initEvent(eventId, initialQuantity)
        googlePayLauncher.isAvailable { isReady ->
            viewModel.setGooglePayReady(isReady)
        }
    }

    Scaffold(
        containerColor = if (isDark) UserDarkBackgroundStart else UserLightBackgroundStart,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.checkoutStep) {
                            CheckoutStep.ORDER_SUMMARY -> "Checkout"
                            CheckoutStep.PROCESSING -> "Processing..."
                            CheckoutStep.PAYMENT_SUCCESS -> "Booking Confirmed"
                            CheckoutStep.PAYMENT_FAILURE -> "Payment Failed"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )
                },
                navigationIcon = {
                    if (uiState.checkoutStep == CheckoutStep.ORDER_SUMMARY) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                                contentDescription = "Back",
                                tint = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.checkoutStep) {
                CheckoutStep.ORDER_SUMMARY -> {
                    OrderSummaryContent(
                        uiState = uiState,
                        isDark = isDark,
                        onQuantityChanged = { viewModel.updateQuantity(it) },
                        onGooglePayClick = {
                            viewModel.processPayment(useGooglePay = true) { amount ->
                                val activity = context as? Activity
                                if (activity != null) {
                                    val task = googlePayLauncher.createPaymentDataTask(amount, uiState.event?.title ?: "Event Ticket")
                                    AutoResolveHelper.resolveTask(
                                        task,
                                        activity,
                                        GooglePayTestLauncher.LOAD_PAYMENT_DATA_REQUEST_CODE
                                    )
                                }
                            }
                        },
                        onSimulateSuccess = {
                            viewModel.processPayment(useGooglePay = false, mockSuccess = true)
                        },
                        onSimulateFailure = {
                            viewModel.processPayment(useGooglePay = false, mockSuccess = false)
                        }
                    )
                }
                CheckoutStep.PROCESSING -> {
                    ProcessingContent(isDark = isDark)
                }
                CheckoutStep.PAYMENT_SUCCESS -> {
                    PaymentSuccessContent(
                        uiState = uiState,
                        isDark = isDark,
                        onViewTickets = {
                            navController.navigate(Screen.UserTickets.route) {
                                popUpTo(Screen.UserExplore.route)
                            }
                        }
                    )
                }
                CheckoutStep.PAYMENT_FAILURE -> {
                    PaymentFailureContent(
                        uiState = uiState,
                        isDark = isDark,
                        onRetry = { viewModel.retryCheckout() },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryContent(
    uiState: CheckoutUiState,
    isDark: Boolean,
    onQuantityChanged: (Int) -> Unit,
    onGooglePayClick: () -> Unit,
    onSimulateSuccess: () -> Unit,
    onSimulateFailure: () -> Unit
) {
    val event = uiState.event
    val scrollState = rememberScrollState()

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CoralPrimary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Event Summary Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_today),
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.date} • ${event.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = if (isDark) UserDarkBorder else UserLightBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Quantity Stepper (Strictly capped at availableSeats)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ticket Quantity",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                        )
                        Text(
                            text = "${event.availableSeats} seats remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledIconButton(
                            onClick = { onQuantityChanged(uiState.quantity - 1) },
                            enabled = uiState.quantity > 1 && !uiState.isProcessing,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isDark) UserDarkCard else Color(0xFFFFECE5),
                                contentColor = CoralPrimary
                            )
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "${uiState.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                        )

                        FilledIconButton(
                            onClick = { onQuantityChanged(uiState.quantity + 1) },
                            enabled = uiState.quantity < event.availableSeats && !uiState.isProcessing,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isDark) UserDarkCard else Color(0xFFFFECE5),
                                contentColor = CoralPrimary
                            )
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Price Breakdown Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Payment Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ticket Price (${uiState.quantity}x ₹${event.price.toInt()})",
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                    Text(
                        text = if (uiState.isFree) "Free" else "₹${uiState.totalPrice.toInt()}",
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Platform / Booking Fee",
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                    Text(
                        text = "₹0 (Waived)",
                        color = StatusPublished,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = if (isDark) UserDarkBorder else UserLightBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Payable",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )
                    Text(
                        text = if (uiState.isFree) "₹0 (Free)" else "₹${uiState.totalPrice.toInt()} INR",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = CoralPrimary
                    )
                }
            }
        }

        // Payment Execution Section
        if (uiState.isFree) {
            GradientButton(
                text = "Confirm Free Booking",
                onClick = onSimulateSuccess,
                enabled = !uiState.isProcessing,
                isLoading = uiState.isProcessing,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Google Pay TEST button (Shown when Google Pay is ready)
            if (uiState.isGooglePayReady) {
                Button(
                    onClick = onGooglePayClick,
                    enabled = !uiState.isProcessing,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Buy with ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "GPay",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(TEST MODE)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFB703)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "  or test with mock simulator  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) UserDarkTextTertiary else UserLightTextTertiary
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            // Mock Checkout Simulator Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusPublished)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mock Payment Simulator (Safe Testing)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                        )
                    }

                    Text(
                        text = "No actual money is charged. Choose an outcome to verify the full flow:",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Simulate Success Button
                    GradientButton(
                        text = "Simulate Pay Success (₹${uiState.totalPrice.toInt()})",
                        onClick = onSimulateSuccess,
                        enabled = !uiState.isProcessing,
                        isLoading = uiState.isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simulate Failure Button
                    OutlinedButton(
                        onClick = onSimulateFailure,
                        enabled = !uiState.isProcessing,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRejected),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejected),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Simulate Pay Failure",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ProcessingContent(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = CoralPrimary,
                modifier = Modifier.size(54.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = "Processing Your Order...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
            )
            Text(
                text = "Holding your seats and confirming payment status with provider.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
            )
        }
    }
}

@Composable
private fun PaymentSuccessContent(
    uiState: CheckoutUiState,
    isDark: Boolean,
    onViewTickets: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(StatusPublishedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.checkbox_on_background),
                        contentDescription = "Success",
                        tint = StatusPublished,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Booking Confirmed!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                )

                Text(
                    text = uiState.resultMessage ?: "Your payment was verified and ticket has been generated.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                )

                uiState.confirmedBooking?.let { b ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x3326201D) else Color(0xFFF9F5F1))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Booking ID: ${b.id}\nTickets: ${b.ticketCount} • Total: ₹${b.totalPrice.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                GradientButton(
                    text = "View My Tickets",
                    onClick = onViewTickets,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PaymentFailureContent(
    uiState: CheckoutUiState,
    isDark: Boolean,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(StatusRejectedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_delete),
                        contentDescription = "Failed",
                        tint = StatusRejected,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Payment Not Completed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                )

                Text(
                    text = uiState.resultMessage ?: "The transaction failed or was cancelled. Any held seats have been released.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                GradientButton(
                    text = "Try Again",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Event")
                }
            }
        }
    }
}
