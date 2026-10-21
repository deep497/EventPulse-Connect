package com.example.eventpulse.ui.checkout

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

sealed class PaymentResult {
    data class Success(val gatewayToken: String, val transactionId: String? = null) : PaymentResult()
    data class Failure(val errorMessage: String) : PaymentResult()
    object Cancelled : PaymentResult()
}

/**
 * Abstraction for payment execution.
 */
interface PaymentLauncher {
    fun isAvailable(callback: (Boolean) -> Unit)
}

/**
 * Google Pay implementation using WalletConstants.ENVIRONMENT_TEST.
 *
 * // TEST mode only (WalletConstants.ENVIRONMENT_TEST) - No real money is charged.
 */
class GooglePayTestLauncher(
    private val context: Context
) : PaymentLauncher {

    companion object {
        const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }

    // TEST mode only (WalletConstants.ENVIRONMENT_TEST) - No real money is charged.
    private val paymentsClient: PaymentsClient by lazy {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
        Wallet.getPaymentsClient(context, walletOptions)
    }

    private val baseCardPaymentMethod: JSONObject by lazy {
        JSONObject()
            .put("type", "CARD")
            .put(
                "parameters",
                JSONObject()
                    .put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
                    .put("allowedCardNetworks", JSONArray(listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA")))
            )
    }

    private val googlePayBaseConfiguration: JSONObject by lazy {
        JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0)
            .put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod))
    }

    override fun isAvailable(callback: (Boolean) -> Unit) {
        val isReadyToPayRequest = IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString())
        paymentsClient.isReadyToPay(isReadyToPayRequest)
            .addOnCompleteListener { task ->
                try {
                    val result = task.getResult(ApiException::class.java)
                    callback(result == true)
                } catch (e: Exception) {
                    callback(false)
                }
            }
    }

    /**
     * Creates the Google Pay PaymentDataRequest for the specified INR amount.
     */
    fun createPaymentDataTask(amountInInr: Double, description: String): Task<PaymentData> {
        val tokenizationSpecification = JSONObject()
            .put("type", "PAYMENT_GATEWAY")
            .put(
                "parameters",
                JSONObject()
                    .put("gateway", "example")
                    .put("gatewayMerchantId", "exampleGatewayMerchantId")
            )

        val cardPaymentMethod = JSONObject(baseCardPaymentMethod.toString())
            .put("tokenizationSpecification", tokenizationSpecification)

        val transactionInfo = JSONObject()
            .put("totalPrice", String.format(java.util.Locale.US, "%.2f", amountInInr))
            .put("totalPriceStatus", "FINAL")
            .put("currencyCode", "INR")

        val merchantInfo = JSONObject()
            .put("merchantName", "EventPulse Test Merchant")

        val paymentDataRequestJson = JSONObject(googlePayBaseConfiguration.toString())
            .put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
            .put("transactionInfo", transactionInfo)
            .put("merchantInfo", merchantInfo)

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        return paymentsClient.loadPaymentData(request)
    }

    /**
     * Resolves the PaymentData task with the current Activity.
     */
    fun launchPayment(activity: Activity, amountInInr: Double, description: String) {
        val task = createPaymentDataTask(amountInInr, description)
        AutoResolveHelper.resolveTask(task, activity, LOAD_PAYMENT_DATA_REQUEST_CODE)
    }

    /**
     * Extracts token from completed Google Pay PaymentData.
     */
    fun parsePaymentResult(paymentData: PaymentData): PaymentResult {
        return try {
            val paymentInformation = paymentData.toJson()
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val token = paymentMethodData.getJSONObject("tokenizationData").getString("token")
            PaymentResult.Success(gatewayToken = token)
        } catch (e: Exception) {
            PaymentResult.Failure("Failed to parse Google Pay response: ${e.message}")
        }
    }
}
