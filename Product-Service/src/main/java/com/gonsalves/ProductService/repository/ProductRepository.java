package com.gonsalves.ProductService.repository;


import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.ProductService.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public List<Product> loadProductWithProductName(String name) {
        Product product = new Product();
        product.setName(name);
        DynamoDBQueryExpression<Product> queryExpression = new DynamoDBQueryExpression<Product>()
                .withIndexName("name-index")
                .withHashKeyValues(product)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Product.class, queryExpression);

    }

    public List<Product> loadAll() {
        return dynamoDBMapper.scan(Product.class, new DynamoDBScanExpression());
    }

    public void create(Product product) {
        dynamoDBMapper.save(product);
    }
    public void delete(Product product) {
        dynamoDBMapper.delete(product);
    }
    public void update(Product product) {
        dynamoDBMapper.save(product);
    }
}
