package com.gonsalves.cartservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemResponse {

    private String id;
    private String userId;
    private int quantity;
    private String productName;
    private String productImageUrl;
    private double productPrice;

}
