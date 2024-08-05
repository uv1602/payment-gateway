package com.loadbalancer.payment_gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.entity.GatewayBank;

@Repository
public interface GatewayBankRepository extends JpaRepository<GatewayBank, Long> {
    List<GatewayBank> findByBankId(Long bankId);
}
