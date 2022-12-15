package com.gonsalves.ui.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {


    private String id;

    private String userId;

    private int quantity;

    private String productId;


    private String productName;

    private String productImageUrl;

    private double productPrice;


}
