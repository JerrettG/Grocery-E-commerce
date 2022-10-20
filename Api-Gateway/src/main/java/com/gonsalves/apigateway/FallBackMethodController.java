package com.gonsalves.apigateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackMethodController {

    @GetMapping("/cartServiceFallBack")
    public String cartServiceFallBackMethod() {
        return "Cart Service is taking longer than Expected. Please try again later";
    }

    @GetMapping("/customerProfileServiceFallBack")
    public String customerProfileServiceFallBackMethod() {
        return "Customer Profile Service is taking longer than Expected. Please try again later";
    }
    @GetMapping("/orderServiceFallBack")
    public String orderServiceFallBackMethod() {
        return "Order Service is taking longer than Expected. Please try again later";
    }

    @GetMapping("/productServiceFallBack")
    public String productServiceFallBackMethod() {
        return "Product Service is taking longer than Expected. Please try again later";
    }
}
