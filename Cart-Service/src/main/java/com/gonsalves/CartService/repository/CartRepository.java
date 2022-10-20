package com.gonsalves.CartService.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.CartService.entity.Cart;
import com.gonsalves.CartService.entity.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CartRepository {
    @Autowired
    DynamoDBMapper dynamoDBMapper;

    //CREATE
    public void create(CartItem cartItem) {
        dynamoDBMapper.save(cartItem);
    }

    //READ
    public List<CartItem> loadAllCartItems(String userId) {
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        DynamoDBQueryExpression<CartItem> queryExpression = new DynamoDBQueryExpression<CartItem>()
                .withIndexName("user_id-product_id-index")
                .withHashKeyValues(cartItem)
                .withConsistentRead(false);
        return dynamoDBMapper.query(CartItem.class, queryExpression);
    }

    public List<CartItem> loadCartItem(String userId, String productId) {
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#user_id", "user_id");
        expressionAttributeNames.put("#product_id", "product_id");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":user_id", new AttributeValue(userId));
        expressionAttributeValues.put(":product_id", new AttributeValue(productId));

        DynamoDBQueryExpression<CartItem> queryExpression = new DynamoDBQueryExpression<CartItem>()
                .withIndexName("user_id-product_id-index")
                .withKeyConditionExpression("#user_id = :user_id and #product_id = :product_id")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConsistentRead(false);

        return dynamoDBMapper.query(CartItem.class, queryExpression);
    }

    //UPDATE
    public void updateCartItem(CartItem cartItem) {
        dynamoDBMapper.save(cartItem,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(cartItem.getId())))
                        .withExpectedEntry(
                                "product_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(cartItem.getProductId())
                                )
                        )
        );
    }

    //DELETE
    public void removeItem(CartItem cartItem) {dynamoDBMapper.delete(cartItem);
    }

    public void clearCart(String userId) {
        List<CartItem> resources = loadAllCartItems(userId);
        dynamoDBMapper.batchDelete(resources);
    }
}
