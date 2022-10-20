package com.gonsalves.orderservice.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeToStringTypeConverter implements DynamoDBTypeConverter<String, String> {

    @Override
    public String convert(String localDateTime) {
        LocalDateTime now = LocalDateTime.now();
        return DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(now);
    }

    @Override
    public String unconvert(String s) {
        return s;
    }
}