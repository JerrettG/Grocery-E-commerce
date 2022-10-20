package com.gonsalves.CartService.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
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
    public Cart(CartItem[] cartItems) {
        this.cartItems = new ArrayList(Arrays.asList(cartItems));
        this.subtotal = 0d;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }
    public void setCartItems(List<CartItem> cartItems) {
    this.cartItems = cartItems;
    }
    public void calculateTotalCost() {
        double total = 0;
        for (CartItem cartItem : this.cartItems)
            total += cartItem.getProductPrice() * cartItem.getQuantity();
        this.subtotal = total;
    }
    public double getSubtotal() {return this.subtotal;
    }
    public void setSubtotal(double subtotal) {this.subtotal = subtotal;}
}
