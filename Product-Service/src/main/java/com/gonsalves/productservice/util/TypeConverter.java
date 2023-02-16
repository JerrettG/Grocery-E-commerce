package com.gonsalves.productservice.util;

import com.gonsalves.productservice.repository.entity.Category;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import com.gonsalves.productservice.service.model.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class TypeConverter {
    private static final Gson gson = new GsonBuilder().create();

    public static <T> String toJson(T object) {
        return gson.toJson(object);
    }
    public static List<Product> fromJsonToProductList(String json) {
        return gson.fromJson(json, new TypeToken<ArrayList<Product>>() { }.getType());
    }

    public static Product fromJsonToProduct(String json) {
        return gson.fromJson(json, Product.class);
    }
    public static Product convertFromEntity(ProductEntity productEntity) {
        return Product.builder()
                .productId(productEntity.getProductId())
                .name(productEntity.getName())
                .price(productEntity.getPrice())
                .unitMeasurement(productEntity.getUnitMeasurement())
                .description(productEntity.getDescription())
                .category(productEntity.getCategory().toString())
                .imageUrl(productEntity.getImageUrl())
                .rating(productEntity.getRating())
                .build();
    }
    public static ProductEntity convertToEntity(Product product) {
        return ProductEntity.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .unitMeasurement(product.getUnitMeasurement())
                .description(product.getDescription())
                .category(Category.valueOf(product.getCategory()))
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                .build();
    }
}
