package com.loadbalancer.payment_gateway.dto;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long amcId;
    private Long paymentMethodId;
    private Double amount;
    private String currency;
    private String source;

}
