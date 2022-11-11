package com.gonsalves.productservice.service.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Product {

    private String productId;
    private String name;
    private double price;
    private String description;
    private String category;
    private String imageUrl;
    private double rating;

}
