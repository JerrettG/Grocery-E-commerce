package com.gonsalves.productservice.controller.model;

import com.gonsalves.productservice.service.model.Product;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductListResponse {

    List<Product> productList;

    public ProductListResponse() {
        productList = new ArrayList<>();
    }
    public ProductListResponse(List<Product> productList) {
        this.productList = productList;
    }
}
