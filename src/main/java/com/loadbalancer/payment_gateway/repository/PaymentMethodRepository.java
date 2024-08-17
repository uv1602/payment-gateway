package com.loadbalancer.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loadbalancer.payment_gateway.entity.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}
