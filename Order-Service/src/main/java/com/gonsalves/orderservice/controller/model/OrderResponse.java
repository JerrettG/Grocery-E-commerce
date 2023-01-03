package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Address;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;
    private String userId;
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
