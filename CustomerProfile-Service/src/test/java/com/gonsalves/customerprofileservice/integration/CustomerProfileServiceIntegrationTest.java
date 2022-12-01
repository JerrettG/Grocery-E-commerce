package com.gonsalves.customerprofileservice.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.customerprofileservice.IntegrationTest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileCreateRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileUpdateRequest;
import com.gonsalves.customerprofileservice.integration.configuration.DynamoDBMapperTestConfiguration;
import com.gonsalves.customerprofileservice.integration.configuration.DynamoDBTestConfiguration;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import com.gonsalves.customerprofileservice.service.CustomerProfileService;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({DynamoDBTestConfiguration.class, DynamoDBMapperTestConfiguration.class})
@IntegrationTest
public class CustomerProfileServiceIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerProfileRepository customerProfileRepository;
    @Autowired
    AmazonDynamoDB amazonDynamoDB;

    private final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();
    private CustomerProfileEntity customerProfileEntity;


    @BeforeEach
    protected void setup() {
        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("user_id").withAttributeType("S"));

        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH));

        CreateTableRequest request = new CreateTableRequest()
                .withTableName("Ecommerce-CustomerProfileService-Profiles")
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));
        try {
            CreateTableResult response = amazonDynamoDB.createTable(request);
        } catch (AmazonDynamoDBException e) {
            System.out.println(e.getMessage());
        }


        customerProfileEntity = CustomerProfileEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email(mockNeat.emails().valStr())
                .firstName(mockNeat.names().first().valStr())
                .lastName(mockNeat.names().last().valStr())
                .shippingAddress(mockNeat.addresses().valStr())
                .status(Status.ACTIVE)
                .build();
        customerProfileRepository.createCustomerProfile(customerProfileEntity);

    }

    @AfterEach
    public void cleanUp() {
        DeleteTableRequest request = new DeleteTableRequest();
        request.setTableName("Ecommerce-CustomerProfileService-Profiles");

        amazonDynamoDB.deleteTable(request);
    }

    @Test
    public void getCustomerProfileByUserId_profileExists_returnsCorrectProfile() throws Exception {
        //GIVEN
        String userId = customerProfileEntity.getUserId();
        String expectedFirstName = customerProfileEntity.getFirstName();
        String expectedLastName = customerProfileEntity.getLastName();
        String expectedEmail = customerProfileEntity.getEmail();
        String expectedShippingAddress = customerProfileEntity.getShippingAddress();
        //WHEN
        mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(expectedFirstName))
                .andExpect(jsonPath("$.lastName").value(expectedLastName))
                .andExpect(jsonPath("$.email").value(expectedEmail))
                .andExpect(jsonPath("$.shippingAddress").value(expectedShippingAddress));
    }
    @Test
    public void getCustomerProfileByUserId_profileDoesNotExist_doesNotReturnProfile() throws Exception{
        //GIVEN
        String invalidUserId = UUID.randomUUID().toString();

        //WHEN
        mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", invalidUserId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void createCustomerProfile_profileDoesNotExist_createsProfile() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String email = mockNeat.emails().valStr();
        String firstName = mockNeat.names().first().valStr();
        String lastName = mockNeat.names().last().valStr();
        String shippingAddress = mockNeat.addresses().valStr();

        CustomerProfileCreateRequest createRequest = new CustomerProfileCreateRequest(
                userId,
                email,
                firstName,
                lastName,
                shippingAddress
        );
        //WHEN
        mockMvc.perform(post("/api/v1/customerProfileService/user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
        //THEN
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.shippingAddress").value(shippingAddress));
    }
    @Test
    public void createCustomerProfile_profileExists_doesNotCreateProfile() throws Exception {
        //GIVEN
        String existingUserId = customerProfileEntity.getUserId();
        String existingEmail = customerProfileEntity.getEmail();
        String existingFirstName = customerProfileEntity.getFirstName();
        String existingLastName = customerProfileEntity.getLastName();
        String existingShippingAddress = customerProfileEntity.getShippingAddress();

        CustomerProfileCreateRequest createRequest = new CustomerProfileCreateRequest(
                existingUserId,
                existingEmail,
                existingFirstName,
                existingLastName,
                existingShippingAddress
        );
        //WHEN
        mockMvc.perform(post("/api/v1/customerProfileService/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
                //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateCustomerProfile_profileExists_updatesProfile() throws Exception{
        //GIVEN
        String existingUserId = customerProfileEntity.getUserId();
        String updatedEmail = mockNeat.emails().valStr();
        String existingFirstName = customerProfileEntity.getFirstName();
        String existingLastName = customerProfileEntity.getLastName();
        String updatedShippingAddress = mockNeat.addresses().valStr();

        CustomerProfileUpdateRequest updateRequest = new CustomerProfileUpdateRequest(
                existingUserId,
                updatedEmail,
                existingFirstName,
                existingLastName,
                updatedShippingAddress
        );
        //WHEN
        mockMvc.perform(put("/api/v1/customerProfileService/user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isAccepted());
        mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", existingUserId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(updatedEmail))
                .andExpect(jsonPath("$.firstName").value(existingFirstName))
                .andExpect(jsonPath("$.lastName").value(existingLastName))
                .andExpect(jsonPath("$.shippingAddress").value(updatedShippingAddress));
    }

    @Test
    public void updateCustomerProfile_profileDoesNotExist_doesNotUpdateProfile() throws Exception{
        //GIVEN
        String invalidUserId = UUID.randomUUID().toString();
        String updatedEmail = mockNeat.emails().valStr();
        String firstName = customerProfileEntity.getFirstName();
        String lastName = customerProfileEntity.getLastName();
        String updatedShippingAddress = mockNeat.addresses().valStr();

        CustomerProfileUpdateRequest updateRequest = new CustomerProfileUpdateRequest(
                invalidUserId,
                updatedEmail,
                firstName,
                lastName,
                updatedShippingAddress
        );
        //WHEN
        mockMvc.perform(put("/api/v1/customerProfileService/user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteCustomerProfile_existingProfileEraseDataFalse_profileStatusChangedToInactive() throws Exception {
        //GIVEN
        String existingUserId = customerProfileEntity.getUserId();
        //WHEN
        mockMvc.perform(delete("/api/v1/customerProfileService/user/{userId}",existingUserId)
                .param("eraseData", "false")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isAccepted());
        mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", existingUserId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
    @Test
    public void deleteCustomerProfile_existingProfileEraseDataTrue_profileDeleted() throws Exception {
        //GIVEN
        String existingUserId = customerProfileEntity.getUserId();
        //WHEN
        mockMvc.perform(delete("/api/v1/customerProfileService/user/{userId}",existingUserId)
                        .param("eraseData", "true")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isAccepted());
        mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", existingUserId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteCustomerProfile_notExistingProfileEraseDataFalse_noChange() throws Exception {
        //GIVEN
        String invalidUserId = UUID.randomUUID().toString();
        //WHEN
        mockMvc.perform(delete("/api/v1/customerProfileService/user/{userId}",invalidUserId)
                        .param("eraseData", "false")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isBadRequest());
    }
    @Test
    public void deleteCustomerProfile_notExistingProfileEraseDataTrue_noChange() throws Exception {
        //GIVEN
        String invalidUserId = UUID.randomUUID().toString();
        //WHEN
        mockMvc.perform(delete("/api/v1/customerProfileService/user/{userId}",invalidUserId)
                        .param("eraseData", "true")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isBadRequest());
    }
}
