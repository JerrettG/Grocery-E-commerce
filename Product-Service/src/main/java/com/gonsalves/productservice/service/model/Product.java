package com.gonsalves.productservice.service.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Product {

    private String productId;
    private String name;
    private Double price;
    private String unitMeasurement;
    private String description;
    private String category;
    private String imageUrl;
    private Double rating;

}
