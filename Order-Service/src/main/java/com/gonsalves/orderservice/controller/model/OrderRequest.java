package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.OrderItem;

import java.util.List;

public interface OrderRequest {

    String getUserId();
    void setUserId(String userId);
    AddressInfo getShippingInfo();
    void setShippingInfo(AddressInfo shippingInfo);
    AddressInfo getBillingInfo();
    void setBillingInfo(AddressInfo billingInfo);
    Double getTotal();
    void setTotal(Double total);
    String getStatus();
    void setStatus(String status);
    List<OrderItem> getOrderItems();
    void setOrderItems(List<OrderItem> orderItems);
}
