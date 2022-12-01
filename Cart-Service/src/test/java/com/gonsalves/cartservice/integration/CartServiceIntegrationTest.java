package com.gonsalves.cartservice.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.cartservice.IntegrationTest;
import com.gonsalves.cartservice.controller.model.AddItemToCartRequest;
import com.gonsalves.cartservice.controller.model.CartResponse;
import com.gonsalves.cartservice.controller.model.RemoveItemFromCartRequest;
import com.gonsalves.cartservice.controller.model.UpdateItemQuantityRequest;
import com.gonsalves.cartservice.integration.configuration.DynamoDBMapperTestConfiguration;
import com.gonsalves.cartservice.integration.configuration.DynamoDBTestConfiguration;
import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import com.gonsalves.cartservice.service.model.CartItem;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;
@Import({DynamoDBMapperTestConfiguration.class, DynamoDBTestConfiguration.class})
@IntegrationTest
class CartServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    AmazonDynamoDB amazonDynamoDB;

    private String firstItemId;
    private String firstItemProductId;
    private int firstItemQuantity;
    private double firstItemPrice;
    private String firstItemName;
    private String firstItemProductImageUrl;
    private String secondItemId;
    private String secondItemProductId;
    private int secondItemQuantity;
    private double secondItemPrice;
    private String userId;
    private final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    protected void setup() {
        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("item_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("user_id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("product_id").withAttributeType("S"));

        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("item_id").withKeyType(KeyType.RANGE));

        List<KeySchemaElement> lsiKeySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("product_id").withKeyType(KeyType.RANGE));

        LocalSecondaryIndex lsi = new LocalSecondaryIndex()
                .withIndexName(CartItemEntity.PRODUCT_ID_INDEX)
                .withKeySchema(lsiKeySchema)
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

        CreateTableRequest request = new CreateTableRequest()
                .withTableName("Ecommerce-CartService-CartItems")
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

        this.firstItemId = UUID.randomUUID().toString();
        this.firstItemProductId = UUID.randomUUID().toString();
        this.secondItemId = UUID.randomUUID().toString();
        this.secondItemProductId = UUID.randomUUID().toString();
        this.userId = UUID.randomUUID().toString();
        this.firstItemPrice = mockNeat.doubles().val();
        this.secondItemPrice = mockNeat.doubles().val();
        this.firstItemQuantity = mockNeat.ints().range(1,10).val();
        this.secondItemQuantity = mockNeat.ints().range(1,10).val();
        this.firstItemName = mockNeat.cars().valStr();
        this.firstItemProductImageUrl = mockNeat.urls().valStr();

        Map<String, AttributeValue> firstAttributeValueMap = new HashMap<>();
        firstAttributeValueMap.put("item_id", new AttributeValue().withS(firstItemId));
        firstAttributeValueMap.put("user_id", new AttributeValue().withS(userId));
        firstAttributeValueMap.put("product_id", new AttributeValue().withS(firstItemProductId));
        firstAttributeValueMap.put("product_price", new AttributeValue().withN(String.valueOf(firstItemPrice)));
        firstAttributeValueMap.put("quantity", new AttributeValue().withN(String.valueOf(firstItemQuantity)));
        firstAttributeValueMap.put("product_image_url", new AttributeValue().withS(firstItemProductImageUrl));
        firstAttributeValueMap.put("product_name", new AttributeValue().withS(firstItemName));

        Map<String, AttributeValue> secondAttributeValueMap = new HashMap<>();
        secondAttributeValueMap.put("item_id", new AttributeValue().withS(secondItemId));
        secondAttributeValueMap.put("user_id", new AttributeValue().withS(userId));
        secondAttributeValueMap.put("product_id", new AttributeValue().withS(secondItemProductId));
        secondAttributeValueMap.put("product_price", new AttributeValue().withN(String.valueOf(secondItemPrice)));
        secondAttributeValueMap.put("quantity", new AttributeValue().withN(String.valueOf(secondItemQuantity)));
        secondAttributeValueMap.put("product_image_url", new AttributeValue().withS(mockNeat.urls().valStr()));
        secondAttributeValueMap.put("product_name", new AttributeValue().withS(mockNeat.cars().valStr()));
        
        PutItemRequest putItemRequest = new PutItemRequest("Ecommerce-CartService-CartItems", firstAttributeValueMap);
        amazonDynamoDB.putItem(putItemRequest);

        putItemRequest = new PutItemRequest("Ecommerce-CartService-CartItems", secondAttributeValueMap);
        amazonDynamoDB.putItem(putItemRequest);
    }

    @AfterEach
    public void cleanUp(){
        DeleteTableRequest request = new DeleteTableRequest();
        request.setTableName("Ecommerce-CartService-CartItems");

        amazonDynamoDB.deleteTable(request);
    }

    @Test
    public void getCart_returnsAllCartItemsForUserId() throws Exception {
        //GIVEN
        double expectedSubtotal = (firstItemQuantity*firstItemPrice) + (secondItemPrice*secondItemQuantity);
        //WHEN
        mockMvc.perform(get("/api/v1/cartService/cart/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                )
        //THEN
                .andExpect(jsonPath("$.cartItems.length()").value(2))
                .andExpect(jsonPath("$.subtotal").value(expectedSubtotal))
                .andExpect(status().isOk());
    }

    @Test
    public void addCartItem_itemDoesNotExist_itemPersisted() throws Exception {
        //GIVEN
        String userId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        String productName = mockNeat.names().valStr();
        String productImageUrl = mockNeat.urls().valStr();
        double productPrice = mockNeat.doubles().val();
        int quantity = mockNeat.ints().upperBound(10).val();
        AddItemToCartRequest request = new AddItemToCartRequest(userId,quantity, productId, productName, productImageUrl, productPrice);
        String json = mapper.writeValueAsString(request);

        //WHEN
        mockMvc.perform(post("/api/v1/cartService/cart/{userId}/cartItem", userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        //THEN
                .andExpect(status().isCreated());
    }

    @Test
    public void addCartItem_itemAlreadyExist_itemQuantityIncremented() throws Exception {
        //GIVEN
        int additionalQuantity = 3;
        int expectedQuantity = firstItemQuantity + additionalQuantity;
        AddItemToCartRequest request = new AddItemToCartRequest(
                userId,
                additionalQuantity,
                firstItemProductId,
                firstItemName,
                firstItemProductImageUrl,
                firstItemPrice);

        //WHEN
        mockMvc.perform(post("/api/v1/cartService/cart/{userId}/cartItem", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        //THEN
        String jsonReponse = mockMvc.perform(get("/api/v1/cartService/cart/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CartResponse response = mapper.readValue(jsonReponse, CartResponse.class);
        CartItem firstCartItem = null;
        for (CartItem cartItem : response.getCartItems())
            if (cartItem.getId().equals(firstItemId))
                firstCartItem = cartItem;

        Assertions.assertEquals(expectedQuantity, firstCartItem.getQuantity());
    }

    @Test
    public void updateItemQuantity_existingItem_updatesItemQuanity() throws Exception {
        //GIVEN
        int updatedQuantity = 13;

        UpdateItemQuantityRequest updateRequest = new UpdateItemQuantityRequest(firstItemId, userId, updatedQuantity);

        //WHEN
        mockMvc.perform(put("/api/v1/cartService/cart/{userId}/cartItem", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateRequest)))
                .andExpect(status().isAccepted());
        //THEN
        String jsonReponse = mockMvc.perform(get("/api/v1/cartService/cart/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CartResponse response = mapper.readValue(jsonReponse, CartResponse.class);
        CartItem firstCartItem = null;
        for (CartItem cartItem : response.getCartItems())
            if (cartItem.getId().equals(firstItemId))
                firstCartItem = cartItem;

        Assertions.assertEquals(updatedQuantity, firstCartItem.getQuantity());
    }

    @Test
    public void updateItemQuantity_notExistingItem_responseBadRequest() throws Exception {
        //GIVEN
        String id = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        int updatedQuantity = 13;

        UpdateItemQuantityRequest updateRequest = new UpdateItemQuantityRequest(id, userId, updatedQuantity);

        //WHEN
        mockMvc.perform(put("/api/v1/cartService/cart/{userId}/cartItem", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isBadRequest());

    }

    @Test
    public void removeItem_multipleItemsInCart_removesTheOneItem() throws Exception {
        //GIVEN

        //WHEN
        mockMvc.perform(delete("/api/v1/cartService/cart/{userId}/cartItem/{cartItemId}", userId, firstItemId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        //THEN
        String jsonGetResponse = mockMvc.perform(get("/api/v1/cartService/cart/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();

        CartResponse cartResponse = mapper.readValue(jsonGetResponse, CartResponse.class);
        boolean succeeded = true;

        for (CartItem cartItem: cartResponse.getCartItems())
            if (cartItem.getId().equals(firstItemId))
                succeeded = false;

        Assertions.assertEquals(1, cartResponse.getCartItems().size());
        Assertions.assertTrue(succeeded);
        Assertions.assertEquals(secondItemId, cartResponse.getCartItems().get(0).getId());
    }

    @Test
    public void clearCart_multipleItemsInCart_allItemsRemoved() throws Exception {
        //GIVEN


        //WHEN
        mockMvc.perform(delete("/api/v1/cartService/cart/{userId}",userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        //THEN
        String jsonResponse = mockMvc.perform(get("/api/v1/cartService/cart/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
        CartResponse cartResponse = mapper.readValue(jsonResponse, CartResponse.class);

        Assertions.assertEquals(0, cartResponse.getCartItems().size());
    }
}
