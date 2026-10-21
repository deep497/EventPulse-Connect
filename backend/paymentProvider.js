/**
 * PaymentProvider interface and implementations.
 * Allows switching between Mock, Google Pay token verification,
 * or future gateways (e.g. Stripe) without touching booking controller logic.
 */

class PaymentProvider {
  /**
   * Creates an order with the payment provider.
   * @param {{ amount: number, currency: string, receipt: string, metadata?: object }} options
   * @returns {Promise<{ orderId: string, amount: number, currency: string }>}
   */
  async createOrder(options) {
    throw new Error('createOrder() must be implemented by payment provider');
  }

  /**
   * Verifies a completed payment transaction.
   * @param {{ paymentId: string, gatewayToken?: string, result?: string }} options
   * @returns {Promise<{ success: boolean, transactionId?: string, error?: string }>}
   */
  async verifyPayment(options) {
    throw new Error('verifyPayment() must be implemented by payment provider');
  }

  /**
   * Processes a refund.
   * @param {{ paymentId: string, amount: number, reason?: string }} options
   * @returns {Promise<{ success: boolean, refundId?: string, error?: string }>}
   */
  async refundPayment(options) {
    throw new Error('refundPayment() must be implemented by payment provider');
  }
}

class MockPaymentProvider extends PaymentProvider {
  constructor() {
    super();
    console.log('[PaymentProvider] Initialized MockPaymentProvider (PAYMENT_MODE=mock). No real money is collected.');
  }

  async createOrder({ amount, currency = 'INR', receipt, metadata = {} }) {
    const orderId = `mock_ord_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    return {
      orderId,
      amount,
      currency,
      receipt,
      metadata,
    };
  }

  async verifyPayment({ paymentId, gatewayToken, result }) {
    // If client explicitly sent FAILURE or failed token, reject
    if (result === 'FAILURE') {
      return {
        success: false,
        error: 'Simulated payment failure (Mock Provider / Test Card Declined)',
      };
    }

    // Google Pay ENVIRONMENT_TEST token or mock simulator success
    const transactionId = `mock_tx_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
    return {
      success: true,
      transactionId,
      gatewayToken: gatewayToken || 'mock_token',
    };
  }

  async refundPayment({ paymentId, amount, reason = 'Customer cancellation' }) {
    const refundId = `mock_rf_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
    return {
      success: true,
      refundId,
      amount,
      reason,
    };
  }
}

/**
 * Factory function to retrieve the configured payment provider.
 */
function getPaymentProvider(mode = process.env.PAYMENT_MODE || 'mock') {
  switch (mode.toLowerCase()) {
    case 'mock':
    default:
      return new MockPaymentProvider();
  }
}

module.exports = {
  PaymentProvider,
  MockPaymentProvider,
  getPaymentProvider,
};
