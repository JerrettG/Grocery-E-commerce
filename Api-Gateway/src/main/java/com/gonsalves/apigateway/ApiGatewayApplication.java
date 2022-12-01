package com.gonsalves.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(ApiGatewayApplication.class, args);
    }



//    @Bean
//    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("cart-service", r -> r.path("/api/***/cartService/**")
//                        .uri("http://cart-service-svc.default.svc.cluster.local"))
//                .route("customer-profile-service", r -> r.path("/api/***/customerProfileService/**")
//                        .uri("http://customer-profile-service-svc.default.svc.cluster.local"))
//                .route("order-service", r -> r.path("/api/***/orderService/**")
//                        .uri("http://order-service-svc.default.svc.cluster.local"))
//                .route("product-service", r -> r.path("/api/***/productService/**")
//                        .uri("http://product-service-svc.default.svc.cluster.local"))
//                .route("zipkin", r -> r.path("/api/v2/spans")
//                        .uri("http://zipkin-svc.default.svc.cluster.local"))
//                .build();
//
//
//    }

}
