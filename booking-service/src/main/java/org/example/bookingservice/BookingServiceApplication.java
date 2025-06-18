package org.example.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@ComponentScan(basePackages = {
        "org.example.bookingservice",
        "org.example.commonlibrary"
})
@EnableCaching
public class BookingServiceApplication {
    public static void main(final String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}

