package com.loadbalancer.payment_gateway.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.entity.GatewayWeight;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.repository.BankRepository;
import com.loadbalancer.payment_gateway.repository.GatewayWeightRepository;
import com.loadbalancer.payment_gateway.repository.PaymentGatewayRepository;

@Service
public class PaymentGatewayService {

    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private GatewayWeightRepository gatewayWeightRepository;

    public Set<Bank> getSupportedBanksForGateway(Long gatewayId) {
        PaymentGateway gateway = paymentGatewayRepository.findById(gatewayId)
                .orElseThrow(() -> new RuntimeException("Gateway not found"));
        return gateway.getSupportedBanks();
    }

    public boolean isBankSupportedByGateway(Long gatewayId, String bankName) {
        Optional<Bank> bankOpt = bankRepository.findByName(bankName);
        if (bankOpt.isEmpty()) {
            throw new RuntimeException("Bank not found");
        }
        Set<Bank> supportedBanks = getSupportedBanksForGateway(gatewayId);
        return supportedBanks.contains(bankOpt.get());
    }

    public void updateWeight(Long gatewayId, Integer newWeight) {
        Optional<GatewayWeight> gatewayWeightOpt = gatewayWeightRepository.findById(gatewayId);
        if (gatewayWeightOpt.isPresent()) {
            GatewayWeight gatewayWeight = gatewayWeightOpt.get();
            gatewayWeight.setWeight(newWeight);
            gatewayWeightRepository.save(gatewayWeight);
        } else {
            throw new RuntimeException("GatewayWeight not found for ID: " + gatewayId);
        }
    }

    public void updateWeights(Map<Long, Integer> weights) {
        System.out.println(weights);
        weights.forEach((gatewayId, newWeight) -> {
            Optional<GatewayWeight> gatewayWeightOpt = gatewayWeightRepository.findById(gatewayId);
            if (gatewayWeightOpt.isPresent()) {
                GatewayWeight gatewayWeight = gatewayWeightOpt.get();
                gatewayWeight.setWeight(newWeight);
                gatewayWeightRepository.save(gatewayWeight);
            } else {
                throw new RuntimeException("GatewayWeight not found for ID: " + gatewayId);
            }
        });
    }
}
