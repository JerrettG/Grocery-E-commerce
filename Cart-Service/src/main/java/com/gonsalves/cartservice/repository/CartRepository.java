package com.gonsalves.cartservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CartRepository {
    
    private final DynamoDBMapper mapper;

    @Autowired
    public CartRepository(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    //CREATE
    public void create(CartItemEntity cartItemEntity) {
        mapper.save(cartItemEntity);
    }

    //READ
    public List<CartItemEntity> loadAllCartItems(String userId) {
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setUserId(userId);
        DynamoDBQueryExpression<CartItemEntity> queryExpression = new DynamoDBQueryExpression<CartItemEntity>()
                .withHashKeyValues(cartItemEntity)
                .withConsistentRead(false);
        return mapper.query(CartItemEntity.class, queryExpression);
    }

    public List<CartItemEntity> loadCartItem(String userId, String productId) {

        Map<String, AttributeValue> expected = new HashMap<String, AttributeValue>();
        expected.put(":user_id", new AttributeValue(userId));
        expected.put(":product_id", new AttributeValue(productId));

        DynamoDBQueryExpression<CartItemEntity> queryExpression = new DynamoDBQueryExpression<CartItemEntity>()
                .withIndexName(CartItemEntity.PRODUCT_ID_INDEX)
                .withKeyConditionExpression("user_id = :user_id and product_id = :product_id")
                .withExpressionAttributeValues(expected)
                .withConsistentRead(false);

        return mapper.query(CartItemEntity.class, queryExpression);
    }

    //UPDATE
    public void updateCartItem(CartItemEntity cartItemEntity) {
        mapper.save(cartItemEntity,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "user_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(cartItemEntity.getUserId())
                                )
                        )
                        .withExpectedEntry(
                                "item_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(cartItemEntity.getId())))

        );
    }

    //DELETE
    public void removeItem(CartItemEntity cartItemEntity) {mapper.delete(cartItemEntity);
    }

    public void clearCart(String userId) {
        List<CartItemEntity> resources = loadAllCartItems(userId);
        mapper.batchDelete(resources);
    }
}
