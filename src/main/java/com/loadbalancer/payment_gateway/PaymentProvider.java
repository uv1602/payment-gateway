package com.loadbalancer.payment_gateway;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.loadbalancer.payment_gateway.entity.Bank;
import com.loadbalancer.payment_gateway.entity.GatewayBank;
import com.loadbalancer.payment_gateway.entity.PaymentMapping;
import com.loadbalancer.payment_gateway.model.PaymentRequest;
import com.loadbalancer.payment_gateway.repository.BankRepository;
import com.loadbalancer.payment_gateway.repository.GatewayBankRepository;
import com.loadbalancer.payment_gateway.repository.PaymentMappingRepository;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class PaymentProvider implements IPaymentGateway {

    private long id;
    private String name;
    private List<GatewayBank> supportedBanks;

    private final PaymentMappingRepository paymentMappingRepository;
    private final BankRepository bankRepository;
    private final GatewayBankRepository gatewayBankRepository;

    @Autowired
    public PaymentProvider(PaymentMappingRepository paymentMappingRepository,
            BankRepository bankRepository,
            GatewayBankRepository gatewayBankRepository) {
        this.paymentMappingRepository = paymentMappingRepository;
        this.bankRepository = bankRepository;
        this.gatewayBankRepository = gatewayBankRepository;
    }

    public void initialize(Long paymentGatewayId) {
        this.id = paymentGatewayId;
        System.out.println(paymentGatewayId);
        this.supportedBanks = gatewayBankRepository.findAll().stream()
                .filter(gatewayBank -> gatewayBank.getGateway().getId().equals(paymentGatewayId))
                .collect(Collectors.toList());
        System.out.println(this.supportedBanks);
    }

    @Override
    public boolean processPayment(PaymentRequest request) {
        // Implement your payment processing logic here
        return request.getAmcId() == 4L;
    }

    @Override
    public boolean supportsBank(Bank bank, long amcId, long paymentMethodId) {
        List<PaymentMapping> mappingPayment = this.paymentMappingRepository
                .findByAmcIdAndPaymentMethodId(amcId, paymentMethodId);

        for (PaymentMapping paymentMapping : mappingPayment) {
            boolean exists = false;

            if (this.supportedBanks != null)
                exists = this.supportedBanks.stream()
                        .anyMatch(gatewayBank -> gatewayBank.getBank().getId().equals(bank.getId())
                                && gatewayBank.getGateway().getId().equals(paymentMapping.getGatewayId()));

            if (exists) {
                return true;
            }
        }
        return false;
    }
}
