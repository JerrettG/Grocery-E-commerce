package com.gonsalves.productservice.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
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
    @Autowired
    private ProductRepository productRepository;
    public Product loadProductWithProductName(String name) {
        List<ProductEntity> results = productRepository.loadProductWithProductName(name);
        if (results.size() > 0)
            return createProductFromEntity(results.get(0));
        log.error("Product with specified name does not exist.");
        throw new ProductNotFoundException("Product with specified name does not exist.");
    }

    public List<Product> loadAllProducts() {
        List<ProductEntity> results = productRepository.loadAll();
        List<Product> products = new ArrayList<>();
        results.forEach(entity -> products.add(createProductFromEntity(entity)));

        return products;
    }

    public void createProduct(Product product) {
        String productName = product.getName();
            try {
                loadProductWithProductName(productName);
                log.error(String.format("Resource with name: %s already exists.", productName));
                throw new ProductAlreadyExistsException("Cannot create resource. Product with specified name already exists.");
            } catch (ProductNotFoundException e) {
                productRepository.create(createEntityFromProduct(product));
                log.info(String.format("Created Product with name: %s", productName));
            }
    }
    public void updateProduct(Product product) {
            ProductEntity entity = createEntityFromProduct(product);
            try {
                productRepository.update(entity);
                log.info(String.format("Resource with name: %s has been updated.", product.getName()));
            } catch (ConditionalCheckFailedException e) {
                throw new ProductNotFoundException("Cannot update non-existent product");
            }
    }

    public void deleteProduct(String name) {
            Product Product = loadProductWithProductName(name);
            ProductEntity entity = ProductEntity.builder()
                            .productId(Product.getProductId())
                            .name(name)
                            .build();
            productRepository.delete(entity);
            log.info(String.format("Resource with name: %s has been successfully deleted.", Product.getName()));
    }

    private Product createProductFromEntity(ProductEntity productEntity) {
        return Product.builder()
                .productId(productEntity.getProductId())
                .name(productEntity.getName())
                .price(productEntity.getPrice())
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
                .description(product.getDescription())
                .category(Category.valueOf(product.getCategory()))
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                .build();
    }
}
