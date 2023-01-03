package com.gonsalves.grocery.frontend.frontend.model;


import java.util.ArrayList;
import java.util.List;
public class CartItemConverter {

    public static List<OrderItem> convert(List<CartItem> cartItems ) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .itemName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getProductPrice())
                    .imageUrl(item.getProductImageUrl())
                    .build();
            orderItems.add(orderItem);
        }
        return orderItems;



    }

}