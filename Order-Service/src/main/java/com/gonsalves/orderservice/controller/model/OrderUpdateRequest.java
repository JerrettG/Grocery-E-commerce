package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderUpdateRequest implements OrderRequest{

    private String id;
    private String userId;
    private AddressInfo shippingInfo;
    private AddressInfo billingInfo;
    private Double total;
    private String status;
    private List<OrderItem> orderItems;

    public OrderUpdateRequest() {}

}
