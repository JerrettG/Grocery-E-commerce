package com.gonsalves.customerprofileservice.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.gonsalves.customerprofileservice.entity.CustomerProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerProfileRepository {

    @Autowired
    DynamoDBMapper dynamoDBMapper;

    public CustomerProfile loadCustomerByUserId(String userId) {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(userId);
        DynamoDBQueryExpression<CustomerProfile> queryExpression = new DynamoDBQueryExpression<CustomerProfile>()
                .withIndexName("user_id-index")
                .withHashKeyValues(profile)
                .withConsistentRead(false);
        List<CustomerProfile> results = dynamoDBMapper.query(CustomerProfile.class, queryExpression);
        if (results.size() > 0)
            return results.get(0);
        return null;
    }

    public void createCustomerProfile(CustomerProfile profile) {
        dynamoDBMapper.save(profile);
    }

    public void updateCustomerProfile(CustomerProfile profile) {
        dynamoDBMapper.save(profile,
                new DynamoDBSaveExpression()
                        .withExpectedEntry(
                                "id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(profile.getId())))
                        .withExpectedEntry(
                                "user_id",
                                new ExpectedAttributeValue(
                                        new AttributeValue(profile.getUserId())
                                )
                        )
        );
    }

    public void deleteCustomerProfile(CustomerProfile profile) {
         dynamoDBMapper.delete(profile);
    }
}
