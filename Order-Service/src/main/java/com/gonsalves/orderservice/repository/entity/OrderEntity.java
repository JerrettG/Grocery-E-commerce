package com.gonsalves.orderservice.repository.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Ecommerce-OrderService-Orders")
public class OrderEntity {
    public static final String PAYMENT_INTENT_ID_INDEX = "payment_intent_id-index";
    @DynamoDBHashKey(attributeName = "user_id")
    private String userId;
    @DynamoDBRangeKey(attributeName = "order_id")
    private String id;
    @DynamoDBIndexRangeKey(localSecondaryIndexName = PAYMENT_INTENT_ID_INDEX, attributeName = "payment_intent_id")
    private String paymentIntentId;
    @DynamoDBAttribute(attributeName = "shipping_info")
    private AddressInfoEntity shippingInfo;
    @DynamoDBAttribute(attributeName = "billing_info")
    private AddressInfoEntity billingInfo;
    @DynamoDBAttribute(attributeName = "subtotal")
    private Double subtotal;
    @DynamoDBAttribute(attributeName = "tax")
    private Double tax;
    @DynamoDBAttribute(attributeName = "shipping_cost")
    private Double shippingCost;
    @DynamoDBAttribute(attributeName = "total")
    private Double total;
    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "status")
    private Status status;
    @DynamoDBAttribute(attributeName = "items")
    private List<OrderItemEntity> orderItemEntities;
    @DynamoDBAttribute(attributeName = "created_date")
    private String createdDate;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity orderEntity = (OrderEntity) o;
        return Objects.equals(id, orderEntity.id) && Objects.equals(userId, orderEntity.userId) && Objects.equals(paymentIntentId, orderEntity.paymentIntentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, paymentIntentId);
    }
}