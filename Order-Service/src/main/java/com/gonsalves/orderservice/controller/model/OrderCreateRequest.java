package com.gonsalves.orderservice.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest{

    private String userId;
    private String paymentIntentId;
    private AddressInfo shippingInfo;
    private AddressInfo billingInfo;
    private Double subtotal;
    private Double tax;
    private Double shippingCost;
    private Double total;
    private String status;
    private List<OrderItem> orderItems;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public AddressInfo getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(AddressInfo shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public AddressInfo getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(AddressInfo billingInfo) {
        this.billingInfo = billingInfo;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
