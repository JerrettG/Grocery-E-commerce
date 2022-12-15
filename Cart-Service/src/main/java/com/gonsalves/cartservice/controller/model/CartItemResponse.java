package com.gonsalves.cartservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private String id;
    private String userId;
    private int quantity;
    private String productName;
    private String productImageUrl;
    private double productPrice;

}
