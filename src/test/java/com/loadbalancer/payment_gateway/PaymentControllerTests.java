package com.loadbalancer.payment_gateway;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
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

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private WeightedLoadBalancerService loadBalancerService;

        @AfterEach
        public void printLoadDistribution() {
                System.out.println("Load Distribution:");
                loadBalancerService.getGatewayUsage().forEach(
                                (gateway, count) -> System.out.println(gateway + " handled " + count + " requests"));
        }

        @Test
        public void testMultipleSuccessfulPayments() throws Exception {
                CompletableFuture<?>[] futures = IntStream.range(0, 1000)
                                .mapToObj((IntFunction<CompletableFuture<?>>) i -> processPaymentAsync("BankD",
                                                "Payment Processed",
                                                status().isOk()))
                                .toArray(CompletableFuture[]::new);

                // Wait for all futures to complete
                CompletableFuture.allOf(futures).join();
        }

        @Test
        public void testMultipleSuccessfulPayments2() throws Exception {
                CompletableFuture<?>[] futures = IntStream.range(0, 500)
                                .mapToObj((IntFunction<CompletableFuture<?>>) i -> processPaymentAsync("BankE",
                                                "Payment Processed",
                                                status().isOk()))
                                .toArray(CompletableFuture[]::new);

                // Wait for all futures to complete
                CompletableFuture.allOf(futures).join();
        }

        @Test
        public void testMultipleUnsupportedBanks() throws Exception {
                CompletableFuture<?>[] futures = IntStream.range(0, 50)
                                .mapToObj((IntFunction<CompletableFuture<?>>) i -> processPaymentAsync(
                                                "UnsupportedBank",
                                                "No available payment gateway supports the bank: UnsupportedBank",
                                                status().isBadRequest()))
                                .toArray(CompletableFuture[]::new);

                // Wait for all futures to complete
                CompletableFuture.allOf(futures).join();
        }

        @Async
        public CompletableFuture<Void> processPaymentAsync(String bank, String expectedMessage,
                        ResultMatcher expectedStatus) {
                try {
                        mockMvc.perform(post("/api/v1/payments")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{ \"bank\": \"" + bank
                                                        + "\", \"currency\": \"USD\", \"amount\": 100.0, \"accountNumber\": \"123456789\", \"paymentMethod\": \"credit card\", \"customerName\": \"John Doe\" }"))
                                        .andExpect(expectedStatus)
                                        .andExpect(content().string(expectedMessage));
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return CompletableFuture.completedFuture(null);
        }
}