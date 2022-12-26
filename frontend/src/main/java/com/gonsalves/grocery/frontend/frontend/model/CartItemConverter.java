package com.gonsalves.grocery.frontend.frontend.model;


import java.util.ArrayList;
import java.util.List;
public class CartItemConverter {

    public static Order convert(List<CartItem> cartItems, String customerId ) {
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
        return Order.builder()
                .userId(customerId)
                .orderItems(orderItems)
                .status("PROCESSING")
                .shippingAddress("1234 Main St, Cupertino, CA, 98765, USA")
                .build();



    }

}