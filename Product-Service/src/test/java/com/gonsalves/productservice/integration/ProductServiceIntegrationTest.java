package com.gonsalves.productservice.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.productservice.IntegrationTest;
import com.gonsalves.productservice.controller.model.ProductCreateRequest;
import com.gonsalves.productservice.controller.model.ProductResponse;
import com.gonsalves.productservice.controller.model.ProductUpdateRequest;
import com.gonsalves.productservice.integration.configuration.DynamoDBMapperTestConfiguration;
import com.gonsalves.productservice.integration.configuration.DynamoDBTestConfiguration;
import com.gonsalves.productservice.integration.configuration.Utility;
import com.gonsalves.productservice.repository.ProductRepository;
import com.gonsalves.productservice.repository.entity.Category;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Import({DynamoDBTestConfiguration.class, DynamoDBMapperTestConfiguration.class})
@IntegrationTest
public class ProductServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private Utility utility;
    @Autowired
    private  AmazonDynamoDB amazonDynamoDB;
    private  final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();



    @BeforeEach
    public void setup() {
        utility = new Utility(mockMvc);
        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("name").withAttributeType("S"));

        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.RANGE));

        List<KeySchemaElement> gsiKeySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH));

        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex()
                .withIndexName(ProductEntity.NAME_INDEX)
                .withKeySchema(gsiKeySchema)
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 2L));

        CreateTableRequest request = new CreateTableRequest()
                .withTableName("Ecommerce-ProductService-Products")
                .withKeySchema(keySchema)
                .withGlobalSecondaryIndexes(gsi)
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
    @AfterEach
    public void cleanUp(){
        DeleteTableRequest request = new DeleteTableRequest();
        request.setTableName("Ecommerce-ProductService-Products");

        amazonDynamoDB.deleteTable(request);
    }

    @Test
    public void getAllProducts_returnsListOfAllProducts() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);
        utility.productServiceClient.createProduct(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.productServiceClient.getAllProducts()
        //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.productList.length()").value(1)
                );

    }

    @Test
    public void getProductWithProductName_validProductName_returnsCorrectProduct() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);
        utility.productServiceClient.createProduct(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.productServiceClient.getProductByProductName(name)

        //THEN
                .andExpectAll(
                        status().isOk(),
                        jsonPath("name").value(name),
                        jsonPath("price").value(price),
                        jsonPath("unitMeasurement").value(unitMeasurement),
                        jsonPath("description").value(description),
                        jsonPath("category").value(category),
                        jsonPath("imageUrl").value(imageUrl)
                );

    }
    
    @Test
    public void getProductWithProductName_invalidProductName_responseNotFound() throws Exception {
        //GIVEN
        String invalidProductName = mockNeat.strings().valStr();
        //WHEN
        utility.productServiceClient.getProductByProductName(invalidProductName)

        //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void createProduct_notExistingProduct_createsProduct() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);
        //WHEN
        utility.productServiceClient.createProduct(createRequest)
        //THEN
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("name").value(name),
                        jsonPath("price").value(price),
                        jsonPath("unitMeasurement").value(unitMeasurement),
                        jsonPath("description").value(description),
                        jsonPath("category").value(category),
                        jsonPath("imageUrl").value(imageUrl)
                );

    }

    @Test
    public void createProduct_existingProduct_responseConflict() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);
        utility.productServiceClient.createProduct(createRequest)
                        .andExpect(status().isCreated());
        //WHEN
        utility.productServiceClient.createProduct(createRequest)
        //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateProductWithProductName_existingProduct_productUpdated() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);

        String jsonResponse = utility.productServiceClient.createProduct(createRequest)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ProductResponse createResponse = mapper.readValue(jsonResponse, ProductResponse.class);

        Category updatedCategory = Category.FROZEN_FOODS;
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                createResponse.getProductId(),
                createResponse.getName(),
                createResponse.getPrice(),
                createResponse.getUnitMeasurement(),
                createResponse.getDescription(),
                updatedCategory.toString(),
                createResponse.getImageUrl(),
                createResponse.getRating());

        //WHEN
        utility.productServiceClient.updateProduct(updateRequest)
        //THEN
                .andExpect(status().isAccepted());
        utility.productServiceClient.getProductByProductName(createResponse.getName())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.productId").value(createResponse.getProductId()),
                        jsonPath("$.category").value(updatedCategory.toString())
                );

    }

    @Test
    public void updateProductWithProductName_notExistingProduct_responseBadRequest() throws Exception {
        //GIVEN
        String invalidProductName = mockNeat.strings().valStr();
        Category updatedCategory = Category.FROZEN_FOODS;
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                UUID.randomUUID().toString(),
                invalidProductName,
                mockNeat.doubles().val(),
                "1 lb",
                mockNeat.strings().valStr(),
                updatedCategory.toString(),
                mockNeat.urls().valStr(),
                mockNeat.doubles().range(0.00, 5.00).val()
        );

        //WHEN
        utility.productServiceClient.updateProduct(updateRequest)
        //THEN
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteProductWithProductName_existingProduct_deletesProduct() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);
        utility.productServiceClient.createProduct(createRequest)
                .andExpect(status().isCreated());
        //WHEN
        utility.productServiceClient.deleteProduct(name)
        //THEN
                .andExpect(status().isAccepted());

        utility.productServiceClient.getProductByProductName(name)
                .andExpect(status().isNotFound());
    }
    @Test
    public void deleteProductWithProductName_notExistingProduct_responseBadRequestNoDeletion() throws Exception {
        //GIVEN
        String name = mockNeat.strings().valStr();
        double price = mockNeat.doubles().val();
        String description = mockNeat.strings().valStr();
        String category = Category.BREAKFAST_AND_CEREAL.toString();
        String imageUrl = mockNeat.urls().valStr();
        String unitMeasurement = "1 lb";

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, unitMeasurement, description, category, imageUrl);
        utility.productServiceClient.createProduct(createRequest)
                .andExpect(status().isCreated());

        String invalidProductName = mockNeat.strings().valStr();
        //WHEN
        utility.productServiceClient.deleteProduct(invalidProductName)
                //THEN
                .andExpect(status().isNotFound());

        utility.productServiceClient.getAllProducts()
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.productList.length()").value(1)
                );

    }
}