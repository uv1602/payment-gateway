package com.loadbalancer.payment_gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "gateway_amcs")
@Data
public class GatewayAmc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gateway_id")
    private Long gatewayId;

    @Column(name = "amc_id")
    private Long amcId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;
}
