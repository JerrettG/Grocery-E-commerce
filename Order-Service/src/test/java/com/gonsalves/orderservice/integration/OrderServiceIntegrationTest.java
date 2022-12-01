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
    MockMvc mockMvc;
    @Autowired
    OrderRepository orderRepository;

    private final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    AmazonDynamoDB amazonDynamoDB;

    private OrderEntity orderEntity;
    private String orderId;

    @BeforeEach
    protected void setup() {
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
        List<OrderItemEntity> orderItemEntities = Arrays.asList(
                new OrderItemEntity(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1, 10).val(),
                        mockNeat.doubles().val()),
                new OrderItemEntity(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1, 10).val(),
                        mockNeat.doubles().val())
        );

        orderEntity = OrderEntity.builder()
                .userId(UUID.randomUUID().toString())
                .paymentIntentId(UUID.randomUUID().toString())
                .shippingAddress(mockNeat.addresses().valStr())
                .orderTotal(mockNeat.doubles().val())
                .status(Status.PROCESSING)
                .orderItemEntities(orderItemEntities)
                .build();

        orderRepository.createOrder(orderEntity);
        orderId = orderRepository.getOrderByPaymentIntentId(orderEntity.getUserId(), orderEntity.getPaymentIntentId()).get(0).getId();

    }

    @AfterEach
    public void cleanUp(){
        DeleteTableRequest request = new DeleteTableRequest();
        request.setTableName("Ecommerce-OrderService-Orders");

        amazonDynamoDB.deleteTable(request);
    }

    @Test
    public void getAllOrderForUserId_returnsOrdersForThatUser() throws Exception {
        //GIVEN
        String userId = orderEntity.getUserId();

        //WHEN
        String jsonResponse = mockMvc.perform(get(String.format("/api/v1/orderService/order/all/user/%s", userId))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        OrdersListResponse response = mapper.readValue(jsonResponse, OrdersListResponse.class);

        Assertions.assertEquals(1, response.getOrderList().size());
    }

    @Test
    public void getOrderByOrderId_existingOrder_returnsCorrectOrder() throws Exception {
        //GIVEN
        String userId = orderEntity.getUserId();
        String orderId = orderEntity.getId();
        //WHEN
        String jsonResponse = mockMvc.perform(get("/api/v1/orderService/order/{orderId}/user/{userId}", orderId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                //THEN
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        OrderResponse response = mapper.readValue(jsonResponse, OrderResponse.class);

        Assertions.assertEquals(orderId, response.getId());
        Assertions.assertEquals(userId, response.getUserId());
        Assertions.assertEquals(orderEntity.getCreatedDate(), response.getCreatedDate());
    }
    @Test
    public void getOrderByOrderId_existingOrder_responseNotFound() throws Exception {
        //GIVEN
        String userId = orderEntity.getUserId();
        String orderId = UUID.randomUUID().toString();
        //WHEN
         mockMvc.perform(get("/api/v1/orderService/order/{orderId}/user{userId}", orderId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                //THEN
                .andExpect(status().isNotFound());
    }


    @Test
    public void createOrder_notExistingOrder_createsOrder() throws Exception{
        //GIVEN
        String userId = orderEntity.getUserId();
        String paymentIntentId = UUID.randomUUID().toString();
        String shippingAddress = mockNeat.addresses().valStr();
        double orderTotal = mockNeat.doubles().val();
        String status = Status.PROCESSING.toString();
        List<OrderItem> orderItems = Arrays.asList(new OrderItem(
                mockNeat.names().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.ints().range(1,10).val(),
                mockNeat.doubles().val()
        ));

        OrderCreateRequest createRequest = new OrderCreateRequest(
                userId, paymentIntentId, shippingAddress,
                orderTotal, status, orderItems
        );
        //WHEN
        mockMvc.perform(post("/api/v1/orderService/order")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
        //THEN
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/orderService/order/all/user/{userId}",userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderList.length()").value(2));
    }

    @Test
    public void createOrder_existingOrder_doesNotCreateOrder() throws Exception {
        //GIVEN
        OrderCreateRequest createRequest = new OrderCreateRequest(
                orderEntity.getUserId(),
                orderEntity.getPaymentIntentId(),
                orderEntity.getShippingAddress(),
                orderEntity.getOrderTotal(),
                orderEntity.getStatus().toString(),
                Arrays.asList(new OrderItem(
                        mockNeat.names().valStr(),
                        mockNeat.urls().valStr(),
                        mockNeat.ints().range(1,10).val(),
                        mockNeat.doubles().val()
                ))
        );

        //WHEN
        mockMvc.perform(post("/api/v1/orderService/order")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
        //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateOrder_existingOrder_updatesOrder() throws Exception {
        //GIVEN
        String userId = orderEntity.getUserId();
        String updatedStatus = Status.OUT_FOR_DELIVERY.toString();
        OrderUpdateRequest updateRequest = new OrderUpdateRequest(
                orderId,
                userId,
                orderEntity.getShippingAddress(),
                orderEntity.getOrderTotal(),
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
        mockMvc.perform(put("/api/v1/orderService/order")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isAccepted());
        mockMvc.perform(get("/api/v1/orderService/order/{orderId}/user/{userId}", orderId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                //THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value(updatedStatus))
                .andExpect(jsonPath("$.orderItems.length()").value(2));
    }

    @Test
    public void updateOrder_notExistingOrder_doesNotUpdateOrder() throws Exception{
        //GIVEN
        String userId = orderEntity.getUserId();
        String updatedStatus = "SHIPPING";
        String invalidOrderId = UUID.randomUUID().toString();
        OrderUpdateRequest updateRequest = new OrderUpdateRequest(
                invalidOrderId,
                userId,
                orderEntity.getShippingAddress(),
                orderEntity.getOrderTotal(),
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
        mockMvc.perform(put("/api/v1/orderService/order")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteOrder_existingOrder_deletesOrder() throws Exception{
        //GIVEN
        String userId = orderEntity.getUserId();
        //WHEN
        mockMvc.perform(delete("/api/v1/orderService/order/{orderId}/user/{userId}",orderId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isAccepted());
        mockMvc.perform(get("/api/v1/orderService/order/all/user/{userId}",userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderList.length()").value(0));

    }
    @Test
    public void deleteOrder_notExistingOrder_doesNotDeleteOrder() throws Exception{
        //GIVEN
        String userId = orderEntity.getUserId();
        String invalidOrderId = UUID.randomUUID().toString();
        //WHEN
        mockMvc.perform(delete("/api/v1/orderService/order/{orderId}/user/{userId}",invalidOrderId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isBadRequest());
    }

}
