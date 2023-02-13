package com.gonsalves.productservice.repository;


import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.productservice.repository.entity.Category;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepository {

    
    private final DynamoDBMapper mapper;
    @Autowired
    public ProductRepository(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<ProductEntity> loadProductWithProductName(String name) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(name);
        DynamoDBQueryExpression<ProductEntity> queryExpression = new DynamoDBQueryExpression<ProductEntity>()
                .withIndexName(ProductEntity.NAME_INDEX)
                .withHashKeyValues(productEntity)
                .withConsistentRead(false);

        return mapper.query(ProductEntity.class, queryExpression).stream().findFirst();
    }

    public List<ProductEntity> loadAll() {
        return mapper.scan(ProductEntity.class, new DynamoDBScanExpression());
    }

    public List<ProductEntity> loadAllProductsInCategory(Category category) {
        Map<String, AttributeValue> valueMap = new HashMap<>();
        valueMap.put(":category", new AttributeValue().withS(category.toString()));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("category = :category")
                .withExpressionAttributeValues(valueMap);
        return mapper.scan(ProductEntity.class, scanExpression);
    }

    public void create(ProductEntity productEntity) {
        mapper.save(productEntity);
    }

    public void update(ProductEntity productEntity) {
        mapper.save(productEntity,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(productEntity.getProductId())))
                        .withExpectedEntry(
                                "name",
                                new ExpectedAttributeValue(
                                        new AttributeValue(productEntity.getName())
                                )
                        )
        );
    }
    public void delete(ProductEntity productEntity) {
        mapper.delete(productEntity);
    }
}
