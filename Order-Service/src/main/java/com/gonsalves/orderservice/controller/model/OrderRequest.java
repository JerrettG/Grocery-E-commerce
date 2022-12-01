package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.service.model.OrderItem;

import java.util.List;

public interface OrderRequest {

    String getUserId();
    void setUserId(String userId);
    String getShippingAddress();
    void setShippingAddress(String shippingAddress);
    double getOrderTotal();
    void setOrderTotal(double orderTotal);
    String getStatus();
    void setStatus(String status);
    List<OrderItem> getOrderItems();
    void setOrderItems(List<OrderItem> orderItems);
}
