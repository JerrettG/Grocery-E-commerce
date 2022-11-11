package com.gonsalves.cartservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDBConfig {
    @Value("${dynamodb.override_endpoint}")
    String dynamoOverrideEndpoint;
    @Value("${GROCERY_AWS_REGION}")
    private String region;
    @Value("${GROCERY_AWS_ACCESS_KEY_ID}")
    private String accessKey;
    @Value("${GROCERY_AWS_SECRET_KEY}")
    private String secretKey;

    @Bean
    @ConditionalOnProperty(name = "dynamodb.override_endpoint", havingValue = "true")
    public AmazonDynamoDB amazonDynamoDB(@Value("${dynamodb.endpoint}") String dynamoEndpoint) {
        AwsClientBuilder.EndpointConfiguration endpointConfig = new
                AwsClientBuilder.EndpointConfiguration(dynamoEndpoint,
                "us-west-1");

        return AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfig)
                .build();

    }
    @Bean
    public AmazonDynamoDB buildAmazonDynamoDB() {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }
}