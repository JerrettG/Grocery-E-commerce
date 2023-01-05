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
    private String paymentIntentId;
    private String userId;
    private AddressInfo shippingInfo;
    private AddressInfo billingInfo;
    private Double subtotal;
    private Double tax;
    private Double shippingCost;
    private Double total;
    private String status;
    private List<OrderItem> orderItems;
    private String createdDate;

}
