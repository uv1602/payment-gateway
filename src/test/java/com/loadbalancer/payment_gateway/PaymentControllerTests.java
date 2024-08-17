package com.loadbalancer.payment_gateway;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.service.LoadBalancerService;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private LoadBalancerService loadBalancerService;

        @AfterEach
        public void printLoadDistribution() {
                System.out.println("Load Distribution:");
                Map<PaymentGateway, Integer> gatewayUsage = loadBalancerService.getGatewayUsage();
                for (Map.Entry<PaymentGateway, Integer> entry : gatewayUsage.entrySet()) {
                        PaymentGateway key = entry.getKey();
                        Integer value = entry.getValue();
                        System.out.println(key.getName() + ": " + value);
                }
                System.out.println(gatewayUsage);
        }

        @Test
        public void testDistributeSuccessfulPayments() throws Exception {
                CompletableFuture<?>[] futures = IntStream.range(0, 50)
                                .mapToObj(i -> processPaymentAsync(1L, 1L, 100.0, "USD", "card_abc123",
                                                status().isOk()))
                                .toArray(CompletableFuture[]::new);

                CompletableFuture.allOf(futures).join();
        }

        private CompletableFuture<Void> processPaymentAsync(Long amcId, Long paymentMethodId, Double amount,
                        String currency, String source, ResultMatcher expectedStatus) {
                try {
                        String jsonPayload = String.format(
                                        "{\"amcId\": %d, \"paymentMethodId\": %d, \"amount\": %.1f, \"currency\": \"%s\", \"source\": \"%s\"}",
                                        amcId, paymentMethodId, amount, currency, source);

                        mockMvc.perform(post("/distribute")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonPayload));
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return CompletableFuture.completedFuture(null);
        }
}
