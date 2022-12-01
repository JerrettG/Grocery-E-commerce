package com.gonsalves.cartservice.controller.model;


import com.gonsalves.cartservice.service.model.CartItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class CartResponse {
    private List<CartItem> cartItems;
    private double subtotal;

    public CartResponse() {
        this.cartItems = new ArrayList<>();
        this.subtotal = 0d;
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
