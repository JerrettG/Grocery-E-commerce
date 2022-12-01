package com.gonsalves.ui.web;

import com.gonsalves.ui.model.CartItem;
import com.gonsalves.ui.model.RemoveItemFromCartRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

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
                .uri(String.format("/api/v1/cartService/cart/%s/cartItem/%s", request.getUserId(), request.getId()))
                .retrieve().toEntity(String.class).block();

        return response;
    }
}
