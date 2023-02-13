package com.gonsalves.orderservice.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.orderservice.IntegrationTest;
import com.gonsalves.orderservice.controller.model.OrderCreateRequest;
import com.gonsalves.orderservice.controller.model.OrderResponse;
import com.gonsalves.orderservice.controller.model.OrderUpdateRequest;
import com.gonsalves.orderservice.controller.model.OrdersListResponse;
import com.gonsalves.orderservice.integration.configuration.DynamoDBMapperTestConfiguration;
import com.gonsalves.orderservice.integration.configuration.DynamoDBTestConfiguration;
import com.gonsalves.orderservice.repository.OrderRepository;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.OrderItemEntity;
import com.gonsalves.orderservice.repository.entity.Status;
import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.OrderItem;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.*;

@Import({DynamoDBTestConfiguration.class, DynamoDBMapperTestConfiguration.class})
@IntegrationTest
public class OrderServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private Utility utility;

    private final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    AmazonDynamoDB amazonDynamoDB;


    @BeforeEach
    protected void setup() {
        utility = new Utility(mockMvc);
        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("order_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("user_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("payment_intent_id").withAttributeType("S"));
        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("order_id").withKeyType(KeyType.RANGE));

        List<KeySchemaElement> lsiKeySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("payment_intent_id").withKeyType(KeyType.RANGE));
        LocalSecondaryIndex lsi = new LocalSecondaryIndex()
                .withIndexName(OrderEntity.PAYMENT_INTENT_ID_INDEX)
                .withKeySchema(lsiKeySchema)
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
        CreateTableRequest request = new CreateTableRequest()
                .withTableName("Ecommerce-OrderService-Orders")
                .withKeySchema(keySchema)
                .withLocalSecondaryIndexes(lsi)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));
        try {
            CreateTableResult response = amazonDynamoDB.createTable(request);
        } catch (AmazonDynamoDBException e) {
            System.out.println(e.getMessage());
        }

    }


    @Test
    public void getAllOrderForUserId_returnsOrdersForThatUser() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String paymentId = UUID.randomUUID().toString();
        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId,
                paymentId,
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING.toString(),
                Arrays.asList(new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                ))
        );
        utility.orderServiceClient.createOrder(createRequest).andExpect(status().isCreated());

        //WHEN
        utility.orderServiceClient.getAllOrdersForUserId(userId)
        //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("orderList.length()").value(1)
                );
    }

    @Test
    public void getOrderByOrderId_existingOrder_returnsCorrectOrder() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId,
                UUID.randomUUID().toString(),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING.toString(),
                Arrays.asList(new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                ))
        );

        String jsonResponse = utility.orderServiceClient.createOrder(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        OrderResponse createResponse = mapper.readValue(jsonResponse, OrderResponse.class);
        String orderId = createResponse.getId();
        //WHEN
        utility.orderServiceClient.getOrderByOrderId(orderId, userId)
                //THEN
                .andExpectAll(status().isOk());

    }
    @Test
    public void getOrderByOrderId_existingOrder_responseNotFound() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        //WHEN
        utility.orderServiceClient.getOrderByOrderId(orderId,userId)
                //THEN
                .andExpect(status().isNotFound());
    }


    @Test
    public void createOrder_notExistingOrder_createsOrder() throws Exception{
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String paymentIntentId = UUID.randomUUID().toString();
        AddressInfo shippingInfo = new AddressInfo();
        AddressInfo billingInfo = new AddressInfo();
        double orderTotal = mockNeat.doubles().val();
        String status = Status.PROCESSING.toString();
        List<OrderItem> orderItems = Arrays.asList(new OrderItem(
                mockNeat.names().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.ints().range(1,10).val(),
                mockNeat.doubles().val()
        ));
        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId, paymentIntentId, shippingInfo, billingInfo,
                mockNeat.doubles().val(), mockNeat.doubles().val(), mockNeat.doubles().val(),
                orderTotal, status, orderItems
        );

        //WHEN
        utility.orderServiceClient.createOrder(createRequest)
        //THEN
                .andExpect(status().isCreated());
        utility.orderServiceClient.getAllOrdersForUserId(userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderList.length()").value(1));
    }

    @Test
    public void createOrder_existingOrder_doesNotCreateOrder() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String paymentId = UUID.randomUUID().toString();
        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId,
                paymentId,
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING.toString(),
                Arrays.asList(new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                ))
        );
        utility.orderServiceClient.createOrder(createRequest).andExpect(status().isCreated());

        //WHEN
        utility.orderServiceClient.createOrder(createRequest)
        //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateOrder_existingOrder_updatesOrder() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId,
                UUID.randomUUID().toString(),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING.toString(),
                Arrays.asList(new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                ))
        );

        String jsonResponse = utility.orderServiceClient.createOrder(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        OrderResponse createResponse = mapper.readValue(jsonResponse, OrderResponse.class);
        String orderId = createResponse.getId();
        String updatedStatus = Status.OUT_FOR_DELIVERY.toString();
        OrderUpdateRequest updateRequest = new OrderUpdateRequest(
                orderId,
                userId,
                createResponse.getShippingInfo(),
                createResponse.getBillingInfo(),
                createResponse.getTotal(),
                updatedStatus,
                Arrays.asList(
                        new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                        ),
                        new OrderItem(
                                mockNeat.names().valStr(),
                                mockNeat.urls().valStr(),
                                mockNeat.ints().range(1,10).val(),
                                mockNeat.doubles().val()
                        )
                )

        );
        //WHEN
        utility.orderServiceClient.updateOrder(updateRequest)
        //THEN
                .andExpect(status().isAccepted());
        utility.orderServiceClient.getOrderByOrderId(orderId, userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(orderId))
                .andExpect(jsonPath("status").value(updatedStatus))
                .andExpect(jsonPath("orderItems.length()").value(2));
    }

    @Test
    public void updateOrder_notExistingOrder_doesNotUpdateOrder() throws Exception{
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String updatedStatus = "SHIPPING";
        String invalidOrderId = UUID.randomUUID().toString();
        OrderUpdateRequest updateRequest = new OrderUpdateRequest(
                invalidOrderId,
                userId,
                new AddressInfo(),
                new AddressInfo(),
                mockNeat.doubles().val(),
                updatedStatus,
                Arrays.asList(
                        new OrderItem(
                                mockNeat.names().valStr(),
                                mockNeat.urls().valStr(),
                                mockNeat.ints().range(1,10).val(),
                                mockNeat.doubles().val()
                        ),
                        new OrderItem(
                                mockNeat.names().valStr(),
                                mockNeat.urls().valStr(),
                                mockNeat.ints().range(1,10).val(),
                                mockNeat.doubles().val()
                        )
                )

        );
        //WHEN
        utility.orderServiceClient.updateOrder(updateRequest)
        //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteOrder_existingOrder_deletesOrder() throws Exception{
        //GIVEN
        String userId = UUID.randomUUID().toString();
        //GIVEN
        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId,
                UUID.randomUUID().toString(),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                new AddressInfo(
                        mockNeat.names().first().valStr(),
                        mockNeat.names().last().valStr(),
                        mockNeat.addresses().line1().valStr(),
                        mockNeat.addresses().line2().valStr(),
                        mockNeat.cities().us().valStr(),
                        mockNeat.usStates().valStr(),
                        mockNeat.ints().valStr()
                ),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING.toString(),
                Arrays.asList(new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                ))
        );

        String jsonResponse = utility.orderServiceClient.createOrder(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        OrderResponse createResponse = mapper.readValue(jsonResponse, OrderResponse.class);
        String orderId = createResponse.getId();
        //WHEN
        utility.orderServiceClient.deleteOrder(orderId, userId)
        //THEN
                .andExpect(status().isAccepted());
        utility.orderServiceClient.getAllOrdersForUserId(userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("orderList.length()").value(0));

    }
    @Test
    public void deleteOrder_notExistingOrder_doesNotDeleteOrder() throws Exception{
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String invalidOrderId = UUID.randomUUID().toString();
        //WHEN
        utility.orderServiceClient.deleteOrder(invalidOrderId, userId)
        //THEN
                .andExpect(status().isNotFound());
    }

}
