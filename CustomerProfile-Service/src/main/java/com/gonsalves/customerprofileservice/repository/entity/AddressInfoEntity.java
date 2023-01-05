package com.gonsalves.customerprofileservice.repository.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class AddressInfoEntity {
    @DynamoDBAttribute(attributeName = "first_name")
    private String firstName;
    @DynamoDBAttribute(attributeName = "last_name")
    private String lastName;
    @DynamoDBAttribute(attributeName = "address_first_line")
    private String addressFirstLine;
    @DynamoDBAttribute(attributeName = "address_second_line")
    private String addressSecondLine;
    @DynamoDBAttribute(attributeName = "city")
    private String city;
    @DynamoDBAttribute(attributeName = "state")
    private String state;
    @DynamoDBAttribute(attributeName = "zip_code")
    private String zipCode;
}