package com.gonsalves.productservice.unit;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.productservice.config.CacheStore;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CacheStore cacheStore;
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

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));
        Product result = productService.loadProductWithProductName(productName);

        assertEquals(productEntity.getProductId(), result.getProductId(), "Expected result to have matching productEntity id when loading by productEntity name, but did not");
    }

    @Test
    public void loadProductWithProductName_productDoesNotExist_throwsProductNotFoundException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());

        assertThrows(ProductNotFoundException.class, ()->productService.loadProductWithProductName(productName),
                "Expected ProductNotFoundException to be thrown when loading non-existent productEntity, but was not.");
    }

    @Test
    public void loadProductWithProductName_dataNotInCache_callsRepositoryAndAddsDataToCache() {
        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));
        productService.loadProductWithProductName(productName);

        verify(productRepository).loadProductWithProductName(productName);
        verify(cacheStore).addByProductName(eq(productName), any(Product.class));
    }

    @Test
    public void loadProductWithProductName_dataAlreadyInCache_doesNotCallRepository() {
        when(cacheStore.getByProductName(productName)).thenReturn(product);

        productService.loadProductWithProductName(productName);

        verifyNoInteractions(productRepository);
    }

    @Test
    public void loadAllProducts_dataNotInCache_callsRepositoryAndAddsToCache() {
        when(cacheStore.getByCategory("ALL")).thenReturn(null);

        productService.loadAllProducts();

        verify(productRepository).loadAll();
        verify(cacheStore).addByCategory(eq("ALL"), anyList());
    }
    @Test
    public void loadAllProducts_dataInCache_doesNotCallRepository() {
        when(cacheStore.getByCategory("ALL")).thenReturn(Arrays.asList(product));

        productService.loadAllProducts();

        verifyNoInteractions(productRepository);
    }

    @Test
    public void loadAllProductsInCategory_dataNotInCache_callsRepositoryAndAddsToCache() {
        String category = product.getCategory();
        when(cacheStore.getByCategory(category)).thenReturn(null);

        productService.loadAllProductsInCategory(category);

        verify(productRepository).loadAllProductsInCategory(Category.valueOf(category));
        verify(cacheStore).addByCategory(eq(category), anyList());
    }
    @Test
    public void loadAllProductsInCategory_dataInCache_doesNotCallRepository() {
        String category = product.getCategory();
        when(cacheStore.getByCategory(category)).thenReturn(Arrays.asList(product));

        productService.loadAllProductsInCategory(category);

        verifyNoInteractions(productRepository);
    }

    @Test
    public void createProduct_productDoesNotExist_createsProductAndEvictsCategoryCache() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());
        productService.createProduct(product);

        verify(productRepository).create(eq(productEntity));
        verify(cacheStore).evictByCategory(product.getCategory());
    }

    @Test
    public void createProduct_productAlreadyExists_throwsProductAlreadyExistsException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));

       assertThrows(ProductAlreadyExistsException.class, ()->productService.createProduct(product),
               "Expected ProductAlreadyExistsException to be thrown when creating productEntity with the same name, but was not.");
    }

    @Test
    public void updateProduct_productExists_updatesProductAndEvictsCache() {
        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));

        productService.updateProduct(product);

        verify(productRepository).update(eq(productEntity));
        verify(cacheStore).evictByProductName(productName);
    }

    @Test
    public void updateProduct_productDoesNotExist_throwsProductNotFoundException() {
        doThrow(ConditionalCheckFailedException.class).when(productRepository).update(eq(productEntity));

        assertThrows(ProductNotFoundException.class, ()-> productService.updateProduct(product),
                "Expected updating product that does not exist to throw ProductNotFoundException, but did not.");
    }

    @Test
    public void deleteProduct_productExists_deletesProductAndEvictsCache() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(productEntity));
        productService.deleteProduct(productName);

        verify(productRepository).delete(productEntity);
        verify(cacheStore).evictByProductName(productName);
    }

    @Test
    public void deleteProduct_productDoesNotExists_throwsProductNotFoundException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());

        assertThrows(ProductNotFoundException.class, ()->productService.deleteProduct(productName),
                "Expected delete of productEntity that does not exist to throw ProductNotFoundException, but did not.");
    }
}
