package org.example.eventservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.commonlibrary.kafka.MessageProducer;
import org.example.eventservice.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MessageConsumer {
    private final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private final EventService eventService;
    private final MessageProducer messageProducer;

    @KafkaListener(topics = "reserve-ticket", groupId = "${spring.kafka.consumer.group-id}")
    public void reserveTicket(final String messageString) throws JsonProcessingException {
        logger.info("Received message='{}'", messageString);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> messageMap = mapper.readValue(messageString, HashMap.class);
        eventService.reserveEventTicket(UUID.fromString(messageMap.get("eventId")),
                UUID.fromString(messageMap.get("ticketId")),
                messageMap.get("userId"));
        logger.info("Ticket reserved successfully for eventId={}, ticketId={}, userId={}",
                messageMap.get("eventId"),
                messageMap.get("ticketId"),
                messageMap.get("userId"));
        messageProducer.sendMessage("ticket-reserved", new HashMap<>(
                Map.of("bookingId", messageMap.get("bookingId")
                )));
    }

}
