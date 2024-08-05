package com.loadbalancer.payment_gateway.dto;

import lombok.Data;

@Data
public class WeightUpdateRequest {
    private Long gatewayId;
    private Integer newWeight;
}
