package com.loadbalancer.payment_gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gateway_weights")
@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GatewayWeight {

    @Id
    @Column(name = "gateway_id")
    private Long gatewayId;

    @Column(name = "weight")
    private Integer weight;
}
