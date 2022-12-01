package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest implements OrderRequest{

    private String userId;
    private String paymentIntentId;
    private String shippingAddress;
    private double orderTotal;
    private String status;
    private List<OrderItem> orderItems;

}
