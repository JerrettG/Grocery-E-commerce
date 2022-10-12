package com.gonsalves.ProductService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductDTO {

    private String productId;
    private String name;
    private String price;
    private String imageUrl;
    private String category;


}
