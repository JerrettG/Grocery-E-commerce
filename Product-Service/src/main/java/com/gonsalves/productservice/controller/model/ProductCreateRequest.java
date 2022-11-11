package com.gonsalves.productservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest implements ProductRequest{

    private String name;
    private double price;
    private String description;
    private String category;
    private String imageUrl;

}
