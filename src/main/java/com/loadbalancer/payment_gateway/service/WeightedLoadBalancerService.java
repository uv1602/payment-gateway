// package com.loadbalancer.payment_gateway.service;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Random;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.loadbalancer.payment_gateway.PaymentProvider;
// import com.loadbalancer.payment_gateway.entity.Amc;
// import com.loadbalancer.payment_gateway.entity.PaymentGateway;
// import com.loadbalancer.payment_gateway.entity.PaymentMapping;
// import com.loadbalancer.payment_gateway.model.PaymentRequest;import com.loadbalancer.payment_gateway.repository.AmcRepository;
// ;
// import com.loadbalancer.payment_gateway.repository.PaymentGatewayRepository;
// import com.loadbalancer.payment_gateway.repository.PaymentMappingRepository;

// import jakarta.annotation.PostConstruct;

// @Service
// public class WeightedLoadBalancerService {

//     private final Map<PaymentProvider, Integer> gatewayWeights = new HashMap<>();
//     private final Map<PaymentProvider, Integer> gatewayUsage = new HashMap<>();
//     private final Map<PaymentProvider, Integer> unsupportedBankRequests = new HashMap<>();

//     @Autowired
//     private PaymentMappingRepository paymentMappingRepository ;

// @PostConstruct
// public void loadGatewayData() {
//     Map<Long, Integer> mappingPayment = paymentMappingRepository.findAll().stream()
//     .collect(Collectors.toMap(PaymentMapping::getGatewayId, PaymentMapping::getWeight));

//     mappingPayment.forEach((gatewayId, weight) -> {
//         PaymentProvider paymentProvider = new PaymentProvider(gatewayId);

//         // Store in maps
//         gatewayWeights.put(paymentProvider, weight);
//         gatewayUsage.put(paymentProvider, 0);
//         unsupportedBankRequests.put(paymentProvider, 0);
//     });
// }

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

//     public Map<PaymentProvider, Integer> getGatewayUsage() {
//         return gatewayUsage;
//     }
// }

// package com.loadbalancer.payment_gateway.service;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Random;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import com.loadbalancer.payment_gateway.PaymentProvider;
// import com.loadbalancer.payment_gateway.entity.PaymentGateway;
// import com.loadbalancer.payment_gateway.model.PaymentRequest;
// import com.loadbalancer.payment_gateway.repository.PaymentGatewayRepository;
// import com.loadbalancer.payment_gateway.repository.PaymentMappingRepository;

// import jakarta.annotation.PostConstruct;

// @Service
// public class WeightedLoadBalancerService {

//     @Autowired
//     private PaymentMappingRepository mappingRepository ;

//     private final Map<PaymentProvider, Integer> gatewayWeights = new HashMap<>();
//     private final Map<PaymentProvider, Integer> gatewayUsage = new HashMap<>();
//     private final Map<PaymentProvider, Integer> unsupportedBankRequests = new HashMap<>();

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

//     public Map<PaymentProvider, Integer> getGatewayUsage() {
//         return gatewayUsage;
//     }
// }

package com.loadbalancer.payment_gateway.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.loadbalancer.payment_gateway.PaymentProvider;
import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.entity.PaymentGateway;
import com.loadbalancer.payment_gateway.entity.PaymentMapping;
import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.repository.BankRepository;
import com.loadbalancer.payment_gateway.repository.GatewayBankRepository;
import com.loadbalancer.payment_gateway.repository.PaymentGatewayRepository;
import com.loadbalancer.payment_gateway.repository.PaymentMappingRepository;

import jakarta.annotation.PostConstruct;

@Service
public class WeightedLoadBalancerService {

    private final Map<PaymentProvider, Integer> gatewayWeights = new HashMap<>();
    private final Map<PaymentProvider, Integer> gatewayUsage = new HashMap<>();
    private final Map<PaymentProvider, Integer> unsupportedBankRequests = new HashMap<>();

    @Autowired
    private PaymentMappingRepository paymentMappingRepository;

    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private GatewayBankRepository gatewayBankRepository;

    public Set<Bank> findSupportedBanks(Long amcId, Long paymentMethodId) {
        List<PaymentMapping> mappings = paymentMappingRepository.findByAmcIdAndPaymentMethodId(amcId, paymentMethodId);

        System.out.println(mappings);
        Set<Bank> supportedBanks = new HashSet<>();
        for (PaymentMapping mapping : mappings) {
            PaymentGateway gateway = paymentGatewayRepository.findById(mapping.getGatewayId())
                    .orElseThrow(
                            () -> new RuntimeException("PaymentGateway not found with id: " + mapping.getGatewayId()));
            supportedBanks.addAll(gateway.getBanks());
        }
        System.out.println(supportedBanks);
        return supportedBanks;
    }

    @PostConstruct
    public void loadGatewayData() {
        Map<Long, Integer> mappingPayment = paymentMappingRepository.findAll().stream()
                .collect(Collectors.toMap(
                        PaymentMapping::getGatewayId,
                        PaymentMapping::getWeight,
                        Integer::sum));
        mappingPayment.forEach((gatewayId, weight) -> {
            PaymentProvider paymentProvider = new PaymentProvider(paymentMappingRepository, bankRepository,
                    gatewayBankRepository);
            paymentProvider.initialize(gatewayId);
            gatewayWeights.put(paymentProvider, weight);
            gatewayUsage.put(paymentProvider, 0);
            unsupportedBankRequests.put(paymentProvider, 0);
        });
        System.out.println(gatewayWeights);
    }

    public boolean processPayment(PaymentRequest request) throws Exception {
        System.out.println(request);
        Bank bank = this.bankRepository.findById(request.getBankId()).orElseThrow();
        List<PaymentProvider> providers = gatewayWeights.keySet().stream()
                .filter(provider -> provider
                        .supportsBank(bank, request.getAmcId(), request.getPaymentMethodId()))
                .collect(Collectors.toList());
        System.out.println(providers);
        if (providers.isEmpty()) {
            // trackUnsupportedBankRequest(request.getBank().getName());
            System.out.println("Payment Failed");
            throw new Exception("No available payment provider supports the bank: ");
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

    // private void trackUnsupportedBankRequest(String bank) {
    // gatewayWeights.keySet()
    // .forEach(provider -> unsupportedBankRequests.put(provider,
    // unsupportedBankRequests.get(provider) + 1));
    // }

    private PaymentProvider selectProvider(List<PaymentProvider> providers) {
        int totalWeight = providers.stream().mapToInt(gatewayWeights::get).sum();
        int random = new Random().nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (PaymentProvider provider : providers) {
            cumulativeWeight += gatewayWeights.get(provider);
            if (random < cumulativeWeight) {
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

    public Map<PaymentProvider, Integer> getGatewayUsage() {
        return gatewayUsage;
    }
}
