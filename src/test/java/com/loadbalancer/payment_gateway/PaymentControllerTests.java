package com.loadbalancer.payment_gateway;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.service.WeightedLoadBalancerService;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private WeightedLoadBalancerService loadBalancerService;

        @Autowired
        private ObjectMapper objectMapper;

        @AfterEach
        public void printLoadDistribution() {
                System.out.println("Load Distribution:");
                loadBalancerService.getGatewayUsage().forEach(
                                (gateway, count) -> System.out
                                                .println(gateway.getId() + " handled " + count + " requests"));
        }

        @Test
        public void test1000ConcurrentPayments() throws Exception {
                CompletableFuture<?>[] futures = IntStream.range(0, 1000) // Adjust the range as needed
                                .mapToObj(i -> processPaymentAsync(
                                                4L,
                                                4L,
                                                4L,
                                                "No available payment provider supports the bank: ",
                                                status().isBadRequest(),
                                                "John Doe"))
                                .toArray(CompletableFuture[]::new);

                // Wait until all requests are completed
                CompletableFuture.allOf(futures).join();

                CompletableFuture<?>[] futures1 = IntStream.range(0, 1000) // Adjust the range as needed
                                .mapToObj(i -> processPaymentAsync(
                                                5L,
                                                5L,
                                                5L,
                                                "",
                                                status().isBadRequest(),
                                                "John Doe"))
                                .toArray(CompletableFuture[]::new);

                // Wait until all requests are completed
                CompletableFuture.allOf(futures1).join();
        }

        @Async
        public CompletableFuture<Void> processPaymentAsync(long bankId, long paymentMethodId, long amcId,
                        String expectedMessage, ResultMatcher expectedStatus, String customerName) {
                try {
                        PaymentRequest paymentRequest = new PaymentRequest(bankId, "USD", 100.0,
                                        "123456789", paymentMethodId, customerName, amcId);
                        String requestJson = objectMapper.writeValueAsString(paymentRequest);

                        mockMvc.perform(post("/api/v1/payments")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andExpect(expectedStatus)
                                        .andExpect(content().string(expectedMessage));
                } catch (Exception e) {
                        e.printStackTrace();
                        Assertions.fail("Test failed due to exception: " + e.getMessage());
                }
                return CompletableFuture.completedFuture(null);
        }
}
