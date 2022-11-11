package com.gonsalves.productservice.repository;


import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public List<ProductEntity> loadProductWithProductName(String name) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(name);
        DynamoDBQueryExpression<ProductEntity> queryExpression = new DynamoDBQueryExpression<ProductEntity>()
                .withIndexName("name-index")
                .withHashKeyValues(productEntity)
                .withConsistentRead(false);

        return dynamoDBMapper.query(ProductEntity.class, queryExpression);
    }

    public List<ProductEntity> loadAll() {
        return dynamoDBMapper.scan(ProductEntity.class, new DynamoDBScanExpression());
    }

    public void create(ProductEntity productEntity) {
        dynamoDBMapper.save(productEntity);
    }
    public void delete(ProductEntity productEntity) {
        dynamoDBMapper.delete(productEntity);
    }
    public void update(ProductEntity productEntity) {
        dynamoDBMapper.save(productEntity,
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
}
