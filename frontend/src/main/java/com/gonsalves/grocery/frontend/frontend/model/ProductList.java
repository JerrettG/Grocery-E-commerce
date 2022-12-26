package com.gonsalves.grocery.frontend.frontend.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ProductList {
    private List<Product> productList;

    public ProductList() {
        productList = new ArrayList<>();
    }
    public ProductList(List<Product> productList) {
        this.productList = productList;
    }
}
