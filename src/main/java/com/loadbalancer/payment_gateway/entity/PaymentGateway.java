package com.loadbalancer.payment_gateway.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payment_gateways")
@Data
public class PaymentGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @ManyToMany
    @JoinTable(name = "gateway_banks", joinColumns = @JoinColumn(name = "gateway_id"), inverseJoinColumns = @JoinColumn(name = "bank_id"))
    private Set<Bank> banks = new HashSet<>();
}
