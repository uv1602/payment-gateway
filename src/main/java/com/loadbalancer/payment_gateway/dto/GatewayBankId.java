package com.loadbalancer.payment_gateway.dto;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class GatewayBankId implements Serializable {

    @Column(name = "gateway_id")
    private Long gatewayId;

    @Column(name = "bank_id")
    private Long bankId;
}