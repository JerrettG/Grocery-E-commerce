package com.gonsalves.productservice.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.productservice.caching.CacheStore;
import com.gonsalves.productservice.caching.DistributedCache;
import com.gonsalves.productservice.repository.entity.Category;

import com.gonsalves.productservice.exception.ProductAlreadyExistsException;
import com.gonsalves.productservice.exception.ProductNotFoundException;
import com.gonsalves.productservice.repository.ProductRepository;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import com.gonsalves.productservice.service.model.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
private final String PRODUCT_LIST_KEY = "Product-list::%s";
    private final ProductRepository productRepository;
    private final CacheStore cache;

    private final DistributedCache distributedCache;

    private final Gson gson;
    @Autowired
    public ProductService(ProductRepository productRepository, CacheStore cache, DistributedCache distributedCache) {
        this.productRepository = productRepository;
        this.cache = cache;
        this.distributedCache = distributedCache;
        gson = new GsonBuilder().create();
    }

    public Product loadProductWithProductName(String name) {
        Optional<Product> cachedProduct = cache.getByProductName(name);

        if (cachedProduct.isPresent())
            return cachedProduct.get();

        ProductEntity entity = productRepository.loadProductWithProductName(name)
                .orElseThrow(() -> new ProductNotFoundException("Product with specified name does not exist."));

        Product productFromDatabase = convertFromEntity(entity);
        cache.addByProductName(productFromDatabase.getName(), productFromDatabase);
        return productFromDatabase;
    }

    public List<Product> loadAllProducts() {
        String key = String.format(PRODUCT_LIST_KEY, "ALL");
//        Optional<List<Product>> cachedProducts = cache.getByCategory("ALL");
        Optional<String> cachedProducts = distributedCache.getValue(key);
        if (cachedProducts.isPresent())
            return fromJson(cachedProducts.get());

        List<Product> productsFromDatabase = productRepository.loadAll().stream()
                .map(this::convertFromEntity)
                .collect(Collectors.toList());
//        cache.addByCategory("ALL", productsFromDatabase);
        addToDistributedCache(key, productsFromDatabase);
        return productsFromDatabase;
    }

    public List<Product> loadAllProductsInCategory(String category) {
        Optional<List<Product>> cachedProducts = cache.getByCategory(category);
        if (cachedProducts.isPresent())
            return cachedProducts.get();

        List<Product> productsFromDatabase = productRepository.loadAllProductsInCategory(Category.valueOf(category)).stream()
                        .map(this::convertFromEntity)
                        .collect(Collectors.toList());
        cache.addByCategory(category, productsFromDatabase);
        return productsFromDatabase;
    }




    public Product createProduct(Product product) {
        String productName = product.getName();
            try {
                loadProductWithProductName(productName);
                log.error(String.format("Resource with name: %s already exists.", productName));
                throw new ProductAlreadyExistsException("Cannot create resource. Product with specified name already exists.");
            } catch (ProductNotFoundException e) {
                product.setProductId(UUID.randomUUID().toString());
                productRepository.create(convertToEntity(product));
                cache.evictByCategory(product.getCategory());
                log.info(String.format("Created Product with name: %s", productName));
                return product;
            }
    }
    public void updateProduct(Product product) {
            ProductEntity entity = convertToEntity(product);
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

    private Product convertFromEntity(ProductEntity productEntity) {
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
    private ProductEntity convertToEntity(Product product) {
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


    private void addToDistributedCache(String key, List<Product> products) {
        distributedCache.setValue(key, 60*60, gson.toJson(products));
    }

    private List<Product> fromJson(String json) {
        return gson.fromJson(json, new TypeToken<ArrayList<Product>>() { }.getType());
    }
}
