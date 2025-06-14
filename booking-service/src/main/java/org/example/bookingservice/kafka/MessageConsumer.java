package org.example.bookingservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.service.BookingService;
import org.example.commonlibrary.kafka.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MessageConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);
    private final BookingService bookingService;
    private final MessageProducer producer;

    @KafkaListener(topics = "ticket-reserved", groupId = "${spring.kafka.consumer.group-id}")
    public void updateBookingStatus(String messageString) throws JsonProcessingException {
        LOGGER.info("Received message='{}'", messageString);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> messageMap = mapper.readValue(messageString, HashMap.class);
        bookingService.updateBookingStatus(UUID.fromString(messageMap.get("bookingId")), "WAITING_FOR_PAYMENT");
        LOGGER.info("Booking status updated to WAITING_FOR_PAYMENT for bookingId={}", messageMap.get("bookingId"));
    }

    @KafkaListener(topics = "booking-failed", groupId = "${spring.kafka.consumer.group-id}")
    public void failBooking(String messageString) throws JsonProcessingException {
        LOGGER.info("Received message='{}'", messageString);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> messageMap = mapper.readValue(messageString, HashMap.class);
        bookingService.updateBookingStatus(UUID.fromString(messageMap.get("bookingId")), "CANCELLED");
        LOGGER.info("Booking failed for bookingId={}", messageMap.get("bookingId"));
    }

    @KafkaListener(topics = "payment-success", groupId = "${spring.kafka.consumer.group-id}")
    public void completeBooking(String messageString) throws JsonProcessingException {
        LOGGER.info("Received message='{}'", messageString);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> messageMap = mapper.readValue(messageString, HashMap.class);
        bookingService.updateBookingStatus(UUID.fromString(messageMap.get("bookingId")), "CONFIRMED");
        LOGGER.info("Booking confirmed for bookingId={}", messageMap.get("bookingId"));
    }
}