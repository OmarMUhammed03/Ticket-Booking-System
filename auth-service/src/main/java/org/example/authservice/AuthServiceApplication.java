package org.example.authservice;

import org.example.commonlibrary.config.KafkaConsumerConfig;
import org.example.commonlibrary.config.KafkaProducerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@ComponentScan(
        basePackages = {"org.example.authservice", "org.example.commonlibrary"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {KafkaConsumerConfig.class, KafkaProducerConfig.class}
        )
)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

