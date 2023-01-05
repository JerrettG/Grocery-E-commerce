package com.gonsalves.cartservice.service.model;

import com.gonsalves.cartservice.repository.entity.CartItemEntity;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItemEntity> cartItemEntities;
    private Double subtotal;

    public Cart() {
        this.cartItemEntities = new ArrayList<>();
        this.subtotal = 0d;
    }

    public List<CartItemEntity> getCartItems() {
        return cartItemEntities;
    }
    public void setCartItems(List<CartItemEntity> cartItemEntities) {
        this.cartItemEntities = cartItemEntities;
    }
    public void calculateTotalCost() {
        Double total = 0.0;
        for (CartItemEntity cartItemEntity : this.cartItemEntities)
            total += cartItemEntity.getProductPrice() * cartItemEntity.getQuantity();
        this.subtotal = total;
    }
    public Double getSubtotal() {return this.subtotal;
    }
    public void setSubtotal(Double subtotal) {this.subtotal = subtotal;}
}
