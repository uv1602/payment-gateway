// package com.loadbalancer.payment_gateway.service;

// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.loadbalancer.payment_gateway.entity.GatewayMapping;
// import com.loadbalancer.payment_gateway.entity.PaymentGateway;
// import com.loadbalancer.payment_gateway.repository.GatewayMappingRepository;

// @Service
// public class LoadBalancerService {

//     @Autowired
//     private GatewayMappingRepository gatewayMappingRepository;

//     private AtomicInteger currentIndex = new AtomicInteger(0);

//     private final Map<Long, AtomicInteger> gatewayRequestCounts = new ConcurrentHashMap<>();

//     public PaymentGateway getBestGateway(Long amcId, Long paymentMethodId) {
//         List<GatewayMapping> mappings = gatewayMappingRepository.findByAMCAndPaymentMethod(amcId, paymentMethodId);

//         if (mappings.isEmpty()) {
//             return null;
//         }

//         // Calculate total weight
//         int totalWeight = mappings.stream()
//                 .mapToInt(GatewayMapping::getWeight)
//                 .sum();

//         if (totalWeight == 0) {
//             return null;
//         }

//         List<PaymentGateway> gateways = mappings.stream()
//                 .collect(Collectors.groupingBy(
//                         GatewayMapping::getGateway,
//                         Collectors.summingInt(GatewayMapping::getWeight)))
//                 .entrySet().stream()
//                 .flatMap(entry -> java.util.stream.Stream.generate(() -> entry.getKey())
//                         .limit(entry.getValue()))
//                 .distinct()
//                 .toList();

//         if (gateways.isEmpty()) {
//             return null;
//         }

//         int index = currentIndex.getAndUpdate(i -> (i + 1) % gateways.size());
//         PaymentGateway selectedGateway = gateways.get(index);

//         // Track the request count for the selected gateway
//         gatewayRequestCounts.computeIfAbsent(selectedGateway.getId(), k -> new AtomicInteger(0))
//                 .incrementAndGet();
//         return selectedGateway;
//     }

//     // Method to get the tracking information for testing or monitoring
//     public Map<Long, Integer> getGatewayUsage() {
//         return gatewayRequestCounts.entrySet().stream()
//                 .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
//     }
// }

package com.loadbalancer.payment_gateway.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadbalancer.payment_gateway.entity.GatewayMapping;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.repository.GatewayMappingRepository;

@Service
public class LoadBalancerService {

    @Autowired
    private GatewayMappingRepository gatewayMappingRepository;

    private final Map<PaymentGateway, Integer> gatewayWeights = new ConcurrentHashMap<>();
    private final Map<PaymentGateway, AtomicInteger> gatewayUsage = new ConcurrentHashMap<>();
    private final Map<PaymentGateway, AtomicInteger> unsupportedBankRequests = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String makePaymentRequest(PaymentGateway gateway, Double amount, String currency, String source)
            throws RuntimeException, IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("currency", currency);
        data.put("source", source);

        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            System.err.println("Error creating JSON payload: " + e.getMessage());
            return null;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gateway.getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new RuntimeException("Payment request failed with status code: " + response.statusCode()
                        + " -- body -- " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException | RuntimeException e) {
            System.err.println("Error during HTTP request: " + e.getMessage());
            throw e;
        }
    }

    public PaymentGateway getBestGateway(Long amcId, Long paymentMethodId) {
        List<GatewayMapping> mappings = gatewayMappingRepository.findByAMCAndPaymentMethod(amcId, paymentMethodId);

        if (mappings.isEmpty()) {
            trackUnsupportedBankRequest(amcId);
            return null;
        }

        initializeGatewayMaps(mappings);

        List<PaymentGateway> providers = gatewayWeights.keySet().stream()
                .collect(Collectors.toList());

        if (providers.isEmpty()) {
            trackUnsupportedBankRequest(amcId);
            return null;
        }

        PaymentGateway selectedProvider = selectProvider(providers);
        gatewayUsage.get(selectedProvider).incrementAndGet();
        return selectedProvider;
    }

    private void initializeGatewayMaps(List<GatewayMapping> mappings) {
        mappings.forEach(mapping -> {
            PaymentGateway gateway = mapping.getGateway();
            gatewayWeights.putIfAbsent(gateway, 0);
            gatewayUsage.putIfAbsent(gateway, new AtomicInteger(0));
            unsupportedBankRequests.putIfAbsent(gateway, new AtomicInteger(0));

            gatewayWeights.merge(gateway, mapping.getWeight(), Integer::sum);
        });
    }

    private PaymentGateway selectProvider(List<PaymentGateway> providers) {
        int totalWeight = providers.stream().mapToInt(gatewayWeights::get).sum();
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (PaymentGateway provider : providers) {
            cumulativeWeight += gatewayWeights.get(provider);
            if (randomValue < cumulativeWeight) {
                return provider;
            }
        }

        throw new IllegalStateException("Provider selection failed due to weight misconfiguration");
    }

    private void trackUnsupportedBankRequest(long amcId) {
        System.out.println(amcId);
        gatewayWeights.keySet().forEach(provider -> unsupportedBankRequests.get(provider).incrementAndGet());
    }

    public void adjustWeights() {
        int totalRequests = gatewayUsage.values().stream().mapToInt(AtomicInteger::get).sum();
        int totalUnsupportedRequests = unsupportedBankRequests.values().stream().mapToInt(AtomicInteger::get).sum();

        gatewayWeights.forEach((provider, weight) -> {
            int usage = gatewayUsage.get(provider).get();
            int unsupportedRequests = unsupportedBankRequests.get(provider).get();

            int newWeight = calculateNewWeight(weight, usage, unsupportedRequests, totalRequests,
                    totalUnsupportedRequests);
            gatewayWeights.put(provider, newWeight);
        });

        normalizeWeights();
    }

    private int calculateNewWeight(int currentWeight, int usage, int unsupportedRequests, int totalRequests,
            int totalUnsupportedRequests) {
        double usageRatio = (double) usage / totalRequests;
        double unsupportedRatio = totalUnsupportedRequests > 0 ? (double) unsupportedRequests / totalUnsupportedRequests
                : 0;
        return (int) (currentWeight * (1 - unsupportedRatio) + (usageRatio * 100));
    }

    private void normalizeWeights() {
        int totalWeight = gatewayWeights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight == 0)
            return;

        gatewayWeights.replaceAll((provider, weight) -> weight * 100 / totalWeight);
    }

    public Map<PaymentGateway, Integer> getGatewayUsage() {
        return gatewayUsage.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    public Map<PaymentGateway, Integer> getUnsupportedBankRequests() {
        return unsupportedBankRequests.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }
}
