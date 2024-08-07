package com.loadbalancer.payment_gateway.model;

import lombok.Data;

@Data
public class PaymentRequest {

    private long bankId;
    private String currency;
    private double amount;
    private String accountNumber;
    private long paymentMethodId;
    private String customerName;
    private long amcId;

    public PaymentRequest() {
    }

    public PaymentRequest(long bankId, String currency, double amount, String accountNumber, long paymentMethodId,
            String customerName, long amcId) {
        this.bankId = bankId;
        this.currency = currency;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.paymentMethodId = paymentMethodId;
        this.customerName = customerName;
        this.amcId = amcId;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "bank='" + bankId + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", accountNumber='" + accountNumber + '\'' +
                ", paymentMethod='" + paymentMethodId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", amc='" + amcId + '\'' +
                '}';
    }
}
