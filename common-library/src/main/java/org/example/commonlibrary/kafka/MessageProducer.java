package org.example.commonlibrary.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MessageProducer(final KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(final String topic, final Object message) {
        kafkaTemplate.send(topic, message);
    }

    public void sendMessage(final String topic, final String key, final Object message) {
        kafkaTemplate.send(topic, key, message);
    }
}
