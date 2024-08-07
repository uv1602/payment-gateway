package com.loadbalancer.payment_gateway;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.repository.BankRepository;
import com.loadbalancer.payment_gateway.repository.PaymentMappingRepository;

import lombok.Data;

@Data
public class PaymentProvider implements IPaymentGateway {

    private long id;
    private String name;
    private Set<Bank> supportedBanks;

    @Autowired
    private PaymentMappingRepository paymentMappingRepository;

    @Autowired
    private BankRepository bankRepository;

    public PaymentProvider(Long paymentGatewayId) {
        this.id = paymentGatewayId;
        // this.name = paymentGateway.getName();

    }

    @Override
    public boolean processPayment(PaymentRequest request) {
        return true;
    }

    @Override
    public boolean supportsBank(Bank bank) {
        return true;
    }
}
