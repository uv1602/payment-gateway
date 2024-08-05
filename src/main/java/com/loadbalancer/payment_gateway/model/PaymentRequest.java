package com.loadbalancer.payment_gateway.model;

import lombok.Data;

@Data
public class PaymentRequest {

    private String bank;
    private String currency;
    private double amount;
    private String accountNumber;
    private String paymentMethod;
    private String customerName;
    private String amc;

    public PaymentRequest() {
    }

    public PaymentRequest(String bank, String currency, double amount, String accountNumber, String paymentMethod,
            String customerName, String amc) {
        this.bank = bank;
        this.currency = currency;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.paymentMethod = paymentMethod;
        this.customerName = customerName;
        this.amc = amc;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "bank='" + bank + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", accountNumber='" + accountNumber + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", customerName='" + customerName + '\'' +
                ", amc='" + amc + '\'' +
                '}';
    }
}
