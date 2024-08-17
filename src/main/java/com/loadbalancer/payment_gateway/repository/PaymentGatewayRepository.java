package com.loadbalancer.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loadbalancer.payment_gateway.entity.PaymentGateway;

public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {
}
