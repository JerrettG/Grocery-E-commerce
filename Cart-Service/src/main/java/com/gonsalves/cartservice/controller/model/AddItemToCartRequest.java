package com.gonsalves.cartservice.controller.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemToCartRequest {

    private String userId;
    private int quantity;
    private String productId;
    private String productName;
    private String productImageUrl;
    private double productPrice;
}
