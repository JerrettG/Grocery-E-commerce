package com.gonsalves.productservice.repository.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class CategoryConverter implements DynamoDBTypeConverter<String, Category> {


    @Override
    public String convert(Category category) {
        return category.toString();
    }

    @Override
    public Category unconvert(String s) {
        return Category.valueOf(s);
    }
}
