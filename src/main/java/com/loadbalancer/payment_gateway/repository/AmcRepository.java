package com.loadbalancer.payment_gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadbalancer.payment_gateway.entity.Amc;

@Repository
public interface AmcRepository extends JpaRepository<Amc, Long> {
    Optional<Amc> findByName(String name);
}
