package com.gonsalves.orderservice.service.model;

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
    private String userId;
    private String id;
    private String paymentIntentId;
    private String shippingAddress;
    private double orderTotal;
    private String status;
    private List<OrderItem> orderItems;
    private String createdDate;
}