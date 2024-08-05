package com.loadbalancer.payment_gateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.service.PaymentGatewayService;
import com.loadbalancer.payment_gateway.service.WeightedLoadBalancerService;

@RestController
public class PaymentController {

    @Autowired
    private WeightedLoadBalancerService loadBalancerService;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @PostMapping("/api/v1/payments")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest request) {
        try {
            boolean result = loadBalancerService.processPayment(request);
            if (result) {
            } else {
                throw new RuntimeException();
            }
            return ResponseEntity.ok("Payment Processed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/loadbalancer/update-weights")
    public void updateWeights(@RequestBody Map<Long, Integer> weights) {
        paymentGatewayService.updateWeights(weights);
    }
}
