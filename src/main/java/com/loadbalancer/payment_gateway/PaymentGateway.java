package com.loadbalancer.payment_gateway;

public interface PaymentGateway {

    boolean processPayment(PaymentRequest request);

    boolean supportsBank(String bank);
}
