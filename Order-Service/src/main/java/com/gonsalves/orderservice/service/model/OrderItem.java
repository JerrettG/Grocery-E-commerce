package com.gonsalves.orderservice.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String itemName;
    private String imageUrl;
    private Integer quantity;
    private Double unitPrice;

}
