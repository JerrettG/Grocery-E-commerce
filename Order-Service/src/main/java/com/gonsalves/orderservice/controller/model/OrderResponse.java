package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;
    private String userId;
    private String shippingAddress;
    private double orderTotal;
    private String status;
    private List<OrderItem> orderItems;
    private String createdDate;

}
