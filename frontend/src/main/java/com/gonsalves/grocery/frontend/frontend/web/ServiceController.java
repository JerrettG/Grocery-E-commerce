package com.gonsalves.grocery.frontend.frontend.web;

import com.gonsalves.grocery.frontend.frontend.model.CartItem;
import com.gonsalves.grocery.frontend.frontend.model.RemoveItemFromCartRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/services")
public class ServiceController {
    private final WebClient webClient;

    @Autowired
    public ServiceController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping(path = "/shoppingCart")
    public ResponseEntity<String> removeFromCart(@RequestBody RemoveItemFromCartRequest request) {
        ResponseEntity<String> response = webClient
                .delete()
                .uri(String.format("/static/api/v1/cartService/cart/%s/cartItem/%s", request.getUserId(), request.getId()))
                .retrieve().toEntity(String.class).block();

        return response;
    }

    @PostMapping(path = "/product/{productName}/addToCart")
    public ResponseEntity<String> addToCart(@RequestBody CartItem cartItem, Authentication authentication)  {
        String userId = authentication.getName();
        cartItem.setUserId(userId);
        ResponseEntity<String> response = webClient.post()
                .uri(String.format("/static/api/v1/cartService/cart/%s/cartItem",userId))
                .body(Mono.just(cartItem), CartItem.class).retrieve().toEntity(String.class).block();
        return response;
    }
}
