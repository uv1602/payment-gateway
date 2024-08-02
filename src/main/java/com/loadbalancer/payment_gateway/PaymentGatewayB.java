package com.loadbalancer.payment_gateway;

import java.util.Set;

public class PaymentGatewayB implements PaymentGateway {
    private Set<String> supportedBanks = Set.of("BankB", "BankE");

    @Override
    public String toString() {
        return "PaymentGateway2";
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
