package com.gonsalves.ProductService.controller;

import com.gonsalves.ProductService.entity.Product;
import com.gonsalves.ProductService.entity.ProductList;
import com.gonsalves.ProductService.exception.ProductAlreadyExistsException;
import com.gonsalves.ProductService.exception.ProductNotFoundException;
import com.gonsalves.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("ProductController")
@RequestMapping(value = "/api/v1/productService")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping({"/", "/allProducts"})
    public ResponseEntity<ProductList> getAllProducts() {
        List<Product> results = productService.loadAllProducts();
        ProductList body = new ProductList(results);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/{productName}")
    public ResponseEntity<Product> getProductWithProductName(@PathVariable(name = "productName") String name) {
        try {
            Product result = productService.loadProductWithProductName(name);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/product")
    public ResponseEntity<String> createProduct(@RequestBody Product product) {
        try {
            productService.createProduct(product);
            return new ResponseEntity<>("Resource created successfully", HttpStatus.CREATED);
        } catch (ProductAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
    @PutMapping("/product")
    public ResponseEntity<Product> updateProductWithProductName(@RequestBody Product product) {
        try {
            productService.updateProduct(product);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/product")
    public ResponseEntity<String> deleteProductWithProductName(@RequestParam(name = "name")String name) {
        try {
            productService.deleteProduct(name);
            return new ResponseEntity<>("Successfully deleted resource.", HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}
