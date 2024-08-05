package com.loadbalancer.payment_gateway;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.repository.BankRepository;
import com.loadbalancer.payment_gateway.repository.GatewayBankRepository;

import lombok.Data;

@Data
public class PaymentProvider implements IPaymentGateway {

    private long id;

    private String name;

    private Set<Bank> supportedBanks;

    private List<Bank> banks;

    @Autowired
    private GatewayBankRepository gatewayBankRepository;

    @Autowired
    private BankRepository bankRepository;

    public PaymentProvider(PaymentGateway paymentGateway) {
        this.id = paymentGateway.getId();
        this.name = paymentGateway.getName();
        this.supportedBanks = paymentGateway.getSupportedBanks();
    }

    @Override
    public boolean processPayment(PaymentRequest request) {
        return true;
    }

    @Override
    public boolean supportsBank(Bank bank) {
        return supportedBanks.contains(bank);
    }
}
