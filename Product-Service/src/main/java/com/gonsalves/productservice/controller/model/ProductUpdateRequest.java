package com.gonsalves.productservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest implements ProductRequest{

    private String productId;
    private String name;
    private Double price;
    private String unitMeasurement;
    private String description;
    private String category;
    private String imageUrl;
    private Double rating;

}
