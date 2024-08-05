package com.loadbalancer.payment_gateway;

import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.model.PaymentRequest;

public interface IPaymentGateway {

    boolean processPayment(PaymentRequest request);

    boolean supportsBank(Bank bank);
}
