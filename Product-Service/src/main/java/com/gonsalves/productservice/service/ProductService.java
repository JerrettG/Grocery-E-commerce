package com.gonsalves.productservice.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.productservice.util.TypeConverter;
import com.gonsalves.productservice.caching.InMemoryCache;
import com.gonsalves.productservice.caching.DistributedCache;
import com.gonsalves.productservice.repository.entity.Category;

import com.gonsalves.productservice.exception.ProductAlreadyExistsException;
import com.gonsalves.productservice.exception.ProductNotFoundException;
import com.gonsalves.productservice.repository.ProductRepository;
import com.gonsalves.productservice.repository.entity.ProductEntity;
import com.gonsalves.productservice.service.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    private final String PRODUCT_CATEGORY_KEY = "Product-Category::%s";
    private final String PRODUCT_NAME_KEY = "Product-name::%s";
    private final int PRODUCT_CATEGORY_TTL = 60*60;
    private final int PRODUCT_NAME_TTL = 60*60;
    private final ProductRepository productRepository;

    private final DistributedCache distributedCache;

    @Autowired
    public ProductService(ProductRepository productRepository, DistributedCache distributedCache) {
        this.productRepository = productRepository;
        this.distributedCache = distributedCache;
    }

    public Product loadProductWithProductName(String name) {
        String key = String.format(PRODUCT_NAME_KEY, name);
        Optional<String> cachedProduct = distributedCache.getValue(key);
        if (cachedProduct.isPresent())
            return TypeConverter.fromJsonToProduct(cachedProduct.get());

        ProductEntity entity = productRepository.loadProductWithProductName(name)
                .orElseThrow(() -> new ProductNotFoundException("Product with specified name does not exist."));
        Product productFromDatabase = TypeConverter.convertFromEntity(entity);

        distributedCache.setValue(key, PRODUCT_NAME_TTL, TypeConverter.toJson(productFromDatabase));

        return productFromDatabase;
    }

    public List<Product> loadAllProducts() {
        String key = String.format(PRODUCT_CATEGORY_KEY, "ALL");
        Optional<String> cachedProducts = distributedCache.getValue(key);
        if (cachedProducts.isPresent())
            return TypeConverter.fromJsonToProductList(cachedProducts.get());

        List<Product> productsFromDatabase = productRepository.loadAll().stream()
                .map(TypeConverter::convertFromEntity)
                .collect(Collectors.toList());

        distributedCache.setValue(key, PRODUCT_CATEGORY_TTL, TypeConverter.toJson(productsFromDatabase));
        
        return productsFromDatabase;
    }

    public List<Product> loadAllProductsInCategory(String category) {
        String key = String.format(PRODUCT_CATEGORY_KEY, category);
        Optional<String> cachedProducts = distributedCache.getValue(key);
        if (cachedProducts.isPresent())
            return TypeConverter.fromJsonToProductList(cachedProducts.get());

        List<Product> productsFromDatabase = productRepository.loadAllProductsInCategory(Category.valueOf(category)).stream()
                        .map(TypeConverter::convertFromEntity)
                        .collect(Collectors.toList());

        distributedCache.setValue(key, PRODUCT_CATEGORY_TTL, TypeConverter.toJson(productsFromDatabase));
        
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
                productRepository.create(TypeConverter.convertToEntity(product));
                
                String key = String.format(PRODUCT_CATEGORY_KEY, product.getCategory());
                distributedCache.invalidate(key);
                log.info(String.format("Created Product with name: %s", productName));

                return product;
            }
    }
    public void updateProduct(Product product) {
            ProductEntity entity = TypeConverter.convertToEntity(product);
            try {
                productRepository.update(entity);

                String nameKey = String.format(PRODUCT_NAME_KEY, product.getName());
                String categoryKey = String.format(PRODUCT_CATEGORY_KEY, product.getCategory());
                distributedCache.invalidate(nameKey);
                distributedCache.invalidate(categoryKey);

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

            String nameKey = String.format(PRODUCT_NAME_KEY, product.getName());
            String categoryKey = String.format(PRODUCT_CATEGORY_KEY, product.getCategory());
            distributedCache.invalidate(nameKey);
            distributedCache.invalidate(categoryKey);

            log.info(String.format("Resource with name: %s has been successfully deleted.", product.getName()));
    }



}
