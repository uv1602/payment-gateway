package com.loadbalancer.payment_gateway;

import java.util.Set;

public class PaymentGatewayA implements PaymentGateway {
    private Set<String> supportedBanks = Set.of("BankD", "BankE");

    @Override
    public String toString() {
        return "PaymentGateway1";
    }

    @Override
    public boolean processPayment(PaymentRequest request) {
        return true;
    }

    @Override
    public boolean supportsBank(String bank) {
        return supportedBanks.contains(bank);
    }
}
