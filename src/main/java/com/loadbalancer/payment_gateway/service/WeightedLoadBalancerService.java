// package com.loadbalancer.payment_gateway.service;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Random;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.loadbalancer.payment_gateway.PaymentProvider;
// import com.loadbalancer.payment_gateway.entity.GatewayWeight;
// import com.loadbalancer.payment_gateway.entity.PaymentGateway;
// import com.loadbalancer.payment_gateway.model.PaymentRequest;
// import com.loadbalancer.payment_gateway.repository.GatewayWeightRepository;
// import com.loadbalancer.payment_gateway.repository.PaymentGatewayRepository;

// import jakarta.annotation.PostConstruct;

// @Service
// public class WeightedLoadBalancerService {

//     @Autowired
//     private PaymentGatewayRepository paymentGatewayRepository;

//     @Autowired
//     private GatewayWeightRepository gatewayWeightRepository;

//     @Autowired
//     private PaymentGatewayService paymentGatewayService;

//     private final Map<PaymentProvider, Integer> gatewayWeights = new HashMap<>();
//     private final Map<PaymentProvider, Integer> previousWeights = new HashMap<>();
//     private final Map<PaymentProvider, Integer> gatewayUsage = new HashMap<>();
//     private final Map<PaymentProvider, Integer> unsupportedBankRequests = new HashMap<>();

//     @PostConstruct
//     public void loadGatewayData() {
//         List<PaymentGateway> gateways = paymentGatewayRepository.findAll();
//         Map<Long, Integer> weightMap = gatewayWeightRepository.findAll().stream()
//                 .collect(Collectors.toMap(GatewayWeight::getGatewayId, GatewayWeight::getWeight));

//         gateways.forEach(gateway -> {
//             Integer weight = weightMap.getOrDefault(gateway.getId(), 0);
//             PaymentProvider paymentProvider = new PaymentProvider(gateway);
//             gatewayWeights.put(paymentProvider, weight);
//             previousWeights.put(paymentProvider, weight); // Initialize previous weights
//             gatewayUsage.put(paymentProvider, 0);
//             unsupportedBankRequests.put(paymentProvider, 0);
//         });
//     }

//     public boolean processPayment(PaymentRequest request) throws Exception {
//         List<PaymentProvider> providers = gatewayWeights.keySet().stream()
//                 .filter(provider -> paymentGatewayService.isBankSupportedByGateway(provider.getId(), request.getBank()))
//                 .collect(Collectors.toList());

//         if (providers.isEmpty()) {
//             trackUnsupportedBankRequest(request.getBank());
//             throw new Exception("No available payment provider supports the bank: " + request.getBank());
//         }

//         while (!providers.isEmpty()) {
//             PaymentProvider selectedProvider = selectProvider(providers);
//             try {
//                 gatewayUsage.put(selectedProvider, gatewayUsage.get(selectedProvider) + 1);
//                 return selectedProvider.processPayment(request);
//             } catch (Exception e) {
//                 System.out.println("Failed to process with selected provider, trying another one: " + e.getMessage());
//             }

//             providers.remove(selectedProvider);
//         }

//         throw new Exception("Failed to process payment with any available providers.");
//     }

//     private void trackUnsupportedBankRequest(String bank) {
//         gatewayWeights.keySet()
//                 .forEach(provider -> unsupportedBankRequests.put(provider, unsupportedBankRequests.get(provider) + 1));
//     }

//     private PaymentProvider selectProvider(List<PaymentProvider> providers) {
//         int totalWeight = providers.stream().mapToInt(gatewayWeights::get).sum();
//         int random = new Random().nextInt(totalWeight);
//         int cumulativeWeight = 0;

//         for (PaymentProvider provider : providers) {
//             cumulativeWeight += gatewayWeights.get(provider);
//             System.out
//                     .println("weight  - " + gatewayWeights.get(provider) + "  cumulativeWeight - " + cumulativeWeight);
//             if (random < cumulativeWeight) {
//                 System.out.println(provider.getName() + "  " + random + "    " + cumulativeWeight);
//                 return provider;
//             }
//         }

//         throw new IllegalStateException("Provider selection failed due to weight misconfiguration");
//     }

