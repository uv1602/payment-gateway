package com.loadbalancer.payment_gateway.entity;

import org.springframework.beans.factory.annotation.Autowired;

import com.loadbalancer.payment_gateway.dto.GatewayBankId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gateway_banks")
@Data
@NoArgsConstructor
public class GatewayBank {

    @EmbeddedId
    @Autowired
    private GatewayBankId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("gatewayId")
    @JoinColumn(name = "gateway_id")
    private PaymentGateway gateway;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("bankId")
    @JoinColumn(name = "bank_id")
    private Bank bank;

    public GatewayBank(Long gatewayId, Long bankId) {
        this.id = new GatewayBankId(gatewayId, bankId);
    }
}
