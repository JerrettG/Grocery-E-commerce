package com.gonsalves.cartservice.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String id;
    private String userId;
    private int quantity;
    private String productId;
    private String productName;
    private String productImageUrl;
    private double productPrice;
}
