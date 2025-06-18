package org.example.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@ComponentScan(
        basePackages = {"org.example.authservice", "org.example.commonlibrary"}
)
public class AuthServiceApplication {
    public static void main(final String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

