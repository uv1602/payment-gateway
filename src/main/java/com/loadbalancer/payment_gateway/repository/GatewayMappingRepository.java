package com.loadbalancer.payment_gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loadbalancer.payment_gateway.entity.GatewayMapping;

public interface GatewayMappingRepository extends JpaRepository<GatewayMapping, Long> {

    @Query("SELECT gm FROM GatewayMapping gm WHERE gm.amc.id = :amcId AND gm.paymentMethod.id = :paymentMethodId")
    List<GatewayMapping> findByAMCAndPaymentMethod(@Param("amcId") Long amcId,
            @Param("paymentMethodId") Long paymentMethodId);
}
