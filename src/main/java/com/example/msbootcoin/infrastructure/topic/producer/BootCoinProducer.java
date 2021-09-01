package com.example.msbootcoin.infrastructure.topic.producer;

import com.example.msbootcoin.domain.entity.Acquisition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BootCoinProducer {
    private static final String SERVICE_CREATE_BOOTCOIN_TOPIC = "service-create-bootcoin-topic";
    private static final String SERVICE_PAY_BOOTCOINT_TOPIC = "service-pay-bootcoin-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public BootCoinProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSaveBootCoinService(Acquisition acquisition) {
        kafkaTemplate.send(SERVICE_CREATE_BOOTCOIN_TOPIC, acquisition );
    }

}
