package com.example.recommendations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // ← Add this
public class RecommendationsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationsServiceApplication.class, args);
    }
}