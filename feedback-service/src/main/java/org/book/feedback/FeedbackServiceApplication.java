package org.book.feedback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FeedbackServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeedbackServiceApplication.class, args);
    }
}