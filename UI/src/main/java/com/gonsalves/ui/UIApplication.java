package com.gonsalves.UI;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@EnableDiscoveryClient
@SpringBootApplication
public class UIApplication {
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void setup() {
        Stripe.apiKey = stripeApiKey;}

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {return new RestTemplate();}

    public static void main(String[] args) {
        SpringApplication.run(UIApplication.class, args);
    }
}
