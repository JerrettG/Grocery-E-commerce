package com.gonsalves.UI.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {


    private String id;
    private String userId;
    private String paymentIntentId;
    private String shippingAddress;
    private double orderTotal;
    private String status;private List<OrderItem> orderItems;
    private String createdDate;


    public void calculateAndSetOrderTotal() {
        double total = 0;
        for (OrderItem orderItem: orderItems) {
            total += orderItem.getQuantity()*orderItem.getUnitPrice();
        }
        orderTotal = total;
    }

}