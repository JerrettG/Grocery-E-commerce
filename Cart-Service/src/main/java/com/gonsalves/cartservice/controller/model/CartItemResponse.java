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
    private Integer quantity;
    private String productName;
    private String productImageUrl;
    private Double productPrice;

}
