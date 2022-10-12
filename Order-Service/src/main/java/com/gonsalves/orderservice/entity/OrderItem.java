package com.gonsalves.orderservice.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class OrderItem {

    @DynamoDBAttribute(attributeName = "item_name")
    private String itemName;
    @DynamoDBAttribute(attributeName = "item_quantity")
    private int quantity;
    @DynamoDBAttribute(attributeName = "item_unit_price")
    private double unitPrice;


}
