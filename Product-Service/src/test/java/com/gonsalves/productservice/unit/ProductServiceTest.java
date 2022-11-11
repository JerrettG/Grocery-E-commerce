package com.gonsalves.productservice.unit;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.productservice.repository.entity.Category;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import com.gonsalves.productservice.exception.ProductAlreadyExistsException;
import com.gonsalves.productservice.exception.ProductNotFoundException;
import com.gonsalves.productservice.repository.ProductRepository;
import com.gonsalves.productservice.service.ProductService;
import com.gonsalves.productservice.service.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    private String productId;
    private Product product;

    private ProductEntity productEntity;

    private String productName;
    private String description;
    private String imageUrl;

    @BeforeEach
    public void before() {
        this.productName = "Beef Tenderloin";
        this.productId = UUID.randomUUID().toString();
        this.description = "Test description";
        this.imageUrl = "/demo_images/beefTenderloin.jpg";
        this.product = Product.builder()
                .productId(productId)
                .name(productName)
                .price(4.99)
                .description(description)
                .category(Category.MEAT_POULTRY_AND_SEAFOOD.toString())
                .imageUrl(imageUrl)
                .rating(4.7)
                .build();
        this.productEntity = new ProductEntity(
                productName,
                4.99,
                description,
                Category.MEAT_POULTRY_AND_SEAFOOD,
                imageUrl,
                4.7);
        this.productEntity.setProductId(productId);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loadProductWithProductName() {

        when(productRepository.loadProductWithProductName(productEntity.getName())).thenReturn(Arrays.asList(productEntity));
        Product result = productService.loadProductWithProductName("Beef Tenderloin");

        assertEquals(productEntity.getProductId(), result.getProductId(), "Expected result to have matching productEntity id when loading by productEntity name, but did not");
    }

    @Test
    public void loadProductWithProductName_productDoesNotExist_throwsProductNotFoundException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());

        assertThrows(ProductNotFoundException.class, ()->productService.loadProductWithProductName(productName),
                "Expected ProductNotFoundException to be thrown when loading non-existent productEntity, but was not.");
    }

    @Test
    public void createProduct() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());
        productService.createProduct(product);

        verify(productRepository).create(eq(productEntity));
    }

    @Test
    public void createProduct_productAlreadyExists_throwsProductAlreadyExistsException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));

       assertThrows(ProductAlreadyExistsException.class, ()->productService.createProduct(product),
               "Expected ProductAlreadyExistsException to be thrown when creating productEntity with the same name, but was not.");
    }

    @Test
    public void updateProduct() {
        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));

        productService.updateProduct(product);

        verify(productRepository).update(eq(productEntity));
    }
    @Test
    public void updateProduct_productDoesNotExist_throwsProductNotFoundException() {
        doThrow(ConditionalCheckFailedException.class).when(productRepository).update(eq(productEntity));

        assertThrows(ProductNotFoundException.class, ()-> productService.updateProduct(product),
                "Expected updating producting that does not exist to throw ProductNotFoundException, but did not.");
    }

    @Test
    public void deleteProduct() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));
        productService.deleteProduct(productName);

        verify(productRepository).delete(productEntity);
    }

    @Test
    public void deleteProduct_productDoesNotExists_throwsProductNotFoundException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());

        assertThrows(ProductNotFoundException.class, ()->productService.deleteProduct(productName),
                "Expected delete of productEntity that does not exist to throw ProductNotFoundException, but did not.");
    }
}
