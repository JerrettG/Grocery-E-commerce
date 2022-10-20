package com.gonsalves.ProductService.service;

import com.gonsalves.ProductService.entity.Product;
import com.gonsalves.ProductService.exception.ProductAlreadyExistsException;
import com.gonsalves.ProductService.exception.ProductNotFoundException;
import com.gonsalves.ProductService.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
@Slf4j
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    public Product loadProductWithProductName(String name) {
        List<Product> results = productRepository.loadProductWithProductName(name);
        if (results.size() > 0)
            return results.get(0);
        log.error("Product with specified name does not exist.");
        throw new ProductNotFoundException("Product with specified name does not exist.");
    }

    public List<Product> loadAllProducts() {
        return productRepository.loadAll();
    }

    public void createProduct(Product product) {
        String productName = product.getName();
            try {
                loadProductWithProductName(productName);
                log.error(String.format("Resource with name: %s already exists.", productName));
                throw new ProductAlreadyExistsException("Cannot create resource. Product with specified name already exists.");
            } catch (ProductNotFoundException e) {
                productRepository.create(product);
                log.info(String.format("Created product with name: %s", productName));
            }
    }
    public void updateProduct(Product product) {
            loadProductWithProductName(product.getName());

            productRepository.update(product);
            log.info(String.format("Resource with name: %s has been updated.", product.getName()));
    }

    public void deleteProduct(String name) {
            Product product = loadProductWithProductName(name);
            productRepository.delete(product);
            log.info(String.format("Resource with name: %s has been successfully deleted.", product.getName()));
    }
}
