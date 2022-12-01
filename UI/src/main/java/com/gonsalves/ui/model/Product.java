package com.gonsalves.ui.model;


import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class Product {
    private String productId;
    private String name;
    private double price;
    private String unitMeasurement;
    private String description;
    private String category;
    private String imageUrl;
    private double rating;

    public Product(String name, double price, String unitMeasurement, String description, String category, String imageUrl, double rating)
    {
        this.name = name;
        this.price = price;
        this.unitMeasurement = unitMeasurement;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }
}
