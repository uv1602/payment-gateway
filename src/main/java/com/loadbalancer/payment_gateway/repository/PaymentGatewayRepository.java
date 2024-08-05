package com.loadbalancer.payment_gateway.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;

@Repository
public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {
    Set<Bank> findSupportedBanksById(Long gatewayId);
}
