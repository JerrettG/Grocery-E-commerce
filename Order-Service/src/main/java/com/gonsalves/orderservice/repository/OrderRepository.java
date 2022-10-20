package com.gonsalves.orderservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.orderservice.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public List<Order> getAllOrdersByUserId(String userId) {
        Order order = new Order();
        order.setUserId(userId);
        DynamoDBQueryExpression<Order> queryExpression = new DynamoDBQueryExpression<Order>()
                .withIndexName("user_id-payment_intent_id-index")
                .withHashKeyValues(order)
                .withConsistentRead(false);
        List<Order> results = dynamoDBMapper.query(Order.class, queryExpression);
        return results;
    }

    public Order getOrderByOrderId(String orderId, String userId) {
        return dynamoDBMapper.load(Order.class, orderId, userId);
    }

    public List<Order> getOrderByPaymentIntentId(String userId, String paymentIntentId) {
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#user_id", "user_id");
        expressionAttributeNames.put("#payment_intent_id", "payment_intent_id");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":user_id", new AttributeValue(userId));
        expressionAttributeValues.put(":payment_intent_id", new AttributeValue(paymentIntentId));

        DynamoDBQueryExpression<Order> queryExpression = new DynamoDBQueryExpression<Order>()
                .withIndexName("user_id-payment_intent_id-index")
                .withKeyConditionExpression("#user_id = :user_id and #payment_intent_id = :payment_intent_id")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Order.class, queryExpression);
    }

    public void createOrder(Order order) {
        dynamoDBMapper.save(order);
    }

    public void updateOrder(Order order) {
        dynamoDBMapper.save(order,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(order.getId())))
                        .withExpectedEntry(
                                "user_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(order.getUserId())
                                )
                        )
        );
    }

    public void deleteOrder(Order order) {
        dynamoDBMapper.delete(order);
    }

}