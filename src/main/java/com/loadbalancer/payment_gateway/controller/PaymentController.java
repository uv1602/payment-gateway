package com.loadbalancer.payment_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.service.WeightedLoadBalancerService;

@RestController
public class PaymentController {

    @Autowired
    private WeightedLoadBalancerService loadBalancerService;

    @PostMapping("/api/v1/payments")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest request) {
        try {
            // System.out.println(request);
            boolean result = loadBalancerService.processPayment(request);
            if (!result) {
                System.out.println("Payment Failed");
                throw new RuntimeException();
            }
            return ResponseEntity.ok("Payment Processed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
