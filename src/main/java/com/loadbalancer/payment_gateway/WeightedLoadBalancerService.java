package com.loadbalancer.payment_gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class WeightedLoadBalancerService {

    private final Map<PaymentGateway, Integer> gatewayWeights;
    private final Map<PaymentGateway, Integer> gatewayUsage;
    private final Map<PaymentGateway, Integer> unsupportedBankRequests; // Track unsupported bank requests

    public WeightedLoadBalancerService() {
        gatewayWeights = new HashMap<>();
        gatewayWeights.put(new PaymentGatewayA(), 70);
        gatewayWeights.put(new PaymentGatewayB(), 30);

        gatewayUsage = new HashMap<>();
        gatewayWeights.keySet().forEach(gateway -> gatewayUsage.put(gateway, 0));

        unsupportedBankRequests = new HashMap<>();
        gatewayWeights.keySet().forEach(gateway -> unsupportedBankRequests.put(gateway, 0));
    }

    public boolean processPayment(PaymentRequest request) throws Exception {
        List<PaymentGateway> gateways = gatewayWeights.keySet().stream()
                .filter(gateway -> gateway.supportsBank(request.getBank()))
                .collect(Collectors.toList());

        if (gateways.isEmpty()) {
            trackUnsupportedBankRequest(request.getBank());
            throw new Exception("No available payment gateway supports the bank: " + request.getBank());
        }

        while (!gateways.isEmpty()) {
            PaymentGateway selectedGateway = selectGateway(gateways);
            try {
                gatewayUsage.put(selectedGateway, gatewayUsage.get(selectedGateway) + 1);
                return selectedGateway.processPayment(request);
            } catch (Exception e) {
                System.out.println("Failed to process with selected gateway, trying another one: " + e.getMessage());
            }

            gateways.remove(selectedGateway);
        }

        throw new Exception("Failed to process payment with any available gateways.");
    }

    private void trackUnsupportedBankRequest(String bank) {
        // Increase the request count for all gateways (or specific ones if needed)
        gatewayWeights.keySet()
                .forEach(gateway -> unsupportedBankRequests.put(gateway, unsupportedBankRequests.get(gateway) + 1));
    }

    private PaymentGateway selectGateway(List<PaymentGateway> gateways) {
        int totalWeight = gateways.stream().mapToInt(gatewayWeights::get).sum();
        int random = new Random().nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (PaymentGateway gateway : gateways) {
            cumulativeWeight += gatewayWeights.get(gateway);
            if (random < cumulativeWeight) {
                return gateway;
            }
        }

        throw new IllegalStateException("Gateway selection failed due to weight misconfiguration");
    }

    public void adjustWeights() {
        // Example adjustment strategy: Increase weight for gateways with fewer
        // unsupported requests
        int totalRequests = gatewayUsage.values().stream().mapToInt(Integer::intValue).sum();
        int totalUnsupportedRequests = unsupportedBankRequests.values().stream().mapToInt(Integer::intValue).sum();

        gatewayWeights.forEach((gateway, weight) -> {
            int usage = gatewayUsage.get(gateway);
            int unsupportedRequests = unsupportedBankRequests.get(gateway);

            // Adjust weights based on usage and unsupported requests
            int newWeight = calculateNewWeight(weight, usage, unsupportedRequests, totalRequests,
                    totalUnsupportedRequests);
            gatewayWeights.put(gateway, newWeight);
        });

        // Normalize weights to sum up to 100%
        normalizeWeights();
    }

    private int calculateNewWeight(int currentWeight, int usage, int unsupportedRequests, int totalRequests,
            int totalUnsupportedRequests) {
        // Example adjustment formula (simplified)
        double usageRatio = (double) usage / totalRequests;
        double unsupportedRatio = (double) unsupportedRequests / totalUnsupportedRequests;
        return (int) (currentWeight * (1 - unsupportedRatio) + (usageRatio * 100));
    }

    private void normalizeWeights() {
        int totalWeight = gatewayWeights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight == 0)
            return;

        gatewayWeights.replaceAll((gateway, weight) -> weight * 100 / totalWeight);
    }

    public Map<PaymentGateway, Integer> getGatewayUsage() {
        return gatewayUsage;
    }
}
