package com.gonsalves.productservice.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.productservice.IntegrationTest;
import com.gonsalves.productservice.controller.model.ProductCreateRequest;
import com.gonsalves.productservice.controller.model.ProductUpdateRequest;
import com.gonsalves.productservice.integration.configuration.DynamoDBMapperTestConfiguration;
import com.gonsalves.productservice.integration.configuration.DynamoDBTestConfiguration;
import com.gonsalves.productservice.repository.ProductRepository;
import com.gonsalves.productservice.repository.entity.Category;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
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
    @Autowired
    private  AmazonDynamoDB amazonDynamoDB;
    @Autowired
    private ProductRepository productRepository;
    private  final MockNeat mockNeat = MockNeat.threadLocal();

    private final ObjectMapper mapper = new ObjectMapper();

    private  String firstProductId;
    private  String firstProductName;
    private  double firstProductPrice;
    private  String firstProductDescription;
    private  String firstProductImageUrl;
    private  double firstProductRating;



    @BeforeEach
    public void setup() {

        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("name").withAttributeType("S"));

        List<KeySchemaElement> keySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH),
                new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.RANGE));

        List<KeySchemaElement> gsiKeySchema = Arrays.asList(
                new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH));

        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex()
                .withIndexName("name-index")
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
        firstProductName = mockNeat.cars().valStr();
        firstProductPrice = mockNeat.doubles().val();
        firstProductDescription = mockNeat.words().valStr();
        firstProductImageUrl = mockNeat.urls().valStr();
        firstProductRating = mockNeat.doubles().val();

        ProductEntity firstEntity = ProductEntity.builder()
                .name(firstProductName)
                .price(firstProductPrice)
                .description(firstProductDescription)
                .imageUrl(firstProductImageUrl)
                .category(Category.BEVERAGES)
                .rating(firstProductRating)
                .build();
        ProductEntity secondEntity = ProductEntity.builder()
                .name(mockNeat.cars().valStr())
                .price(mockNeat.doubles().val())
                .description(mockNeat.words().valStr())
                .imageUrl(mockNeat.urls().valStr())
                .category(Category.DRY_GOODS)
                .rating(mockNeat.doubles().val())
                .build();

        productRepository.create(firstEntity);
        productRepository.create(secondEntity);

        firstProductId = productRepository.loadProductWithProductName(firstProductName).get(0).getProductId();
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

        //WHEN
        mockMvc.perform(get("/api/v1/productService/allProducts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))

        //THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productList.length()").value(2));

    }

    @Test
    public void getProductWithProductName_validProductName_returnsCorrectProduct() throws Exception {
        //GIVEN

        //WHEN
        mockMvc.perform(get(String.format("/api/v1/productService/%s", firstProductName))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))

        //THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(firstProductId))
                .andExpect(jsonPath("$.name").value(firstProductName));
    }
    
    @Test
    public void getProductWithProductName_invalidProductName_responseNotFound() throws Exception {
        //GIVEN
        String invalidProductName = mockNeat.strings().valStr();
        //WHEN
        mockMvc.perform(get(String.format("/api/v1/productService/%s", invalidProductName))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))

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

        ProductCreateRequest createRequest = new ProductCreateRequest(name, price, description, category, imageUrl);
        //WHEN
        mockMvc.perform(post("/api/v1/productService/product")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
        //THEN
                .andExpect(status().isCreated());
        mockMvc.perform(get(String.format("/api/v1/productService/%s", name))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void createProduct_existingProduct_responseConflict() throws Exception {
        //GIVEN
        ProductCreateRequest createRequest = new ProductCreateRequest(firstProductName, firstProductPrice, firstProductDescription, Category.BEVERAGES.toString(), firstProductImageUrl);

        //WHEN
        mockMvc.perform(post("/api/v1/productService/product")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)))
        //THEN
                .andExpect(status().isConflict());
    }

    @Test
    public void updateProductWithProductName_existingProduct_productUpdated() throws Exception {
        //GIVEN
        Category updatedCategory = Category.FROZEN_FOODS;
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                firstProductId,
                firstProductName,
                firstProductPrice,
                firstProductDescription,
                updatedCategory.toString(),
                firstProductImageUrl,
                firstProductRating);

        //WHEN
        mockMvc.perform(put("/api/v1/productService/product")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isAccepted());
        mockMvc.perform(get(String.format("/api/v1/productService/%s", firstProductName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(firstProductId))
                .andExpect(jsonPath("$.category").value(updatedCategory.toString()));
    }

    @Test
    public void updateProductWithProductName_notExistingProduct_responseBadRequest() throws Exception {
        //GIVEN
        String invalidProductName = mockNeat.strings().valStr();
        Category updatedCategory = Category.FROZEN_FOODS;
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                firstProductId,
                invalidProductName,
                firstProductPrice,
                firstProductDescription,
                updatedCategory.toString(),
                firstProductImageUrl,
                firstProductRating);

        //WHEN
        mockMvc.perform(put("/api/v1/productService/product")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
        //THEN
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteProductWithProductName_existingProduct_deletesProduct() throws Exception {
        //GIVEN

        //WHEN
        mockMvc.perform(delete(String.format("/api/v1/productService/product?name=%s", firstProductName))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        //THEN
                .andExpect(status().isNoContent());

        mockMvc.perform(get(String.format("/api/v1/productService/%s", firstProductName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test
    public void deleteProductWithProductName_notExistingProduct_responseBadRequestNoDeletion() throws Exception {
        //GIVEN
        String invalidProductName = mockNeat.strings().valStr();
        //WHEN
        mockMvc.perform(delete(String.format("/api/v1/productService/product?name=%s", invalidProductName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                //THEN
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/productService/allProducts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productList.length()").value(2));

    }
}