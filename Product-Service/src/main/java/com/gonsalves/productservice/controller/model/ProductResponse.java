package com.gonsalves.productservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String productId;
    private String name;
    private double price;
    private String unitMeasurement;
    private String description;
    private String category;
    private String imageUrl;
    private double rating;
}