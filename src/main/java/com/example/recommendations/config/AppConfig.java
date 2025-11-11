//package com.example.recommendations.config;
//
//import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestClient;
//import org.springframework.web.client.RestTemplate;
//
//@Configuration
//public class AppConfig {
//
//    @Bean
//    @LoadBalanced
//    public RestClient restClient() {
//        return RestClient.builder().build();
//    }
//}