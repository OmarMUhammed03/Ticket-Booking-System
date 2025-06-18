package org.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@ComponentScan(basePackages = {
        "org.example.userservice",
        "org.example.commonlibrary"
})
public class UserServiceApplication {
    public static void main(final String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

