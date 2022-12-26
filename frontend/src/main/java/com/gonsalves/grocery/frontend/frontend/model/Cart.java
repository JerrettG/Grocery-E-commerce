package com.gonsalves.grocery.frontend.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@JsonPropertyOrder({"cartItems", "subtotal"})
public class Cart {
    @JsonProperty("cartItems")
    private List<CartItem> cartItems;
    @JsonProperty("subtotal")
    private double subtotal;

    public Cart() {
        this.cartItems = new ArrayList<>();
        this.subtotal = 0d;
    }

}
