package com.gonsalves.productservice.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.productservice.config.CacheStore;
import com.gonsalves.productservice.repository.entity.Category;

import com.gonsalves.productservice.exception.ProductAlreadyExistsException;
import com.gonsalves.productservice.exception.ProductNotFoundException;
import com.gonsalves.productservice.repository.ProductRepository;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import com.gonsalves.productservice.service.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CacheStore cache;

    @Autowired
    public ProductService(ProductRepository productRepository, CacheStore cache) {
        this.productRepository = productRepository;
        this.cache = cache;
    }

    public Product loadProductWithProductName(String name) {
        Product cachedProduct = cache.getByProductName(name);

        if (cachedProduct != null)
            return cachedProduct;

        List<ProductEntity> results = productRepository.loadProductWithProductName(name);

        if (results.size() > 0) {
            Product productFromDatabase = createProductFromEntity(results.get(0));
            cache.addByProductName(productFromDatabase.getName(), productFromDatabase);
            return productFromDatabase;
        }
        else {
            log.error("Product with specified name does not exist.");
            throw new ProductNotFoundException("Product with specified name does not exist.");
        }
    }

    public List<Product> loadAllProducts() {
        List<Product> cachedProducts = cache.getByCategory("ALL");
        if (cachedProducts != null)
            return cachedProducts;

        List<Product> productsFromDatabase = new ArrayList<>();
        productRepository.loadAll()
                .forEach(entity -> productsFromDatabase.add(createProductFromEntity(entity)));
        cache.addByCategory("ALL", productsFromDatabase);
        return productsFromDatabase;
    }

    public List<Product> loadAllProductsInCategory(String category) {
        List<Product> cachedProducts = cache.getByCategory(category);
        if (cachedProducts != null)
            return cachedProducts;

        List<Product> productsFromDatabase = new ArrayList<>();
        productRepository.loadAllProductsInCategory(Category.valueOf(category)).
                forEach(entity -> productsFromDatabase.add(createProductFromEntity(entity)));
        cache.addByCategory(category, productsFromDatabase);
        return productsFromDatabase;
    }




    public void createProduct(Product product) {
        String productName = product.getName();
            try {
                loadProductWithProductName(productName);
                log.error(String.format("Resource with name: %s already exists.", productName));
                throw new ProductAlreadyExistsException("Cannot create resource. Product with specified name already exists.");
            } catch (ProductNotFoundException e) {
                productRepository.create(createEntityFromProduct(product));
                cache.evictByCategory(product.getCategory());
                log.info(String.format("Created Product with name: %s", productName));
            }
    }
    public void updateProduct(Product product) {
            ProductEntity entity = createEntityFromProduct(product);
            try {
                productRepository.update(entity);
                cache.evictByProductName(product.getName());
                cache.evictByCategory(product.getCategory());
                log.info(String.format("Resource with name: %s has been updated.", product.getName()));
            } catch (ConditionalCheckFailedException e) {
                throw new ProductNotFoundException("Cannot update non-existent product");
            }
    }

    public void deleteProduct(String name) {
            Product product = loadProductWithProductName(name);
            ProductEntity entity = ProductEntity.builder()
                            .productId(product.getProductId())
                            .name(name)
                            .build();
            productRepository.delete(entity);
            cache.evictByProductName(name);
            cache.evictByCategory(product.getCategory());
            log.info(String.format("Resource with name: %s has been successfully deleted.", product.getName()));
    }

    private Product createProductFromEntity(ProductEntity productEntity) {
        return Product.builder()
                .productId(productEntity.getProductId())
                .name(productEntity.getName())
                .price(productEntity.getPrice())
                .unitMeasurement(productEntity.getUnitMeasurement())
                .description(productEntity.getDescription())
                .category(productEntity.getCategory().toString())
                .imageUrl(productEntity.getImageUrl())
                .rating(productEntity.getRating())
                .build();
    }
    private ProductEntity createEntityFromProduct(Product product) {
        return ProductEntity.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .unitMeasurement(product.getUnitMeasurement())
                .description(product.getDescription())
                .category(Category.valueOf(product.getCategory()))
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                .build();
    }
}
