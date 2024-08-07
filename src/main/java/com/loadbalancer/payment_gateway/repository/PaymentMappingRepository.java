package com.loadbalancer.payment_gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.entity.PaymentMapping;

@Repository
public interface PaymentMappingRepository extends JpaRepository<PaymentMapping, Long> {
    List<PaymentMapping> findByAmcIdAndPaymentMethodId(Long amcId, Long paymentMethodId);

    List<PaymentMapping> findByGatewayId(Long gatewayId);
}
