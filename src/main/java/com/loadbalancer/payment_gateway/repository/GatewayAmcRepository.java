package com.loadbalancer.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.entity.GatewayAmc;

@Repository
public interface GatewayAmcRepository extends JpaRepository<GatewayAmc, Long> {

    boolean existsByGatewayIdAndAmcId(Long gatewayId, Long amcId);
}
