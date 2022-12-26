package com.gonsalves.grocery.frontend.frontend;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class FrontendApplication {
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void setup() {
        Stripe.apiKey = stripeApiKey;}

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {return new RestTemplate();}

    @Bean
    @Profile(value = {"dev","test"})
    public WebClient devAndTestWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8090")
                .build();
    }
    @Bean
    @Profile(value = "prod")
    public WebClient prodWebClient() {
        return WebClient.builder()
                .baseUrl("http://api-gateway-svc.default.svc.cluster.local")
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
