package com.gonsalves.customerprofileservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerProfileRepository {

    
    private final DynamoDBMapper mapper;
    @Autowired
    public CustomerProfileRepository(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }
    public Optional<CustomerProfileEntity> loadCustomerByUserId(String userId) {
        CustomerProfileEntity profile = new CustomerProfileEntity();
        profile.setUserId(userId);
        DynamoDBQueryExpression<CustomerProfileEntity> queryExpression = new DynamoDBQueryExpression<CustomerProfileEntity>()
                .withHashKeyValues(profile)
                .withConsistentRead(false);
        List<CustomerProfileEntity> results = mapper.query(CustomerProfileEntity.class, queryExpression);
        return results.stream().findFirst();
    }

    public void createCustomerProfile(CustomerProfileEntity profile) {
        mapper.save(profile);
    }

    public void updateCustomerProfile(CustomerProfileEntity profile) {
        mapper.save(profile,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "user_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(profile.getUserId())
                                )
                        )
        );
    }

    public void deleteCustomerProfile(CustomerProfileEntity profile) {
         mapper.delete(profile);
    }

}

