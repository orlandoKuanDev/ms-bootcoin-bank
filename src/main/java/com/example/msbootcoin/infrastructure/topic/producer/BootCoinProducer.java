package com.example.msbootcoin.infrastructure.topic.producer;

import com.example.msbootcoin.application.dto.CreateTransferenceDTO;
import com.example.msbootcoin.domain.entity.Acquisition;
import com.example.msbootcoin.domain.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BootCoinProducer {
    private static final String SERVICE_WALLET_TOPIC = "service-wallet-topic";

    private static final String SERVICE_CREATE_CUSTOMER_TOPIC = "service-create-customer-topic";

    private static final String SERVICE_CREATE_RETIRE_TOPIC = "service-create-retire-topic";

    private static final String SERVICE_CREATE_DEPOSIT_TOPIC = "service-create-deposit-topic";


    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public BootCoinProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSaveCustomerService(Customer customer) {
        kafkaTemplate.send(SERVICE_CREATE_CUSTOMER_TOPIC, customer );
    }

    public void sendSaveAcquisitionService(Acquisition acquisition) {
        kafkaTemplate.send(SERVICE_WALLET_TOPIC, acquisition );
    }

    public void sendSaveRetireService(CreateTransferenceDTO retire) {
        kafkaTemplate.send(SERVICE_CREATE_RETIRE_TOPIC, retire );
    }

    public void sendSaveDepositService(CreateTransferenceDTO deposit) {
        kafkaTemplate.send(SERVICE_CREATE_DEPOSIT_TOPIC, deposit );
    }

}
