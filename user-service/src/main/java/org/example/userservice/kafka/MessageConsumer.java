package org.example.userservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.commonlibrary.kafka.MessageProducer;
import org.example.userservice.dto.CreateUserDto;
import org.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MessageConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);
    private final UserService userService;

    @KafkaListener(topics = "user-created", groupId = "${spring.kafka.consumer.group-id}")
    public void createUser(String messageString) throws JsonProcessingException {
        LOGGER.info("Received message='{}'", messageString);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> messageMap = mapper.readValue(messageString, HashMap.class);
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setId(UUID.fromString(messageMap.get("id")));
        createUserDto.setEmail(messageMap.get("email"));
        createUserDto.setFirstname(messageMap.get("firstname"));
        createUserDto.setLastname(messageMap.get("lastname"));
        createUserDto.setDateOfBirth(messageMap.get("dateOfBirth") != null ?
                LocalDate.parse(messageMap.get("dateOfBirth")) : null);
        createUserDto.setGender(messageMap.get("gender"));
        userService.createUser(createUserDto);
        LOGGER.info("User created with ID: {}", createUserDto.getId());
    }

}