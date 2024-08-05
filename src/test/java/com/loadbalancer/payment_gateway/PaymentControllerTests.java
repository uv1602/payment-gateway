package com.loadbalancer.payment_gateway;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
        private ObjectMapper objectMapper; // For converting objects to JSON

        private final CountDownLatch latch = new CountDownLatch(1);

        @AfterEach
        public void printLoadDistribution() {
                System.out.println("Load Distribution:");
                loadBalancerService.getGatewayUsage().forEach(
                                (gateway, count) -> System.out.println(gateway + " handled " + count + " requests"));
        }

        @Test
        public void testMultipleSuccessfulPayments() throws Exception {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

                Runnable task = () -> {
                        try {
                                CompletableFuture<?>[] futures = IntStream.range(0, 10) // Adjust the range as needed
                                                .mapToObj((IntFunction<CompletableFuture<?>>) i -> processPaymentAsync(
                                                                "Bank Y",
                                                                "Credit Card",
                                                                "AMC Alpha",
                                                                "Payment Processed",
                                                                status().isOk(),
                                                                "John Doe"))
                                                .toArray(CompletableFuture[]::new);

                                CompletableFuture.allOf(futures).join();
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                };

                // Schedule the task to run every second, starting after a 10-second delay
                executor.scheduleAtFixedRate(task, 10, 1, TimeUnit.SECONDS);

                // Run for 5 minutes
                new Thread(() -> {
                        try {
                                Thread.sleep(2 * 60 * 1000);
                                latch.countDown(); // Signal that the test should end
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }).start();

                // Wait until latch is counted down
                latch.await();

                // Shutdown executor
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        @Async
        public CompletableFuture<Void> processPaymentAsync(String bank, String paymentMethod, String amc,
                        String expectedMessage, ResultMatcher expectedStatus, String customerName) {
                try {
                        PaymentRequest paymentRequest = new PaymentRequest(bank, "USD", 100.0,
                                        "123456789", paymentMethod, customerName, amc);
                        String requestJson = objectMapper.writeValueAsString(paymentRequest);

                        mockMvc.perform(post("/api/v1/payments")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andExpect(expectedStatus)
                                        .andExpect(content().string(expectedMessage));
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return CompletableFuture.completedFuture(null);
        }
}
