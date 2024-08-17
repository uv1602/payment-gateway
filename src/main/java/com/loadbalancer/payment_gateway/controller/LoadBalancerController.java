package com.loadbalancer.payment_gateway.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.loadbalancer.payment_gateway.dto.PaymentRequest;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.service.LoadBalancerService;

@RestController
public class LoadBalancerController {

    private final LoadBalancerService loadBalancerService;

    @Autowired
    public LoadBalancerController(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    @PostMapping("/distribute")
    public ResponseEntity<?> distribute(@RequestBody PaymentRequest paymentRequest)
            throws InterruptedException, IOException {

        try {
            long amcId = paymentRequest.getAmcId();
            long paymentMethodId = paymentRequest.getPaymentMethodId();
            double amount = paymentRequest.getAmount();
            String currency = paymentRequest.getCurrency();
            String source = paymentRequest.getSource();

            PaymentGateway bestPaymentGateway = loadBalancerService.getBestGateway(amcId, paymentMethodId);

            if (bestPaymentGateway == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No suitable payment gateway found");
            }

            return ResponseEntity
                    .ok(loadBalancerService.makePaymentRequest(bestPaymentGateway, amount, currency, source));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No payment failed");
        }

    }

}
