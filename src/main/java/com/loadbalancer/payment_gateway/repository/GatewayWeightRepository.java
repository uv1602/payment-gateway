package com.loadbalancer.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.entity.GatewayWeight;

@Repository
public interface GatewayWeightRepository extends JpaRepository<GatewayWeight, Long> {
}
