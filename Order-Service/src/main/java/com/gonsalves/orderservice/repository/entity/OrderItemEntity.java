package com.gonsalves.orderservice.repository.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class OrderItemEntity {

    @DynamoDBAttribute(attributeName = "name")
    private String itemName;
    @DynamoDBAttribute(attributeName = "image_url")
    private String imageUrl;
    @DynamoDBAttribute(attributeName = "quantity")
    private int quantity;
    @DynamoDBAttribute(attributeName = "unit_price")
    private double unitPrice;


}
