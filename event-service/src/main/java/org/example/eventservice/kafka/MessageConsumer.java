package org.example.eventservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

    @KafkaListener(topics = "ticket-reserved", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Object message) {
        LOGGER.info("Received message='{}'", message);
    }

}