//     public void adjustWeights() {
//         int totalRequests = gatewayUsage.values().stream().mapToInt(Integer::intValue).sum();
//         int totalUnsupportedRequests = unsupportedBankRequests.values().stream().mapToInt(Integer::intValue).sum();

//         gatewayWeights.forEach((provider, weight) -> {
//             int usage = gatewayUsage.get(provider);
//             int unsupportedRequests = unsupportedBankRequests.get(provider);

//             int newWeight = calculateNewWeight(weight, usage, unsupportedRequests, totalRequests,
//                     totalUnsupportedRequests);
//             gatewayWeights.put(provider, newWeight);
//         });

//         normalizeWeights();

//         if (weightsChanged()) {
//             saveWeightsToDatabase();
//             previousWeights.clear();
//             previousWeights.putAll(gatewayWeights);
//         }
//     }

//     private int calculateNewWeight(int currentWeight, int usage, int unsupportedRequests, int totalRequests,
//             int totalUnsupportedRequests) {
//         double usageRatio = (double) usage / totalRequests;
//         double unsupportedRatio = totalUnsupportedRequests > 0 ? (double) unsupportedRequests / totalUnsupportedRequests
//                 : 0;
//         return (int) (currentWeight * (1 - unsupportedRatio) + (usageRatio * 100));
//     }

//     private void normalizeWeights() {
//         int totalWeight = gatewayWeights.values().stream().mapToInt(Integer::intValue).sum();
//         if (totalWeight == 0)
//             return;

//         gatewayWeights.replaceAll((provider, weight) -> weight * 100 / totalWeight);
//     }

//     private boolean weightsChanged() {
//         for (Map.Entry<PaymentProvider, Integer> entry : gatewayWeights.entrySet()) {
//             Integer previousWeight = previousWeights.get(entry.getKey());
//             if (previousWeight == null || !previousWeight.equals(entry.getValue())) {
//                 return true;
//             }
//         }
//         return false;
//     }

//     private void saveWeightsToDatabase() {
//         List<GatewayWeight> updatedWeights = gatewayWeights.entrySet().stream()
//                 .map(entry -> new GatewayWeight(entry.getKey().getId(), entry.getValue()))
//                 .collect(Collectors.toList());

//         gatewayWeightRepository.saveAll(updatedWeights);
//     }

//     public Map<PaymentProvider, Integer> getGatewayUsage() {
//         return gatewayUsage;
//     }
// }

package com.loadbalancer.payment_gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.loadbalancer.payment_gateway.PaymentProvider;
import com.loadbalancer.payment_gateway.entity.GatewayWeight;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.repository.GatewayWeightRepository;
import com.loadbalancer.payment_gateway.repository.PaymentGatewayRepository;

import jakarta.annotation.PostConstruct;

@Service
public class WeightedLoadBalancerService {

    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;

    @Autowired
    private GatewayWeightRepository gatewayWeightRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    private final Map<PaymentProvider, Integer> gatewayWeights = new HashMap<>();
    private final Map<PaymentProvider, Integer> previousWeights = new HashMap<>();
    private final Map<PaymentProvider, Integer> gatewayUsage = new HashMap<>();
    private final Map<PaymentProvider, Integer> unsupportedBankRequests = new HashMap<>();

    @PostConstruct
    public void loadGatewayData() {
        refreshGatewayData();
    }

    private void refreshGatewayData() {
        List<PaymentGateway> gateways = paymentGatewayRepository.findAll();
        Map<Long, Integer> weightMap = gatewayWeightRepository.findAll().stream()
                .collect(Collectors.toMap(GatewayWeight::getGatewayId, GatewayWeight::getWeight));

        gateways.forEach(gateway -> {
            Integer weight = weightMap.getOrDefault(gateway.getId(), 0);
            PaymentProvider paymentProvider = new PaymentProvider(gateway);
            gatewayWeights.put(paymentProvider, weight);
            previousWeights.put(paymentProvider, weight); // Initialize previous weights
            gatewayUsage.put(paymentProvider, 0);
            unsupportedBankRequests.put(paymentProvider, 0);
        });
    }

