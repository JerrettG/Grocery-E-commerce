package com.gonsalves.cartservice.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.cartservice.IntegrationTest;
import com.gonsalves.cartservice.controller.model.AddItemToCartRequest;
import com.gonsalves.cartservice.controller.model.CartItemResponse;
import com.gonsalves.cartservice.controller.model.UpdateItemQuantityRequest;
import com.gonsalves.cartservice.integration.configuration.DynamoDBMapperTestConfiguration;
import com.gonsalves.cartservice.integration.configuration.DynamoDBTestConfiguration;
import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;
@Import({DynamoDBMapperTestConfiguration.class, DynamoDBTestConfiguration.class})
@IntegrationTest
class CartServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private Utility utility;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    private String userId;
    private final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    protected void setup() {
        utility = new Utility(mockMvc);
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
        this.userId = UUID.randomUUID().toString();

    }


    @Test
    public void getCart_returnsAllCartItemsForUserId() throws Exception {
        //GIVEN
        AddItemToCartRequest request1 = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );
        AddItemToCartRequest request2 = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );
        utility.cartServiceClient.addCartItem(request1)
                .andExpect(status().isCreated());
        utility.cartServiceClient.addCartItem(request2)
                .andExpect(status().isCreated());

        double expectedSubtotal = (request1.getQuantity()*request1.getProductPrice()) + (request2.getQuantity()*request2.getProductPrice());
        //WHEN
        utility.cartServiceClient.getCart(userId)
        //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("cartItems.length()").value(2),
                        jsonPath("subtotal").value(expectedSubtotal)
                );
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


        //WHEN
        utility.cartServiceClient.addCartItem(request)
        //THEN
                .andExpect(status().isCreated());
    }

    @Test
    public void addCartItem_itemAlreadyExist_itemQuantityIncremented() throws Exception {
        //GIVEN
        AddItemToCartRequest request1 = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );
        utility.cartServiceClient.addCartItem(request1)
                .andExpect(status().isCreated());
        int additionalQuantity = 3;
        int expectedQuantity = request1.getQuantity() + additionalQuantity;
        AddItemToCartRequest request2 = new AddItemToCartRequest(
                userId,
                additionalQuantity,
                request1.getProductId(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );
        //WHEN
        utility.cartServiceClient.addCartItem(request2)
                .andExpect(status().isCreated());
        //THEN
        utility.cartServiceClient.getCart(userId)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("cartItems.length()").value(1),
                        jsonPath("cartItems[0].quantity").value(expectedQuantity)
                );
    }

    @Test
    public void updateItemQuantity_existingItem_updatesItemQuantity() throws Exception {
        //GIVEN
        AddItemToCartRequest request = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );
        String jsonResponse = utility.cartServiceClient.addCartItem(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        CartItemResponse response = mapper.readValue(jsonResponse, CartItemResponse.class);
        String itemId = response.getId();
        int updatedQuantity = 13;

        UpdateItemQuantityRequest updateRequest = new UpdateItemQuantityRequest(itemId, userId, updatedQuantity);

        //WHEN
        utility.cartServiceClient.updateItemQuantity(updateRequest)
                .andExpect(status().isAccepted());
        //THEN
        utility.cartServiceClient.getCart(userId)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("cartItems[0].id").value(itemId),
                        jsonPath("cartItems[0].quantity").value(updatedQuantity)
                        );
    }

    @Test
    public void updateItemQuantity_notExistingItem_responseBadRequest() throws Exception {
        //GIVEN
        String id = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        int updatedQuantity = 13;

        UpdateItemQuantityRequest updateRequest = new UpdateItemQuantityRequest(id, userId, updatedQuantity);

        //WHEN
        utility.cartServiceClient.updateItemQuantity(updateRequest)
        //THEN
                .andExpect(status().isNotFound());

    }

    @Test
    public void removeItem_multipleItemsInCart_removesTheOneItem() throws Exception {
        //GIVEN
        AddItemToCartRequest request = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );

        CartItemResponse response = mapper.readValue(
                utility.cartServiceClient.addCartItem(request)
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString(),
                CartItemResponse.class);
        String firstItemId = response.getId();

        request = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );

        response = mapper.readValue(
                utility.cartServiceClient.addCartItem(request)
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString(),
                CartItemResponse.class);
        String secondItemId = response.getId();

        //WHEN
        utility.cartServiceClient.removeItem(userId, firstItemId)
                .andExpect(status().isAccepted());

        //THEN
        utility.cartServiceClient.getCart(userId)
                .andExpectAll(
                        jsonPath("cartItems.length()").value(1),
                        jsonPath("cartItems[*].id", not(contains(firstItemId))),
                        jsonPath("cartItems[*].id", contains(secondItemId))
                );
    }

    @Test
    public void clearCart_multipleItemsInCart_allItemsRemoved() throws Exception {
        //GIVEN
        AddItemToCartRequest request = new AddItemToCartRequest(
                userId,
                mockNeat.ints().range(1,10).val(),
                UUID.randomUUID().toString(),
                mockNeat.cars().valStr(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 100.00).val()
        );
        utility.cartServiceClient.addCartItem(request)
                        .andExpect(status().isCreated());

        //WHEN
        utility.cartServiceClient.clearCart(userId)
                .andExpect(status().isAccepted());

        //THEN
        utility.cartServiceClient.getCart(userId)
                        .andExpectAll(status().isOk(),jsonPath("cartItems.length()").value(0));
    }
}
