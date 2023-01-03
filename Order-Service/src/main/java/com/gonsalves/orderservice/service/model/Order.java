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
    private String id;
    private String userId;
    private String paymentIntentId;
    private AddressInfo shippingInfo;
    private AddressInfo billingInfo;
    private double subtotal;
    private double tax;
    private double shippingCost;
    private double total;
    private String status;
    private List<OrderItem> orderItems;
    private String createdDate;
}
