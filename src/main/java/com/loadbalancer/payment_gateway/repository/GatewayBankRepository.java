package com.loadbalancer.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.dto.GatewayBankId;
import com.loadbalancer.payment_gateway.entity.GatewayBank;

@Repository
public interface GatewayBankRepository extends JpaRepository<GatewayBank, GatewayBankId> {

    // @Query("SELECT gb.id.bankId FROM GatewayBank gb WHERE gb.id.gatewayId =
    // :gatewayId")
    // List<Long> findBankIdsByGatewayId(@Param("gatewayId") Long gatewayId);
}
