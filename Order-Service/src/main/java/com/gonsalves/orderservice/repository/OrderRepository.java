package com.gonsalves.orderservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {


    private final DynamoDBMapper mapper;

    @Autowired
    public OrderRepository(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    public List<OrderEntity> getAllOrdersByUserId(String userId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        DynamoDBQueryExpression<OrderEntity> queryExpression = new DynamoDBQueryExpression<OrderEntity>()
                .withHashKeyValues(orderEntity)
                .withConsistentRead(false);
        return mapper.query(OrderEntity.class, queryExpression);
    }

    public OrderEntity getOrderByOrderId(String userId, String orderId) {
        return mapper.load(OrderEntity.class, userId, orderId);
    }

    public List<OrderEntity> getOrderByPaymentIntentId(String userId, String paymentIntentId) {
        Map<String, AttributeValue> expected     = new HashMap<String, AttributeValue>();
        expected.put(":user_id", new AttributeValue(userId));
        expected.put(":payment_intent_id", new AttributeValue(paymentIntentId));

        DynamoDBQueryExpression<OrderEntity> queryExpression = new DynamoDBQueryExpression<OrderEntity>()
                .withIndexName(OrderEntity.PAYMENT_INTENT_ID_INDEX)
                .withKeyConditionExpression("user_id = :user_id and payment_intent_id = :payment_intent_id")
                .withExpressionAttributeValues(expected)
                .withConsistentRead(false);

        return mapper.query(OrderEntity.class, queryExpression);
    }

    public void createOrder(OrderEntity orderEntity) {
        mapper.save(orderEntity);
    }

    public void updateOrder(OrderEntity orderEntity) {
        mapper.save(orderEntity,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "user_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(orderEntity.getUserId())
                                )
                        )
                        .withExpectedEntry(
                                "order_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(orderEntity.getId())
                                )
                        )

        );
    }

    public void deleteOrder(OrderEntity orderEntity) {
        mapper.delete(orderEntity);
    }

}