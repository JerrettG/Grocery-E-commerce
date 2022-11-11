package com.gonsalves.UI.model;


import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class Product {
    private String productId;
    private String name;
    private double price;
    private String description;
    private String category;
    private String imageUrl;
    private double rating;

    public Product(String name, double price, String description, String category, String imageUrl, double rating)
    {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }
}