    @Scheduled(fixedRate = 1000) // 60,000 milliseconds = 1 minute
    public void updateWeightsIfChanged() {
        System.out.println("Called Scheduled");
        List<GatewayWeight> latestWeights = gatewayWeightRepository.findAll();
        Map<Long, Integer> currentWeightMap = latestWeights.stream()
                .collect(Collectors.toMap(GatewayWeight::getGatewayId, GatewayWeight::getWeight));

        boolean weightsUpdated = false;

        for (Map.Entry<PaymentProvider, Integer> entry : gatewayWeights.entrySet()) {
            Integer currentWeight = currentWeightMap.get(entry.getKey().getId());
            if (currentWeight != null && !currentWeight.equals(entry.getValue())) {
                gatewayWeights.put(entry.getKey(), currentWeight);
                weightsUpdated = true;
            }
        }

        if (weightsUpdated) {
            System.out.println("Called updated");
            adjustWeights();
        }
    }

    public boolean processPayment(PaymentRequest request) throws Exception {
        List<PaymentProvider> providers = gatewayWeights.keySet().stream()
                .filter(provider -> paymentGatewayService.isBankSupportedByGateway(provider.getId(), request.getBank()))
                .collect(Collectors.toList());

        if (providers.isEmpty()) {
            trackUnsupportedBankRequest(request.getBank());
            throw new Exception("No available payment provider supports the bank: " + request.getBank());
        }

        while (!providers.isEmpty()) {
            PaymentProvider selectedProvider = selectProvider(providers);
            try {
                gatewayUsage.put(selectedProvider, gatewayUsage.get(selectedProvider) + 1);
                return selectedProvider.processPayment(request);
            } catch (Exception e) {
                System.out.println("Failed to process with selected provider, trying another one: " + e.getMessage());
            }

            providers.remove(selectedProvider);
        }

        throw new Exception("Failed to process payment with any available providers.");
    }

    private void trackUnsupportedBankRequest(String bank) {
        gatewayWeights.keySet()
                .forEach(provider -> unsupportedBankRequests.put(provider, unsupportedBankRequests.get(provider) + 1));
    }

    private PaymentProvider selectProvider(List<PaymentProvider> providers) {
        int totalWeight = providers.stream().mapToInt(gatewayWeights::get).sum();
        int random = new Random().nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (PaymentProvider provider : providers) {
            cumulativeWeight += gatewayWeights.get(provider);
            System.out
                    .println("weight  - " + gatewayWeights.get(provider) + "  cumulativeWeight - " + cumulativeWeight);
            if (random < cumulativeWeight) {
                System.out.println(provider.getName() + "  " + random + "    " + cumulativeWeight);
                return provider;
            }
        }

        throw new IllegalStateException("Provider selection failed due to weight misconfiguration");
    }

    public void adjustWeights() {
        int totalRequests = gatewayUsage.values().stream().mapToInt(Integer::intValue).sum();
        int totalUnsupportedRequests = unsupportedBankRequests.values().stream().mapToInt(Integer::intValue).sum();

        gatewayWeights.forEach((provider, weight) -> {
            int usage = gatewayUsage.get(provider);
            int unsupportedRequests = unsupportedBankRequests.get(provider);

            int newWeight = calculateNewWeight(weight, usage, unsupportedRequests, totalRequests,
                    totalUnsupportedRequests);
            gatewayWeights.put(provider, newWeight);
        });

        normalizeWeights();

        if (weightsChanged()) {
            saveWeightsToDatabase();
            previousWeights.clear();
            previousWeights.putAll(gatewayWeights);
        }
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

    private boolean weightsChanged() {
        for (Map.Entry<PaymentProvider, Integer> entry : gatewayWeights.entrySet()) {
            Integer previousWeight = previousWeights.get(entry.getKey());
            if (previousWeight == null || !previousWeight.equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    private void saveWeightsToDatabase() {
        List<GatewayWeight> updatedWeights = gatewayWeights.entrySet().stream()
                .map(entry -> new GatewayWeight(entry.getKey().getId(), entry.getValue()))
                .collect(Collectors.toList());

        gatewayWeightRepository.saveAll(updatedWeights);
    }

    public Map<PaymentProvider, Integer> getGatewayUsage() {
        return gatewayUsage;
    }
}
